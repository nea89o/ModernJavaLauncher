package moe.nea.modernjava.launch;

import net.minecraft.launchwrapper.Launch;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class WellKnownBlackboard {
    public static List<String> tweakerNames() {
        return (List<String>) Launch.blackboard.get("TweakClasses");
    }

    public static Map<String, String> launchArgs() {
        return (Map<String, String>) Launch.blackboard.get("launchArgs");
    }
}
