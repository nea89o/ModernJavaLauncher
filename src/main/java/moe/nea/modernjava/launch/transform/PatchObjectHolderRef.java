package moe.nea.modernjava.launch.transform;


import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.reflect.Field;

public class PatchObjectHolderRef extends BasePatch {
	@Override
	protected String getTargetedName() {
		return "net.minecraftforge.fml.common.registry.ObjectHolderRef";
	}

	@Override
	protected ClassNode transform(ClassNode classNode) {
		patchFindWriteable(classNode);
		patchApply(classNode);
		return classNode;
	}

	private void patchApply(ClassNode classNode) {
		MethodNode apply = findMethod(
				classNode, "apply",
				Type.getMethodType(Type.VOID_TYPE)
		);
		assert apply != null;

		InsnList insns = apply.instructions;

		AbstractInsnNode start = null, end = null;
		int c = 0;
		for (AbstractInsnNode instruction : iterableInstructions(insns)) {
			if (instruction instanceof FieldInsnNode && instruction.getOpcode() == Opcodes.GETSTATIC && ((FieldInsnNode) instruction).name.equals("newFieldAccessor")) {
				start = instruction;
			}
			if (instruction.getOpcode() == Opcodes.INVOKEVIRTUAL && start != null) {
				c++;
				if (c == 2) {
					end = instruction.getNext();
					break;
				}
			}
		}
		AbstractInsnNode trueStart = start;
		{
			InsnList insertion = new InsnList();
			insertion.add(new VarInsnNode(Opcodes.ALOAD, 0));
			insertion.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraftforge/fml/common/registry/ObjectHolderRef", "field", "Ljava/lang/reflect/Field;"));
			insertion.add(new VarInsnNode(Opcodes.ALOAD, 1));
			insertion.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					"moe/nea/modernjava/launch/util/ObjectHolderRefCompanion",
					"doFieldWrite",
					"(Ljava/lang/reflect/Field;Ljava/lang/Object;)V",
					false));
			insertion.add(new InsnNode(Opcodes.RETURN));
			insns.insertBefore(trueStart, insertion);
		}
		apply.maxLocals = 0;
		AbstractInsnNode toRemove = start;
		while (true) {
			AbstractInsnNode next = toRemove.getNext();
			insns.remove(toRemove);
			if (end == toRemove)
				break;
			toRemove = next;
		}
	}

	Type companionType = getClassType("moe.nea.modernjava.launch.util.ObjectHolderRefCompanion");

	private void patchFindWriteable(ClassNode classNode) {
		MethodNode makeWriteable = findMethod(
				classNode, "makeWritable",
				Type.getMethodType(Type.VOID_TYPE, Type.getType(Field.class))
		);
		assert makeWriteable != null;
		makeWriteable.tryCatchBlocks.clear();
		InsnList insns = makeWriteable.instructions;
		insns.clear();
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insns.add(new MethodInsnNode(
				Opcodes.INVOKESTATIC,
				companionType.getInternalName(),
				"makeFieldWritable",
				Type.getMethodType(Type.VOID_TYPE, Type.getType(Field.class)).getDescriptor(),
				false
		));
		insns.add(new InsnNode(Opcodes.RETURN));
	}
}
