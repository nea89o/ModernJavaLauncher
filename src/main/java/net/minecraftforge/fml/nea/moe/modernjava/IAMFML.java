package net.minecraftforge.fml.nea.moe.modernjava;

import net.minecraftforge.fml.relauncher.FMLSecurityManager;

/**
 * Class in a package starting with {@code net.minecraftforge.fml} in order to easily bypass the {@link FMLSecurityManager}
 * which disallows calling {@link System#exit(int)}.
 */
public class IAMFML {

    /**
     * Calls {@link System#exit(int)}
     */
    public static void shutdown(int code) {
        System.exit(code);
    }
}
