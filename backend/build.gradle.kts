plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    runtimeOnly("org.postgresql:postgresql")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<ProcessResources>("processResources") {
    // Build frontend only when explicitly requested to avoid running frontend build during backend tests.
    if (project.hasProperty("withFrontend") && project.property("withFrontend") == "true") {
        dependsOn(":copyFrontend")
    }
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    enabled = true
    // Copy frontend artifacts into backend jar only when explicitly requested.
    if (project.hasProperty("withFrontend") && project.property("withFrontend") == "true") {
        dependsOn(":copyFrontend")
    }
}

tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
    isEnabled = true
}

tasks.withType<Test> {
    useJUnitPlatform()
}