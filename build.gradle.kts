import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.gradle.node.npm.task.NpmTask

plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.spotbugs) apply false
    alias(libs.plugins.open.rewrite) apply false
    alias(libs.plugins.node)
    alias(libs.plugins.gradle.versions)
    idea
}

node {
    download.set(true)
    version.set("26.1.0")
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    checkForGradleUpdate = false
    revision = "release"
    rejectVersionIf {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { candidate.version.uppercase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(candidate.version)
        val candidateIsNonStable = !isStable
        
        val currentStableKeyword = listOf("RELEASE", "FINAL", "GA").any { currentVersion.uppercase().contains(it) }
        val currentIsStable = currentStableKeyword || regex.matches(currentVersion)
        val currentIsNonStable = !currentIsStable
        
        candidateIsNonStable && !currentIsNonStable
    }
}

allprojects {
    group = "sgc"
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
    plugins.withId("java") {
        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }
}

// Delegation tasks for convenience
tasks.register<NpmTask>("installRoot") {
    group = "setup"
    description = "Instala as dependências do root (npm install)"
    npmCommand.set(listOf("install"))
    inputs.file("package.json")
    inputs.file("package-lock.json")
    outputs.dir("node_modules")
}

tasks.register<NpmTask>("incrementVersion") {
    group = "versioning"
    description = "Incrementa a versão global usando release-it"
    dependsOn("installRoot")
    npmCommand.set(listOf("run", "release", "--", "--ci", "--patch"))
}

tasks.register("installFrontend") {
    group = "setup"
    description = "Instala as dependências do frontend (NPM)"
    dependsOn(":frontend:install")
}

tasks.register("buildFrontend") {
    group = "build"
    description = "Gera o build de produção do frontend (Vite)"
    dependsOn(":frontend:build")
}

tasks.register<Delete>("cleanFrontend") {
    group = "build"
    description = "Limpa o build do frontend"
    dependsOn(":frontend:clean")
}

tasks.register("frontendQualityCheck") {
    group = "quality"
    description = "Executa verificações de qualidade do frontend"
    dependsOn(":frontend:quality")
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

tasks.register("qualityCheckFast") {
    group = "quality"
    description = "Executa verificações de qualidade rápidas (testes + cobertura) para frontend e backend"
    dependsOn("backendQualityCheckFast", ":frontend:test")
}
