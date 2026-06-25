import java.net.URL

plugins {
    idea
    java
    alias(libs.plugins.essential.loom)
    alias(libs.plugins.architectury.pack200)
    alias(libs.plugins.shadow)
}

val baseGroup: String by project
val modid: String by project

fun parseHotfix(version: String): Int {
    val parts = version.split(".")
    return if (parts.size >= 3) parts[2].toIntOrNull() ?: 0 else 0
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

repositories {
    mavenCentral()
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

val labymodApiJar = layout.projectDirectory.file("libs/lm_api_mc1.8.9.jar")

dependencies {
    // Declared here so IDE classpath sync resolves compile-only libs.
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly(libs.bundles.common.compile)
    implementation(libs.bundles.common.implementation)

    minecraft(libs.minecraft)
    mappings(libs.mcp.mappings)
    forge(libs.forge)

    implementation(project(":core"))
    compileOnly(files(labymodApiJar))

    shadowImpl(libs.bundles.common.shadow)
}

tasks.register("downloadLabyModApi") {
    val output = labymodApiJar.asFile
    outputs.file(output)

    doLast {
        if (!output.parentFile.exists()) {
            output.parentFile.mkdirs()
        }
        if (!output.exists()) {
            URL("https://dl.labymod.net/latest/api/files/lm_api_mc1.8.9.jar").openStream().use { input ->
                output.outputStream().use { outputStream ->
                    input.copyTo(outputStream)
                }
            }
        }
    }
}

tasks.compileJava {
    dependsOn("downloadLabyModApi")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("hotfix", parseHotfix(project.version.toString()))

    filesMatching("addon.json") {
        expand(
            mapOf(
                "version" to project.version,
                "hotfix" to parseHotfix(project.version.toString()),
            ),
        )
    }
}

tasks.register<Jar>("buildLabyAddonDev") {
    archiveBaseName.set(modid)
    archiveClassifier.set("labymod-dev")
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))

    dependsOn(":core:classes", "classes")
    from(project(":core").sourceSets.main.get().output)
    from(sourceSets.main.get().output)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.shadowJar {
    dependsOn("buildLabyAddonDev")
    archiveBaseName.set(modid)
    archiveClassifier.set("labymod-all-dev")
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
    from(tasks.named<Jar>("buildLabyAddonDev").flatMap { it.archiveFile })
    configurations = listOf(shadowImpl)
    exclude("META-INF/versions/**")
    relocate("com.google.gson", "$baseGroup.deps.gson")
    relocate("it.unimi.dsi.fastutil", "$baseGroup.deps.fastutil")
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveBaseName.set(modid)
    archiveClassifier.set("labymod")
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
}

tasks.register("buildLabyAddon") {
    dependsOn(tasks.named("remapJar"))
    group = "build"
    description = "Builds the LabyMod addon JAR."
}

tasks.assemble {
    dependsOn("buildLabyAddon")
}