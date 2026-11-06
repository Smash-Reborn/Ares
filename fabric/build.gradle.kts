plugins {
    idea
    java
    alias(libs.plugins.fabric.loom)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    withSourcesJar()
}

// Settings screen, command, tab overlay, and the Forge tick/mod classes are still 1.8 sources.
sourceSets.main {
    java {
        exclude("sh/smsh/ares/forge/AresMod.java")
        exclude("sh/smsh/ares/forge/ForgeClientTickHandler.java")
        exclude("sh/smsh/ares/forge/command/**")
        exclude("sh/smsh/ares/forge/gui/**")
        exclude("sh/smsh/ares/mixin/MixinGuiPlayerTabOverlay.java")
    }
}

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)

    implementation(project(":core"))
    include(project(":core"))
}

tasks.processResources {
    val version = project.version
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to version))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.jar {
    archiveBaseName.set("AresMod")
}
