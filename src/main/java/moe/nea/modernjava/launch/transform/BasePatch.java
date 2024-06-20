package moe.nea.modernjava.launch.transform;

import net.minecraft.launchwrapper.IClassTransformer;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class BasePatch implements IClassTransformer {
	protected abstract String getTargetedName();

	protected Type getTargetedType() {
		return getClassType(getTargetedName());
	}

	protected abstract ClassNode transform(ClassNode classNode);

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (basicClass == null) return null;
		if (!getTargetedName().equals(name)) return basicClass;
		ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(basicClass);
		reader.accept(node, 0);
		ClassNode processedNode = transform(node);
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		processedNode.accept(writer);
		return writer.toByteArray();
	}

	protected Iterable<AbstractInsnNode> iterableInstructions(InsnList insns) {
		return new Iterable<AbstractInsnNode>() {
			@NotNull
			@Override
			public Iterator<AbstractInsnNode> iterator() {
				return insns.iterator();
			}
		};
	}

	private Printer printer = new Textifier();
	private TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(printer);

	protected String debugInsn(AbstractInsnNode insnNode) {
		insnNode.accept(traceMethodVisitor);
		StringWriter sw = new StringWriter();
		printer.print(new PrintWriter(sw));
		printer.getText().clear();
		return sw.toString();
	}

	protected List<String> debugInsnList(InsnList list) {
		List<String> strings = new ArrayList<>();
		for (AbstractInsnNode node : iterableInstructions(list)) {
			strings.add(debugInsn(node));
		}
		return strings;
	}

	protected Type getClassType(String plainName) {
		return Type.getObjectType(plainName.replace('.', '/'));
	}

	protected static MethodNode findMethod(ClassNode node, String name, Type desc) {
		System.out.println("Searching for " + name + " " + desc.getDescriptor());
		for (MethodNode method : node.methods) {
			System.out.println(" - Candidate: " + method.name + " " + method.desc);
			if (name.equals(method.name) && desc.getDescriptor().equals(method.desc)) {
				System.out.println("Found method");
				return method;
			}
		}
		System.out.println("Could not find method.");
		return null;
	}

}
