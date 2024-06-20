package moe.nea.modernjava.launch.util;

import moe.nea.modernjava.launch.transform.PatchObjectHolderRef;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * A companion to my transformations from {@link PatchObjectHolderRef} to avoid
 * having to write all of this out in bytecode.
 */
public class ObjectHolderRefCompanion {
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

	/**
	 * A noop to have a jump target for the reflection factories.
	 */
	public static void makeFieldWritable(Field f) {
		String s = "Doing nothing. We will use unsafe to set the value instead, if possible";
	}

	/**
	 * Write a value to a static final field.
	 */
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
