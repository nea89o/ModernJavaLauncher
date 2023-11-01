package moe.nea.modernjava.agent;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * The main class for the new java runtime.
 */
public class RelaunchEntryPoint {
    /**
     * Logs things with a prefix to indicate it's coming from the new java runtime.
     */
    public static void log(String text) {
        System.out.println("[MODERNJAVA] " + text);

    }

    /**
     * Wrap {@link #setupLaunch(String[])} in a crash handler for logging.
     */
    public static void main(String[] args) {
        log("This is modern java relaunch talking!");
        try {
            setupLaunch(args);
        } catch (Throwable t) {
            log("Encountered exception during relaunching");
            t.printStackTrace();
        }
    }

    private static void setupLaunch(String[] args) throws Exception {

        // Check if old class loader can load java modules.
        log("Original SQLException: " + Thread.currentThread().getContextClassLoader().loadClass("java.sql.SQLException"));

        log("Setting up fake launch class loader");
        ClassLoader urlClassLoader = new URLClassLoader(retrieveClassPath(), Thread.currentThread().getContextClassLoader());

        log("Created fake url class loader");
        Thread.currentThread().setContextClassLoader(urlClassLoader);
        log("Handing off to Launch");

        // Check if new class loader can load java modules.
        log("LaunchClass ClassLoader SQLException: " + urlClassLoader.loadClass("java.sql.SQLException"));

        // Load launch wrapper and check launch class loader
        Class<?> launchClass = urlClassLoader.loadClass("net.minecraft.launchwrapper.Launch");
        log("LaunchClass: " + launchClass);
        log("LaunchClass ClassLoader: " + launchClass.getClassLoader());

        // Hand off to launch wrapper
        Method main = launchClass.getMethod("main", String[].class);
        main.invoke(null, new Object[]{args});
    }

    private static URL[] retrieveClassPath() throws MalformedURLException {
        String property = System.getProperty("modernjava.relaunchclasspath");
        String[] split = property.split(File.pathSeparator);
        URL[] urls = new URL[split.length];
        for (int i = 0; i < split.length; i++) {
            urls[i] = new File(split[i]).toURI().toURL();
        }
        log("Retrieved classpath: " + Arrays.toString(urls));
        return urls;
    }
}
