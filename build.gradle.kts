plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("jvm") version "1.9.10"
}

group = "com.example.archloomtemplate"
version = "1.0.0"

// Toolchains:
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

val fcpEntryPoint = "moe.nea.modernjava.launch.FCPEntryPoint"

// Minecraft configuration:
loom {
    log4jConfigs.from(file("log4j2.xml"))
    launchConfigs {
        "client" {
            property("fml.coreMods.load", fcpEntryPoint)
        }
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io") {
            content {
                includeGroupByRegex("(com|io)\\.github\\..+")
            }
        }
        maven("https://maven.architectury.dev/")
        maven("https://repository.ow2.org/nexus/content/repositories/releases/")
    }
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

val agentDeps: Configuration by configurations.creating

val shadowOnly: Configuration by configurations.creating {
    attributes {
        this.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 16)
    }
}


dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    shadowOnly("javax.annotation:javax.annotation-api:1.3.2")
    shadowImpl("com.github.Skytils:AsmHelper:91ecc2bd9c")
    shadowImpl(enforcedPlatform(kotlin("bom")))
    shadowImpl(kotlin("stdlib"))
    agentDeps(project(":agent",configuration = "agentShadow"))
}

// Tasks:

val doubleWrappedJar by tasks.creating(Zip::class) {
    archiveFileName.set("agent.jar")
    destinationDirectory.set(project.layout.buildDirectory.dir("wrapper"))
    from(agentDeps)
    into("agent")
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.withType(Jar::class) {
    archiveBaseName.set("ModernJavaLauncher")
    manifest.attributes.run {
        this["FMLCorePlugin"] = fcpEntryPoint
        this["ModSide"] = "BOTH"
        this["ModType"] = "FML"
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
    }
}


val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
}

tasks.jar {
    archiveClassifier.set("thin-dev")
}

tasks.shadowJar {
    archiveClassifier.set("all-dev")
    relocate("dev.falsehonesty.asmhelper", "moe.nea.modernjava.dep.asmhelper")
    configurations = listOf(shadowImpl, shadowOnly)
    from(doubleWrappedJar)
}

tasks.assemble.get().dependsOn(tasks.remapJar)

