import org.apache.tools.ant.taskdefs.condition.Os
import com.github.gradle.node.pnpm.task.PnpmTask

plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.spotbugs) apply false
    alias(libs.plugins.open.rewrite) apply false
    alias(libs.plugins.node)
    java
    jacoco
    idea
}

node {
    download.set(true)
    version.set("22.22.0")
    pnpmVersion.set("10.33.4")
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
                languageVersion.set(JavaLanguageVersion.of(25))
            }
            sourceCompatibility = JavaVersion.VERSION_25
            targetCompatibility = JavaVersion.VERSION_25
        }
    }
}

// Delegation tasks for convenience
tasks.register<PnpmTask>("installRoot") {
    group = "setup"
    description = "Instala as dependências do root (pnpm install)"
    pnpmCommand.set(listOf("install", "--frozen-lockfile"))
    inputs.file("package.json")
    inputs.file("pnpm-lock.yaml")
    outputs.dir("node_modules")
}

tasks.register<PnpmTask>("incrementVersion") {
    group = "versioning"
    description = "Incrementa a versão global usando release-it"
    dependsOn("installRoot")
    pnpmCommand.set(listOf("run", "release", "--", "--ci", "--patch"))
}

tasks.register("installFrontend") {
    group = "setup"
    description = "Instala as dependências do frontend (NPM)"
    dependsOn(":frontend:install")
}

tasks.register("buildFrontend") {
    group = "build"
    description = "Gera o build de produção do frontend (Vite)"
    dependsOn(":frontend:buildVue")
}

tasks.register<Copy>("copyFrontend") {
    group = "build"
    description = "Copia o frontend gerado para os recursos estáticos do backend"
    dependsOn("buildFrontend")
    from("frontend/dist")
    into("backend/src/main/resources/static")
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
