plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(11))
}


val shadowOnly: Configuration by configurations.creating {
}

dependencies {
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-commons:9.7")
    shadowOnly("dev.architectury:architectury-pack200:0.1.3")
    shadowOnly("org.apache.commons:commons-lang3:3.13.0")
}
tasks.withType(Jar::class) {
    manifest {
        attributes.apply {
            this["Premain-Class"] = "moe.nea.modernjava.agent.Pack200Retransformer"
        }
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    configurations = listOf(shadowOnly)
}
tasks.jar {
    archiveClassifier.set("thin")
}
tasks.assemble.get().dependsOn(tasks.shadowJar)

configurations.create("agentShadow")
artifacts {
    add("agentShadow", tasks.shadowJar)
}
