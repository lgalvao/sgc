plugins {
    id("org.springframework.boot") version "3.5.6"
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

tasks.named("clean") {
    dependsOn("cleanFrontend")
}