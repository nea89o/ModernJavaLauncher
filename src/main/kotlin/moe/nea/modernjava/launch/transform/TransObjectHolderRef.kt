package moe.nea.modernjava.launch.transform

import dev.falsehonesty.asmhelper.BaseClassTransformer
import dev.falsehonesty.asmhelper.dsl.instructions.InsnListBuilder
import dev.falsehonesty.asmhelper.dsl.instructions.InvokeType
import dev.falsehonesty.asmhelper.dsl.modify
import dev.falsehonesty.asmhelper.dsl.overwrite
import moe.nea.modernjava.launch.util.ObjectHolderRefCompanion
import net.minecraft.launchwrapper.Launch
import net.minecraft.launchwrapper.LaunchClassLoader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode

/**
 * Transform [net.minecraftforge.fml.common.registry.ObjectHolderRef] such that it does not make references to outdated
 * Java Reflection Tools anymore
 */
class TransObjectHolderRef : BaseClassTransformer() {
    override fun makeTransformers() {
        /**
         * Redirect the makeWritable call to [ObjectHolderRefCompanion]
         */
        overwrite {
            className = "net.minecraftforge.fml.common.registry.ObjectHolderRef"
            methodName = "makeWritable"
            methodDesc = "(Ljava/lang/reflect/Field;)V"

            insnList {
                aload(0/* arg0 */)
                invoke(
                    InvokeType.STATIC,
                    "moe/nea/modernjava/launch/util/ObjectHolderRefCompanion",
                    "makeFieldWritable",
                    "(Ljava/lang/reflect/Field;)V"
                )
                methodReturn()
            }
        }
        /**
         * Redirect the reflection calls to write a value to a static field in apply to [ObjectHolderRefCompanion]
         */
        modify("net/minecraftforge/fml/common/registry/ObjectHolderRef") {
            var end: AbstractInsnNode? = null
            var start: AbstractInsnNode? = null
            var c = 0
            val m = findMethod("apply", "()V")
            for (instruction in m.instructions) {
                if (instruction is FieldInsnNode &&
                    instruction.opcode == Opcodes.GETSTATIC &&
                    instruction.name == "newFieldAccessor"
                ) {
                    start = instruction
                }
                if (instruction.opcode == Opcodes.INVOKEVIRTUAL && start != null) {
                    c++
                    if (c == 2) {
                        end = instruction.next
                        break
                    }
                }
            }
            end!!
            val trueStart = start!!
            m.instructions.insertBefore(trueStart, InsnListBuilder(m).apply {
                aload(0/* this */)
                getField("net/minecraftforge/fml/common/registry/ObjectHolderRef", "field", "Ljava/lang/reflect/Field;")
                aload(1 /*thing*/)
                invokeStatic(
                    "moe/nea/modernjava/launch/util/ObjectHolderRefCompanion",
                    "doFieldWrite",
                    "(Ljava/lang/reflect/Field;Ljava/lang/Object;)V"
                )
                methodReturn()
            }.insnList)
            m.maxLocals = 0
            var toRemove = start!!
            while (true) {
                val n = toRemove.next
                m.instructions.remove(toRemove)
                if (end == toRemove) {
                    break
                }
                toRemove = n
            }
        }
    }

    val supCalledSetup = BaseClassTransformer::class.java.getDeclaredField("calledSetup").also {
        it.isAccessible = true
    }

    fun mySetup() {
        myCalledSetup = true
        supCalledSetup.set(this, true)

        val classLoader: LaunchClassLoader = Launch.classLoader

        classLoader.addTransformerExclusion("kotlin.")
        classLoader.addTransformerExclusion("moe.nea.modernjava.dep.asmhelper.")
        classLoader.addTransformerExclusion(this.javaClass.name)

        setup(classLoader)

        makeTransformers()

    }

    var myCalledSetup = false
    override fun transform(name: String?, transformedName: String?, basicClass: ByteArray?): ByteArray? {
        if (!myCalledSetup) {
            mySetup()
        }

        return super.transform(name, transformedName, basicClass)
    }

}