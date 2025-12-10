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

    val projectDir = layout.projectDirectory.asFile.absolutePath

    doLast {
        println("\n=== Frontend Quality Check Summary ===")
        println("Coverage Report: file://$projectDir/frontend/coverage/index.html")
    }
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

    val projectDir = layout.projectDirectory.asFile.absolutePath

    doLast {
        println("\n=== Comprehensive Quality Check Summary ===")
        println("Backend Reports:")
        println("  JaCoCo: file://$projectDir/backend/build/reports/jacoco/test/html/index.html")
        println("  SpotBugs: file://$projectDir/backend/build/reports/spotbugs/main.html")
        println("  Checkstyle: file://$projectDir/backend/build/reports/checkstyle/main.html")
        println("  PMD: file://$projectDir/backend/build/reports/pmd/main.html")
        println("Frontend Reports:")
        println("  Coverage: file://$projectDir/frontend/coverage/index.html")
    }
}

tasks.register<Exec>("qualityCheckFast") {
    group = "quality"
    description = "Runs fast quality checks (tests + coverage) for both frontend and backend"
    dependsOn("backendQualityCheckFast")

    workingDir = file("frontend")
    val isWindows = System.getProperty("os.name").lowercase().contains("win")
    commandLine =
        if (isWindows) listOf("cmd", "/c", "npm", "run", "quality:test") else listOf("npm", "run", "quality:test")

    isIgnoreExitValue = true
}