package moe.nea.modernjava.launch;

import moe.nea.modernjava.launch.live.FCPFixTweaker;
import moe.nea.modernjava.launch.util.ClassLoaderManipulations;
import moe.nea.modernjava.launch.util.WellKnownBlackboard;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static moe.nea.modernjava.launch.util.PropertyNames.HAS_RELAUNCHED;

/**
 * Global entrypoint for both the FML part of the relaunched (live) and the relaunching (relaunch) runs. Execution
 * begins during static initialization to be as early as possible. There aren't any security implications, but especially
 * on Windows it can be problematic if two processes try to open up the same file, so we try to avoid these conflicts.
 * Also, it's just a faster launch.
 */
@IFMLLoadingPlugin.Name("ModernJavaRelauncher")
public class FCPEntryPoint implements IFMLLoadingPlugin {
    public static URI fileUri;

    static {
        try {
            fileUri = new URI(FCPEntryPoint.class.getProtectionDomain().getCodeSource().getLocation().toString().split("!")[0]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (System.getProperty(HAS_RELAUNCHED) == null) {
            try {
                Class.forName("moe.nea.modernjava.launch.relaunch.FCPRelauncher")
                        .getMethod("relaunch").invoke(null);
            } catch (Throwable t) {
                System.out.println("Failed to relaunch");
                t.printStackTrace();
            }
        } else {
            try {
                ClassLoaderManipulations.addToParentClassLoader(fileUri.toURL());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            List<String> tweakClasses = WellKnownBlackboard.tweakerNames();
            tweakClasses.add(FCPFixTweaker.class.getName());
        }
    }


    @Override
    public String[] getASMTransformerClass() {
        return new String[]{
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> map) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
