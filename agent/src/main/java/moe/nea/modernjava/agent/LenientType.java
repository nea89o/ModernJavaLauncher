package moe.nea.modernjava.agent;

import org.objectweb.asm.Type;

public class LenientType {
	/**
	 * {@link Type#getType(String)}, but implementing the old lenient behaviour.
	 * This deviates from the old behaviour in that it defaults to creating an object,
	 * instead of a method, but this is generally the desired behaviour.
	 */
	public static Type getType(String typeDescriptor) {
		char c = 0;
		if (!typeDescriptor.isEmpty()) {
			c = typeDescriptor.charAt(0);
		}
		switch (c) {
			case 'V':
			case 'Z':
			case 'C':
			case 'B':
			case 'S':
			case 'I':
			case 'F':
			case 'J':
			case 'D':
			case '[':
			case 'L':
			case '(':
				return Type.getType(typeDescriptor);
			default:
				return Type.getObjectType(typeDescriptor);
		}
	}
}
