allprojects {
    group = property("baseGroup") as String
    version = property("version") as String
}

subprojects {
    repositories {
        mavenCentral()
    }

    // Lombok, FastUtil, Log4j API, JetBrains annotations, and Gson — every Java module.
    // afterEvaluate ensures Loom modules receive these once their plugin setup finishes.
    afterEvaluate {
        if (plugins.hasPlugin("java")) {
            dependencies.add("compileOnly", libs.lombok)
            dependencies.add("annotationProcessor", libs.lombok)
            dependencies.add("compileOnly", libs.bundles.common.compile)
            dependencies.add("implementation", libs.bundles.common.implementation)
        }
    }
}