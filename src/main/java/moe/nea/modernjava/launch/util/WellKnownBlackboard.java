package moe.nea.modernjava.launch.util;

import net.minecraft.launchwrapper.Launch;

import java.util.List;
import java.util.Map;

/**
 * Contains references to the {@link Launch#blackboard black board} in one central spot to avoid misspelling.
 */
@SuppressWarnings("unchecked")
public class WellKnownBlackboard {
    /**
     * A list of tweaker class names yet to be executed. This does not include tweaker class names present in the current
     * round of tweaking.
     */
    public static List<String> tweakerNames() {
        return (List<String>) Launch.blackboard.get("TweakClasses");
    }

    /**
     * A map of arguments in the form of --prefixedKey to value.
     */
    public static Map<String, String> launchArgs() {
        return (Map<String, String>) Launch.blackboard.get("launchArgs");
    }
}
