plugins {
    idea
    java
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly(libs.bundles.common.compile)
    implementation(libs.bundles.common.implementation)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}