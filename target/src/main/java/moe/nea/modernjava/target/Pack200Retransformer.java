package moe.nea.modernjava.target;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

public class Pack200Retransformer implements ClassFileTransformer {
    // relocate("dev.architectury.pack200.java", "java.util.jar")
    List<String> classes = Arrays.asList("AdaptiveCoding", "Attribute", "BandStructure", "ClassReader", "ClassWriter", "Code", "Coding", "CodingChooser", "CodingMethod", "ConstantPool", "Constants", "Driver", "DriverResource", "DriverResource_ja", "DriverResource_zh_CN", "FixedList", "Fixups", "Histogram", "Instruction", "NativeUnpack", "Pack200", "Pack200Adapter", "Pack200Plugin", "Package", "PackageReader", "PackageWriter", "PackerImpl", "PopulationCoding", "PropMap", "TLGlobals", "UnpackerImpl", "Utils");
    String architecturyPackage = "dev/architectury/pack200/java/";
    String javaPackage = "java/util/jar/";

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new Pack200Retransformer());
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader, 0);
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
        ClassVisitor visitor = new ClassRemapper(writer, remapper);
        reader.accept(visitor, 0);
        return writer.toByteArray();
    }
}
