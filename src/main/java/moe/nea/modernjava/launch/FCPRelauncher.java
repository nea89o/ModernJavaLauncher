package moe.nea.modernjava.launch;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.launcher.FMLTweaker;
import net.minecraftforge.fml.nea.moe.modernjava.IAMFML;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FCPRelauncher {
    public static void relaunch() {

        List<String> originalArgs = new ArrayList<>();

        // Provided by FML
        // This is highly processed so there might be some arguments that become lost, but almost everything should be in here.
        // Namely non -- arguments get lost. I don't know any of these arguments that the vanilla launcher uses, so it should be fine?
        // Also some tweakers are missing. But we can fix this.
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

        String modernJavaPath = "/home/nea/.sdkman/candidates/java/16.0.2-tem/bin/java";


        String thisJarFile = FCPEntryPoint.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        thisJarFile = "/home/nea/src/ModernJavaLauncher/target/build/libs/target.jar";
        System.out.println("Found modern java jar at: " + thisJarFile);

        System.out.println("Located modern minecraft at: " + modernJavaPath);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.inheritIO();
        processBuilder.directory(null);


        List<String> moduleOpens = new ArrayList<>();
        moduleOpens.add("java.base/java.util=ALL-UNNAMED");
        moduleOpens.add("java.base/jdk.internal.loader=ALL-UNNAMED");
        moduleOpens.add("java.base/java.lang.reflect=ALL-UNNAMED");


        List<String> fullCommandLine = new ArrayList<>();
        fullCommandLine.add(modernJavaPath);
        fullCommandLine.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
        fullCommandLine.add("-Dmodernjava.hasrelaunched=true");
        fullCommandLine.add("-Dmodernjava.relaunchclasspath=" + thisJarFile + File.pathSeparator + ManagementFactory.getRuntimeMXBean().getClassPath());
        fullCommandLine.add("--illegal-access=permit");
        for (String open : moduleOpens) {
            fullCommandLine.add("--add-opens");
            fullCommandLine.add(open);
        }
        fullCommandLine.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005");
        fullCommandLine.add("-javaagent:" + thisJarFile);
        fullCommandLine.add("--add-modules=ALL-MODULE-PATH,ALL-SYSTEM,ALL-DEFAULT,java.sql");
        fullCommandLine.add("-Xbootclasspath/a:" + thisJarFile);
        fullCommandLine.add("moe.nea.modernjava.target.RelaunchEntryPoint");
        fullCommandLine.addAll(originalArgs);

        System.out.println("Full relaunch commandline: " + fullCommandLine);


        processBuilder.command(fullCommandLine);
        int exitCode;
        try {
            try {
                Process process = processBuilder.start();
                exitCode = process.waitFor();
            } finally {
                try {
                    new FileOutputStream(FileDescriptor.out).flush();
                    new FileOutputStream(FileDescriptor.err).flush();
                } catch (IOException ignored) {
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to relaunch with old java version", e);
        }


        System.out.println("Exiting outer relaunch layer");
        IAMFML.shutdown(exitCode);
    }

}
