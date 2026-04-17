plugins {
    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openrewrite.rewrite") version "7.18.0" apply false
    id("base")
}

fun detectarWindows(): Boolean {
    val nomeSistemaOperacional: String = System.getProperty("os.name") ?: return false
    return nomeSistemaOperacional.lowercase().contains("win")
}

allprojects {
    group = "sgc"
    version = "1.0.0"
    repositories {
        mavenCentral {
            mavenContent {
                releasesOnly()
            }
        }
        maven {
            url = uri("https://repo.spring.io/milestone")
            mavenContent {
                releasesOnly()
            }
        }
        maven {
            url = uri("https://repo.spring.io/snapshot")
            mavenContent {
                snapshotsOnly()
            }
        }
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
    description = "Instalar frontend"
    workingDir = file("frontend")
    commandLine = if (detectarWindows()) listOf(
        "cmd",
        "/c",
        "npm",
        "install"
    ) else listOf("npm", "install")
}

tasks.register<Exec>("buildFrontend") {
    description = ""
    dependsOn("installFrontend")
    workingDir = file("frontend")
    commandLine = if (detectarWindows()) listOf(
        "cmd",
        "/c",
        "npm",
        "run",
        "build"
    ) else listOf("npm", "run", "build")
}

tasks.register<Copy>("copyFrontend") {
    description = "Copiar frontend para servidor"
    dependsOn("buildFrontend")
    from("frontend/dist")
    into("backend/src/main/resources/static")
}

tasks.register<Delete>("cleanFrontend") {
    description = "Limpar build de frontend"
    delete("frontend/dist", "frontend/node_modules")
}

tasks.register<Exec>("frontendQualityCheck") {
    group = "quality"
    description = "Executa verificações de qualidade do frontend (testes, lint, typecheck)"
    dependsOn("installFrontend")
    workingDir = file("frontend")
    commandLine = if (detectarWindows()) listOf(
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
    description = "Executa verificações de qualidade do backend"
    dependsOn(":backend:qualityCheck")
}

tasks.register("backendQualityCheckFast") {
    group = "quality"
    description = "Executa verificações de qualidade rápidas do backend"
    dependsOn(":backend:qualityCheckFast")
}

tasks.register("qualityCheckAll") {
    group = "quality"
    description = "Executa todas as verificações de qualidade (frontend e backend)"
    dependsOn("backendQualityCheck", "frontendQualityCheck")
}

tasks.register<Exec>("qualityCheckFast") {
    group = "quality"
    description = "Executa verificações de qualidade rápidas (testes + cobertura) para frontend e backend"
    dependsOn("backendQualityCheckFast")

    workingDir = file("frontend")
    val isWindows: Boolean = detectarWindows()
    commandLine =
        if (isWindows) listOf("cmd", "/c", "npm", "run", "quality:test")
        else listOf("npm", "run", "quality:test")

    isIgnoreExitValue = true
}
