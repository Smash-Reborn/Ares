plugins {
    idea
    java
    alias(libs.plugins.essential.loom)
    alias(libs.plugins.architectury.pack200)
    alias(libs.plugins.shadow)
}

val baseGroup: String by project
val mcVersion: String by project
val modid: String by project
val mixinGroup = "$baseGroup.mixin"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.log4j.api)
    compileOnly(libs.bundles.common.compile)
    implementation(libs.bundles.common.implementation)

    minecraft(libs.minecraft)
    mappings(libs.mcp.mappings)
    forge(libs.forge)

    implementation(project(":core"))

    shadowImpl(libs.bundles.common.shadow)
    shadowImpl(libs.mixin.runtime) {
        isTransitive = false
    }
    annotationProcessor(libs.mixin.processor)

    runtimeOnly(libs.devauth)
}

loom {
    launchConfigs {
        "client" {
            property("mixin.debug", "true")
            arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
        }
    }

    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        mixinConfig("mixins.$modid.json")
    }

    @Suppress("UnstableApiUsage")
    mixin {
        defaultRefmapName.set("mixins.$modid.refmap.json")
    }

    runConfigs {
        named("client") {
            if (System.getProperty("os.name").lowercase().contains("mac")) {
                vmArgs.remove("-XstartOnFirstThread")
            }
        }
        remove(getByName("server"))
    }
}

tasks.compileJava {
    dependsOn(tasks.processResources)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.annotationProcessorPath = configurations.annotationProcessor.get()
}

tasks.withType<Jar>().configureEach {
    archiveBaseName.set(modid)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest.attributes(
        mapOf(
            "FMLCorePluginContainsFMLMod" to "true",
            "ForceLoadAsMod" to "true",
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "MixinConfigs" to "mixins.$modid.json",
        ),
    )
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mcversion", mcVersion)
    inputs.property("modid", modid)
    inputs.property("mixinGroup", mixinGroup)

    filesMatching(listOf("mcmod.info", "mixins.$modid.json")) {
        expand(
            mapOf(
                "version" to project.version,
                "mcversion" to mcVersion,
                "modid" to modid,
                "mixinGroup" to mixinGroup,
            ),
        )
    }
}

tasks.named<Jar>("jar") {
    archiveClassifier.set("without-deps")
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
}

tasks.shadowJar {
    archiveClassifier.set("all-dev")
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
    from(project(":core").sourceSets.main.get().output)
    configurations = listOf(shadowImpl)
    exclude("META-INF/versions/**")
    relocate("com.google.gson", "$baseGroup.deps.gson")
    relocate("it.unimi.dsi.fastutil", "$baseGroup.deps.fastutil")
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveBaseName.set(modid)
    archiveClassifier.set("")
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
}

tasks.assemble {
    dependsOn(tasks.named("remapJar"))
}