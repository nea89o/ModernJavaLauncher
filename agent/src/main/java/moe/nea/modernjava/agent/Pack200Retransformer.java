package moe.nea.modernjava.agent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

/**
 * Transforms all classes such that they use dev.architectury.pack200.java instead of java.util.jar for Pack200 packing
 * and unpacking. We cannot simply transform the jar on the classpath, since defining a package in the java directory
 * is impossible in most versions of java, even from the boot class path. In theory this could be a custom class loader,
 * but the java agent is far more reliable.
 */
public class Pack200Retransformer implements ClassFileTransformer {
	// relocate("dev.architectury.pack200.java", "java.util.jar")
	List<String> classes = Arrays.asList("AdaptiveCoding", "Attribute", "BandStructure", "ClassReader", "ClassWriter", "Code", "Coding", "CodingChooser", "CodingMethod", "ConstantPool", "Constants", "Driver", "DriverResource", "DriverResource_ja", "DriverResource_zh_CN", "FixedList", "Fixups", "Histogram", "Instruction", "NativeUnpack", "Pack200", "Pack200Adapter", "Pack200Plugin", "Package", "PackageReader", "PackageWriter", "PackerImpl", "PopulationCoding", "PropMap", "TLGlobals", "UnpackerImpl", "Utils");
	String architecturyPackage = "dev/architectury/pack200/java/";
	String javaPackage = "java/util/jar/";

	public static void premain(String agentArgs, Instrumentation inst) {
		inst.addTransformer(new Pack200Retransformer());
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		if (className.startsWith("moe/nea/modernjava/agent")) return classfileBuffer;
		ClassReader reader = new ClassReader(classfileBuffer);
		ClassWriter writer = new ClassWriter(reader, 0);
		ClassVisitor visitor = writer;
		visitor = getRemapVisitor(visitor);
		visitor = getTypeVisitor(className, visitor);
		reader.accept(visitor, 0);
		return writer.toByteArray();
	}

	private ClassVisitor getRemapVisitor(ClassVisitor parent) {
		Remapper remapper = new Remapper() {
			@Override
			public String map(String internalName) {
				if (internalName.startsWith(javaPackage)) {
					for (String aClass : classes) {
						if (internalName.equals(javaPackage + aClass) || (internalName.startsWith(javaPackage + aClass + "$")))
							return internalName.replace(javaPackage, architecturyPackage);
					}
				}
				return internalName;
			}
		};
		return new ClassRemapper(parent, remapper);
	}

	private ClassVisitor getTypeVisitor(String className, ClassVisitor parent) {
		if (className.startsWith("org/objectweb/asm/")) return parent;
		return new ClassVisitor(Opcodes.ASM9, parent) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
				return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
					@Override
					public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
						boolean isType = owner.equals("org/objectweb$/asm/Type".replace("$",""));
						boolean isName = "getType".equals(name);
						boolean isDesc = "(Ljava/lang/String;)Lorg/objectweb/asm/Type;".equals(descriptor);
						if (isType && isName && isDesc) {
							owner = Type.getInternalName(LenientType.class);
						}
						super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

					}
				};
			}
		};
	}
}
