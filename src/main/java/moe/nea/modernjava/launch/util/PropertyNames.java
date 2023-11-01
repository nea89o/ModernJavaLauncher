package moe.nea.modernjava.launch.util;

public class PropertyNames {
    /**
     * Property set to indicate whether the java process was launched on the new java version.
     */
    public static final String HAS_RELAUNCHED = "modernjava.hasrelaunched";
    /**
     * Classpath to load after reloading.
     */
    public static final String RELAUNCH_CLASSPATH = "modernjava.relaunchclasspath";
    /**
     * Starts a debugger on the given port if present.
     */
    public static final String DEBUG_PORT = "modernjava.debugport";

    /**
     * Scans the given directories for java binaries.
     */
    public static final String JAVA_SCAN_PATH = "modernjava.scanpath";
}
