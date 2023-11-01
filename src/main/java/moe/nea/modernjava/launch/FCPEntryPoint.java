package moe.nea.modernjava.launch;

import moe.nea.modernjava.launch.util.ClassLoaderManipulations;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.net.URI;
import java.util.List;
import java.util.Map;


@IFMLLoadingPlugin.Name("ModernJavaRelauncher")
public class FCPEntryPoint implements IFMLLoadingPlugin {
    static URI fileUri;

    static {
        try {
            fileUri = new URI(FCPEntryPoint.class.getProtectionDomain().getCodeSource().getLocation().toString().split("!")[0]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (System.getProperty("modernjava.hasrelaunched") == null) {
            try {
                Class.forName("moe.nea.modernjava.launch.FCPRelauncher").getMethod("relaunch").invoke(null);
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
            tweakClasses.add(FCPMixinAwareTweaker.class.getName());
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
