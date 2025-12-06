import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestStackTraceFilter
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

extra["jjwt.version"] = "0.13.0"
extra["mapstruct.version"] = "1.6.3"
extra["lombok.version"] = "1.18.42"

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("jakarta.servlet:jakarta.servlet-api")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.security:spring-security-test")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // BD
    runtimeOnly("org.postgresql:postgresql")
    implementation("com.h2database:h2")

    // Lombok
    compileOnly("org.projectlombok:lombok:${property("lombok.version")}")
    annotationProcessor("org.projectlombok:lombok:${property("lombok.version")}")

    // MapStruct
    implementation("org.mapstruct:mapstruct:${property("mapstruct.version")}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${property("mapstruct.version")}")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:${property("jjwt.version")}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jjwt.version")}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jjwt.version")}")

    // Segurança
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20240325.1")

    // Testes
    testImplementation("org.awaitility:awaitility")
    testImplementation("com.tngtech.archunit:archunit:1.4.1")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testImplementation("net.jqwik:jqwik:1.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Documentação da API
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
    testImplementation("io.swagger.parser.v3:swagger-parser:2.1.35")
    testImplementation("com.atlassian.oai:swagger-request-validator-mockmvc:2.46.0")

    // Dependências básicas com versões mais recentes que as definidas pelo Spring (reduz CVEs)
    implementation("org.apache.commons:commons-lang3:3.19.0")
    implementation("ch.qos.logback:logback-classic:1.5.20")
    implementation("ch.qos.logback:logback-core:1.5.20")
}

tasks.withType<BootJar> {
    enabled = true
    mainClass.set("sgc.Sgc")
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        events("skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
        showStackTraces = true
        showCauses = true
        showStandardStreams = false
        stackTraceFilters = setOf(TestStackTraceFilter.ENTRY_POINT)
    }

    jvmArgs = listOf(
        "-Dlogging.level.root=INFO",
        "-Dlogging.level.sgc=INFO",
        "-Dspring.jpa.show-sql=false",
        "-Dmockito.ext.disable=true",
        "-Xshare:off",
        "-XX:+EnableDynamicAgentLoading",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-opens=jdk.unsupported/sun.misc=ALL-UNNAMED"
    )

    val byteBuddyAgentFile =
        project.configurations.getByName("testRuntimeClasspath").files.find { it.name.contains("byte-buddy-agent") }

    doFirst {
        if (byteBuddyAgentFile != null) {
            jvmArgs("-javaagent:${byteBuddyAgentFile.path}")
        } else {
            logger.warn("byte-buddy-agent nao encontrado. Avisos do Mockito podem continuar aparecendo.")
        }
    }
}

tasks.withType<JavaCompile> {
    options.apply {
        isIncremental = true
        isFork = true
        encoding = "UTF-8"
        compilerArgs.add("-Xlint:deprecation")
    }
}