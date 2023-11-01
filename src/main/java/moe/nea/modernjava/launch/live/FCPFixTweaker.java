package moe.nea.modernjava.launch.live;

import moe.nea.modernjava.launch.transform.TransObjectHolderRef;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.List;

/**
 * Tweaker class to inject {@link TransObjectHolderRef}
 */
public class FCPFixTweaker implements ITweaker {
    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        classLoader.addClassLoaderExclusion("moe.nea.modernjava.");
        classLoader.addClassLoaderExclusion("kotlin.");
        classLoader.registerTransformer("moe.nea.modernjava.launch.transform.TransObjectHolderRef");
    }

    @Override
    public String getLaunchTarget() {
        return null;
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}
