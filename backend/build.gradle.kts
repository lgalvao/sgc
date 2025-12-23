import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.3.6"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

extra["mapstruct.version"] = "1.6.3"
extra["lombok.version"] = "1.18.42"
extra["jjwt.version"] = "0.13.0"
extra["modulith.version"] = "1.2.5"  // Versão do Spring Modulith compatível com Spring Boot 3.3.x

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:${property("modulith.version")}")
    }
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("jakarta.servlet:jakarta.servlet-api")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // BD
    runtimeOnly("com.oracle.database.jdbc:ojdbc11")
    implementation("com.h2database:h2")

    // Lombok
    compileOnly("org.projectlombok:lombok:${property("lombok.version")}")
    annotationProcessor("org.projectlombok:lombok:${property("lombok.version")}")
    testCompileOnly("org.projectlombok:lombok:${property("lombok.version")}")
    testAnnotationProcessor("org.projectlombok:lombok:${property("lombok.version")}")

    // MapStruct
    implementation("org.mapstruct:mapstruct:${property("mapstruct.version")}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${property("mapstruct.version")}")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // Segurança
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20240325.1")
    implementation("io.jsonwebtoken:jjwt-api:${property("jjwt.version")}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jjwt.version")}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jjwt.version")}")

    // Spring Modulith - Core
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-events-api")
    
    // Spring Modulith - Event Publication Registry
    implementation("org.springframework.modulith:spring-modulith-events-jpa")
    
    // Spring Modulith - Observability (opcional mas recomendado)
    runtimeOnly("org.springframework.modulith:spring-modulith-actuator")
    runtimeOnly("org.springframework.modulith:spring-modulith-observability")
    
    // Spring Modulith - Testes
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    
    // Spring Modulith - Documentação
    testImplementation("org.springframework.modulith:spring-modulith-docs")

    // Testes
    testImplementation("org.awaitility:awaitility")
    testImplementation("com.tngtech.archunit:archunit:1.4.1")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Documentação da API
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")
    testImplementation("io.swagger.parser.v3:swagger-parser:2.1.36")
    implementation("org.mozilla:rhino:1.8.1")
    testImplementation("com.atlassian.oai:swagger-request-validator-mockmvc:2.46.0")

    // Dependências básicas com versões mais recentes que as definidas pelo Spring (reduz CVEs)
    implementation("org.apache.commons:commons-lang3:3.20.0")
    implementation("ch.qos.logback:logback-classic:1.5.21")
    implementation("ch.qos.logback:logback-core:1.5.21")
}


tasks.withType<BootJar> {
    enabled = true
    mainClass.set("sgc.Sgc")
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    // Carregar variáveis do arquivo .env apropriado baseado na variável ENV
    // Uso: ./gradlew bootRun -PENV=hom (ou test, e2e)
    val env = project.findProperty("ENV")?.toString() ?: "test"
    val envFile = rootProject.file(".env.$env")
    
    // Define o perfil Spring automaticamente baseado no ENV
    systemProperty("spring.profiles.active", env)
    println("Perfil Spring ativado: $env")
    
    if (envFile.exists()) {
        println("Carregando configurações de: .env.$env")
        envFile.readLines()
            .filter { it.isNotBlank() && !it.trim().startsWith("#") }
            .forEach { line ->
                val parts = line.split("=", limit = 2)
                if (parts.size == 2) {
                    environment(parts[0].trim(), parts[1].trim())
                }
            }
    } else {
        println("Arquivo .env.$env não encontrado, usando configurações padrão do application.yml")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // Relatório é gerado após os testes

    testLogging {
        events("skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
        showStackTraces = true
        showCauses = true
        showStandardStreams = false
    }

    jvmArgs = listOf(
        "-Dmockito.ext.disable=true",
        "-Xshare:off",
        "-XX:+EnableDynamicAgentLoading",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-opens=jdk.unsupported/sun.misc=ALL-UNNAMED"
    )

    val byteBuddyAgentFile =
        project.configurations.getByName("testRuntimeClasspath").files.find {
            it.name.contains("byte-buddy-agent")
        }

    doFirst {
        if (byteBuddyAgentFile != null) {
            jvmArgs("-javaagent:${byteBuddyAgentFile.path}")
        } else {
            logger.warn("byte-buddy-agent nao encontrado. Avisos do Mockito podem continuar aparecendo.")
        }
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = "BRANCH"
                minimum = "0.60".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "LINE"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
