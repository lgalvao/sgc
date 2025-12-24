import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    jacoco
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("info.solidsoft.pitest") version "1.19.0-rc.2"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

extra["mapstruct.version"] = "1.6.3"
extra["lombok.version"] = "1.18.42"
extra["jjwt.version"] = "0.13.0"
extra["modulith.version"] = "2.0.1"  // Versão do Spring Modulith compatível com Spring Boot 4.0.1

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:${property("modulith.version")}")
    }
}

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
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
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
    
    // Spring Modulith - Event Serialization (required for 2.0+)
    implementation("org.springframework.modulith:spring-modulith-events-jackson")
    
    // Spring Modulith - Observability (opcional mas recomendado)
    runtimeOnly("org.springframework.modulith:spring-modulith-actuator")
    runtimeOnly("org.springframework.modulith:spring-modulith-observability")
    
    // Spring Modulith - Testes
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    
    // Spring Modulith - Documentação
    testImplementation("org.springframework.modulith:spring-modulith-docs")

    // Testes
    testImplementation("org.awaitility:awaitility")
    testImplementation("net.jqwik:jqwik:1.9.2")
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
                minimum = "0.70".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "LINE"
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.jacocoTestCoverageVerification)
    dependsOn("propertyTest")
}

tasks.register<Test>("propertyTest") {
    description = "Runs property-based tests."
    group = "verification"
    val testSourceSet = sourceSets.getByName("test")
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath
    useJUnitPlatform {
        includeTags("pbt")
    }
    shouldRunAfter("test")
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        excludeTags("pbt")
    }
}

// ===== Mutation Testing (PIT) =====

pitest {
    // Versão do PITest
    pitestVersion.set("1.22.0")
    
    // Versão do JUnit 5 Plugin
    junit5PluginVersion.set("1.2.1")
    
    // Pacotes alvo para mutation testing (foco em lógica de negócio)
    targetClasses.set(setOf(
        "sgc.processo.internal.service.*",
        "sgc.subprocesso.internal.service.*",
        "sgc.mapa.*",
        "sgc.mapa.internal.service.*",
        "sgc.atividade.*",
        "sgc.comum.erros.*",
        "sgc.comum.json.*"
    ))
    
    // Pacotes de teste correspondentes
    targetTests.set(setOf(
        "sgc.processo.*",
        "sgc.subprocesso.*",
        "sgc.mapa.*",
        "sgc.atividade.*",
        "sgc.comum.*"
    ))
    
    // Mutadores (operadores de mutação)
    mutators.set(setOf(
        "DEFAULTS",           // Conjunto padrão de mutadores
        "STRONGER",           // Mutadores mais fortes
        "REMOVE_CONDITIONALS" // Remove condicionais para verificar se são testados
    ))
    
    // Threads para execução paralela
    threads.set(Runtime.getRuntime().availableProcessors())
    
    // Output formats
    outputFormats.set(setOf("HTML", "XML"))
    
    // Não usar subpastas com timestamp
    timestampedReports.set(false)
    
    // Excluir classes geradas automaticamente ou 'burras'
    excludedClasses.set(setOf(
        "sgc.*.internal.model.*",      // Entidades JPA (apenas getters/setters)
        "sgc.*.api.*Dto",               // DTOs (apenas dados)
        "sgc.*.api.*Request",           // Request objects
        "sgc.*.api.*Response",          // Response objects
        "sgc.*.internal.mappers.*",     // MapStruct mappers (gerados)
        "sgc.*.*Mapper",                // MapStruct mappers
        "sgc.*.*MapperImpl",            // MapStruct implementations
        "sgc.*.internal.erros.Erro*",   // Exceções customizadas (apenas estrutura)
        "sgc.comum.config.*",           // Configurações do Spring
        "sgc.Sgc",                      // Classe main
        "sgc.e2e.*"                     // Endpoints de teste E2E
    ))
    
    // Evitar chamadas de log
    avoidCallsTo.set(setOf(
        "org.slf4j",
        "ch.qos.logback"
    ))
    
    
    // JVM args para PITest
    jvmArgs.set(listOf(
        "-Xmx4g",
        "-Dmockito.ext.disable=true",
        "-Xshare:off",
        "-XX:+EnableDynamicAgentLoading",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-opens=jdk.unsupported/sun.misc=ALL-UNNAMED"
    ))
}

// Task para executar mutation testing em módulo específico
tasks.register("mutationTestModule") {
    group = "verification"
    description = "Executa mutation testing em um módulo específico (use -Pmodule=nome)"
    
    doFirst {
        val module = project.findProperty("module")?.toString()
            ?: throw GradleException("Especifique o módulo com -Pmodule=nome (ex: -Pmodule=processo)")
        
        println("Executando mutation testing no módulo: $module")
        
        // Configurar PIT para o módulo específico
        configure<info.solidsoft.gradle.pitest.PitestPluginExtension> {
            targetClasses.set(setOf("sgc.$module.*"))
            targetTests.set(setOf("sgc.$module.*"))
        }
    }
    
    finalizedBy("pitest")
}
