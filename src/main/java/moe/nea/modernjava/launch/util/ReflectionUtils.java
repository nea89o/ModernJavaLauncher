package moe.nea.modernjava.launch.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class ReflectionUtils {
    private static Unsafe unsafe;

    static {
        try {
            final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void makeFieldWritable(Field f) {
        String s = "Doing nothing. We will use unsafe to set the value instead, if possible";
    }

    public static void doFieldWrite(Field field, Object object) throws IllegalAccessException {
        if (unsafe == null) {
            field.set(null, object);
        } else {
            Object o = unsafe.staticFieldBase(field);
            long l = unsafe.staticFieldOffset(field);
            unsafe.putObject(o, l, object);
        }
    }
}
