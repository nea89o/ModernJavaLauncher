package moe.nea.modernjava.launch.relaunch;

import moe.nea.modernjava.launch.FCPEntryPoint;
import moe.nea.modernjava.launch.util.PropertyNames;
import moe.nea.modernjava.launch.util.TextIoUtils;
import moe.nea.modernjava.launch.util.WellKnownBlackboard;
import net.minecraftforge.fml.common.launcher.FMLTweaker;
import net.minecraftforge.fml.nea.moe.modernjava.IAMFML;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class FCPRelauncher {

	/**
	 * @return the original arguments, as passed to the main method.
	 */
	public static List<String> getOriginalArguments() {
		List<String> originalArgs = new ArrayList<>();

		// Provided by FML
		// This is highly processed so there might be some arguments that become lost, but almost everything should be in here.
		// Namely non -- arguments get lost. I don't know any of these arguments that the vanilla launcher uses, so it should be fine?
		// Also, some tweakers are missing. But we can fix this.
		Map<String, String> launchArgs = WellKnownBlackboard.launchArgs();
		if ("UnknownFMLProfile".equals(launchArgs.get("--version"))) {
			launchArgs.remove("--version");
		}
		for (Map.Entry<String, String> argument : launchArgs.entrySet()) {
			originalArgs.add(argument.getKey());
			originalArgs.add(argument.getValue());
		}


		originalArgs.add("--tweakClass");
		originalArgs.add(FMLTweaker.class.getName());
		System.out.println("Reconstructed original minecraft arguments: " + originalArgs);
		return originalArgs;
	}

	public static File findJavaLauncher() {
		JavaScanner javaScanner = new JavaScanner();
		javaScanner.scanDefaultPaths();
		javaScanner.prettyPrint();
		JavaScanner.LocalJavaVersion candidate = javaScanner.findCandidate();
		if (candidate == null) {
			System.err.println("Looks like we couldn't find a java candidate. Either install one, or if you have one" +
					" and we just cannot find it, specify -D" + PropertyNames.JAVA_SCAN_PATH + "=<java home here>." +
					" We need a Java 16 JDK. Exiting now.");
			IAMFML.shutdown(1);
			throw new RuntimeException();
		}
		System.out.println("Choosing Java Candidate:\n" + candidate.prettyPrint());
		return candidate.getJavaBinary();
	}

	public static File copyResource(String name, String resourcePath) {
		try {
			File file = File.createTempFile(name, ".jar");
			try (InputStream is = FCPRelauncher.class.getResourceAsStream(resourcePath);
			     OutputStream os = Files.newOutputStream(file.toPath())) {
				assert is != null;
				IOUtils.copy(is, os);
			}
			return file;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static File findAgentJar() {
		return copyResource("mjr-agent", "/agent/agent.jar");
	}

	public static void relaunch() {

		List<String> originalArgs = getOriginalArguments();

		File modernJavaPath = findJavaLauncher();
		System.out.println("Located modern minecraft at: " + modernJavaPath);

		File agentFile = findAgentJar();
		System.out.println("Located agent jar at: " + agentFile);


		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.inheritIO();
		processBuilder.directory(null);


		List<String> moduleOpens = new ArrayList<>();
		moduleOpens.add("java.base/java.util=ALL-UNNAMED");
		moduleOpens.add("java.base/jdk.internal.loader=ALL-UNNAMED");
		moduleOpens.add("java.base/java.lang.reflect=ALL-UNNAMED");


		List<String> fullCommandLine = new ArrayList<>();
		fullCommandLine.add(modernJavaPath.getAbsolutePath());
		fullCommandLine.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
		fullCommandLine.add("-D" + PropertyNames.HAS_RELAUNCHED + "=true");
		fullCommandLine.add("-D" + PropertyNames.RELAUNCH_CLASSPATH + "=" + createClassPath(agentFile));
		fullCommandLine.add("--illegal-access=permit");
		for (String open : moduleOpens) {
			fullCommandLine.add("--add-opens");
			fullCommandLine.add(open);
		}
		if (System.getProperty(PropertyNames.DEBUG_PORT) != null)
			fullCommandLine.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:" + System.getProperty(PropertyNames.DEBUG_PORT));
		fullCommandLine.add("-Xbootclasspath/a:" + agentFile.getAbsolutePath());
		fullCommandLine.add("-Xbootclasspath/a:" + extractExtraDependencies().stream().map(File::getAbsolutePath).collect(Collectors.joining(File.pathSeparator)));
		fullCommandLine.add("-javaagent:" + agentFile.getAbsolutePath());
		fullCommandLine.add("--add-modules=ALL-MODULE-PATH,ALL-SYSTEM,ALL-DEFAULT,java.sql");
		fullCommandLine.add("moe.nea.modernjava.agent.RelaunchEntryPoint");
		fullCommandLine.addAll(originalArgs);

		System.out.println("Full relaunch commandline: " + fullCommandLine);


		processBuilder.command(fullCommandLine);
		int exitCode;
		try {
			try {
				Process process = processBuilder.start();
				exitCode = process.waitFor();
			} finally {
				TextIoUtils.flushStdIO();
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("Failed to relaunch with old java version", e);
		}


		System.out.println("Exiting outer relaunch layer");
		IAMFML.shutdown(exitCode);
	}

	private static String createClassPath(File agentFile) {
		List<String> classPath = new ArrayList<>();
		classPath.addAll(Arrays.asList(ManagementFactory.getRuntimeMXBean().getClassPath().split(File.pathSeparator)));
		classPath.removeIf(it -> it.matches("[/\\\\]asm-[^/\\\\]+-5\\.[^/\\\\]+\\.jar"));
		classPath.add(0, agentFile.getAbsolutePath());
		return String.join(File.pathSeparator, classPath);
	}

	public static List<File> extractExtraDependencies() {
		try (ZipFile zipFile = new ZipFile(getCurrentJarFile())) {
			List<File> extractedResources = new ArrayList<>();
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (!entry.getName().startsWith("deps/")) continue;
				if (!entry.getName().endsWith(".jar")) continue;
				String[] split = entry.getName().split("/");
				extractedResources.add(copyResource(split[split.length - 1].split("\\.")[0], "/" + entry.getName()));
			}
			return extractedResources;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static File getCurrentJarFile() {
		return new File(FCPEntryPoint.fileUri);
	}

}
