package moe.nea.modernjava.launch.util;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

public class ClassLoaderManipulations {
    /**
     * Adds a File to the parent class loader of the launch class loader. Necessary if you want to/have to use
     * {@link LaunchClassLoader#addClassLoaderExclusion(String)}.
     */
    public static void addToParentClassLoader(File file) {
        try {
            addToParentClassLoader(file.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a URL to the parent class loader of the launch class loader. Necessary if you want to/have to use
     * {@link LaunchClassLoader#addClassLoaderExclusion(String)}.
     */
    public static void addToParentClassLoader(URL file) {
        try {
            Launch.classLoader.addURL(file);
            ClassLoader parentClassLoader = Launch.classLoader.getClass().getClassLoader();
            Method addUrl = parentClassLoader.getClass().getDeclaredMethod("addURL", URL.class);
            addUrl.setAccessible(true);
            addUrl.invoke(parentClassLoader, file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
