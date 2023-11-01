plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(11))
}


val shadowOnly: Configuration by configurations.creating {
    attributes {
        this.attribute(org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 11)
    }
}
repositories {
    mavenCentral()
    maven("https://maven.architectury.dev/")
    maven("https://repository.ow2.org/nexus/content/repositories/releases/")
}

dependencies {
    shadowOnly(implementation("org.ow2.asm:asm:9.4")!!)
    shadowOnly(implementation("org.ow2.asm:asm-commons:9.4")!!)
    shadowOnly("dev.architectury:architectury-pack200:0.1.3")
}
tasks.withType(Jar::class) {
    manifest {
        attributes.apply {
            this["Premain-Class"] = "moe.nea.modernjava.target.Pack200Retransformer"
        }
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    configurations = listOf(shadowOnly)
    relocate("org.objectweb.asm", "moe.nea.modernjava.asm")
    // relocate("dev.architectury.pack200.java", "java.util.jar")
}
tasks.jar {
    archiveClassifier.set("thin")
}
tasks.assemble.get().dependsOn(tasks.shadowJar)