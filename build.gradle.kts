plugins {
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}

apply(plugin = "base")

allprojects {
    group = "sgc"
    version = "1.0.0"
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

tasks.register<Exec>("installFrontend") {
    workingDir = file("frontend")
    commandLine = if (System.getProperty("os.name").lowercase().contains("win")) listOf(
        "cmd",
        "/c",
        "npm",
        "install"
    ) else listOf("npm", "install")
}

tasks.register<Exec>("buildFrontend") {
    dependsOn("installFrontend")
    workingDir = file("frontend")
    commandLine = if (System.getProperty("os.name").lowercase().contains("win")) listOf(
        "cmd",
        "/c",
        "npm",
        "run",
        "build"
    ) else listOf("npm", "run", "build")
}

tasks.register<Copy>("copyFrontend") {
    dependsOn("buildFrontend")
    from("frontend/dist")
    into("backend/src/main/resources/static")
}

tasks.register<Delete>("cleanFrontend") {
    delete("frontend/dist", "frontend/node_modules")
}

tasks.register<Exec>("frontendQualityCheck") {
    group = "quality"
    description = "Runs frontend quality checks (tests, lint, typecheck)"
    dependsOn("installFrontend")
    workingDir = file("frontend")
    commandLine = if (System.getProperty("os.name").lowercase().contains("win")) listOf(
        "cmd",
        "/c",
        "npm",
        "run",
        "quality:all"
    ) else listOf("npm", "run", "quality:all")

    isIgnoreExitValue = true
}

tasks.register("backendQualityCheck") {
    group = "quality"
    description = "Runs backend quality checks"
    dependsOn(":backend:qualityCheck")
}

tasks.register("backendQualityCheckFast") {
    group = "quality"
    description = "Runs backend quality checks (fast)"
    dependsOn(":backend:qualityCheckFast")
}

tasks.register("qualityCheckAll") {
    group = "quality"
    description = "Runs all quality checks for both frontend and backend"
    dependsOn("backendQualityCheck", "frontendQualityCheck")
}

tasks.register<Exec>("qualityCheckFast") {
    group = "quality"
    description = "Runs fast quality checks (tests + coverage) for both frontend and backend"
    dependsOn("backendQualityCheckFast")

    workingDir = file("frontend")
    val isWindows = System.getProperty("os.name").lowercase().contains("win")
    commandLine =
        if (isWindows) listOf("cmd", "/c", "npm", "run", "quality:test")
        else listOf("npm", "run", "quality:test")

    isIgnoreExitValue = true
}