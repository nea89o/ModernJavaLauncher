package moe.nea.modernjava.launch.relaunch;

import moe.nea.modernjava.launch.util.PropertyNames;
import moe.nea.modernjava.launch.util.TextIoUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JavaScanner {

	public static final String JAVA_BINARY_PATH = "bin/java" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : "");
	public static final String JAVA_COMPILER_PATH = "bin/javac" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : "");

	public boolean isJavaHome(File potential) {
		if (new File(potential, JAVA_BINARY_PATH).exists()) {
			return true;
		}
		return false;
	}

	private List<LocalJavaVersion> versionList = new ArrayList<>();

	public void scanDirectory(File file) {
		scanDirectory(file, 1);
	}

	public void scanDirectory(File file, int depthLimit) {
		if (depthLimit < 0) return;
		if (isJavaHome(file)) {
			versionList.add(new LocalJavaVersion(file));
		} else {
			File[] files = file.listFiles();
			if (files == null) return;
			for (File listFile : files) {
				if (listFile.isDirectory()) {
					scanDirectory(listFile, depthLimit - 1);
				}
			}
		}
	}

	public void prettyPrint() {
		String s = "Fun fact these are the found Java Runtime Environments:\n";
		for (LocalJavaVersion localJavaVersion : versionList) {
			s += localJavaVersion.prettyPrint();
		}
		System.out.println(s);
	}

	public void scanDefaultPaths() {
		File home = new File(System.getProperty("user.home"));
		scanDirectory(new File(home, ".sdkman/candidates/java"));
		scanDirectory(new File(home, ".jdks"));
		scanDirectory(new File("/usr"), 0);
		String[] paths = System.getProperty(PropertyNames.JAVA_SCAN_PATH, "").split(File.pathSeparator);
		for (String path : paths) {
			if (!path.isEmpty()) {
				scanDirectory(new File(path).getParentFile(), 3);
			}
		}
	}

	public LocalJavaVersion findCandidate() {
		LocalJavaVersion bestFit = null;
		for (LocalJavaVersion localJavaVersion : versionList) {
			if (localJavaVersion.isJdk() && localJavaVersion.getMajorVersion() == 16) {
				bestFit = localJavaVersion;
			}
		}
		return bestFit;
	}

	public static class LocalJavaVersion {
		private final File javaHome;
		private String versionString;

		public LocalJavaVersion(File javaHome) {
			this.javaHome = javaHome;
		}

		public File getJavaHome() {
			return javaHome;
		}

		public File getJavaBinary() {
			return new File(javaHome, JAVA_BINARY_PATH);
		}

		public boolean isJdk() {
			return new File(javaHome, JAVA_COMPILER_PATH).exists();
		}

		public String getVersionString() {
			if (versionString == null) {
				ProcessBuilder processBuilder = new ProcessBuilder();
				processBuilder.command(getJavaBinary().getAbsolutePath(), "-version");
				processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
				processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
				processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);
				try {
					Process process = processBuilder.start();
					process.waitFor();
					versionString = IOUtils.toString(process.getInputStream()) + IOUtils.toString(process.getErrorStream());
				} catch (Exception e) {
					e.printStackTrace();
					versionString = "<invalid>";
				}
			}
			return versionString;
		}

		public String prettyPrint() {
			return javaHome.getAbsolutePath() + ":\n"
					+ "\tJava Binary: " + getJavaBinary().getAbsolutePath() + "\n"
					+ "\tMajor Version: " + getMajorVersion() + "\n"
					+ "\tFull Version: " + getVersion() + "\n"
					+ "\tIs Jdk: " + isJdk() + "\n"
					;
		}

		public String getVersion() {
			String v = getVersionString();
			String[] s = v.split("\"");
			if (s.length < 2) return null;
			return s[1];
		}

		public int getMajorVersion() {
			try {
				return Integer.parseInt(getVersion().split("\\.")[0]);
			} catch (Exception e) {
				return -1;
			}
		}
	}
}
