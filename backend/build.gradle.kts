import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    jacoco
    checkstyle
    pmd
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("info.solidsoft.pitest") version "1.19.0-rc.1"
}

java {
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

extra["mapstruct.version"] = "1.6.3"
extra["lombok.version"] = "1.18.42"
extra["jjwt.version"] = "0.13.0"

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
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

    // Relatórios
    implementation("com.github.librepdf:openpdf:3.0.0")

    // Segurança
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20260102.1")
    implementation("io.jsonwebtoken:jjwt-api:${property("jjwt.version")}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jjwt.version")}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jjwt.version")}")

    // Testes
    testImplementation("org.awaitility:awaitility")
    testImplementation("com.tngtech.archunit:archunit:1.4.1")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("net.jqwik:jqwik:1.9.3")
    testImplementation("nl.jqno.equalsverifier:equalsverifier:3.18.1")

    // Documentação da API
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")
    testImplementation("io.swagger.parser.v3:swagger-parser:2.1.37")
    implementation("org.mozilla:rhino:1.9.0")
    testImplementation("com.atlassian.oai:swagger-request-validator-mockmvc:2.46.0")

    // Dependências básicas com versões mais recentes que as definidas pelo Spring (reduz CVEs)
    implementation("org.apache.commons:commons-lang3:3.20.0")
    implementation("ch.qos.logback:logback-classic:1.5.23")
    implementation("ch.qos.logback:logback-core:1.5.23")
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
    
    testLogging {
        events(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.SHORT
        showStackTraces = true
        showCauses = true
        showStandardStreams = false
    }

    val slowTests = mutableListOf<Pair<String, Long>>()

    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            // Exibir resumo apenas para a suite raiz (nível do projeto)
            if (suite.parent == null) {
                val output = """
                    |  Results: ${result.resultType}
                    |  Total:     ${result.testCount} tests run
                    |  ✓ Passed:  ${result.successfulTestCount}
                    |  ✗ Failed:  ${result.failedTestCount}
                    |  ○ Ignored: ${result.skippedTestCount}
                    |  Time:     ${(result.endTime - result.startTime) / 1000.0}s
                """.trimMargin()
                println(output)

                if (slowTests.isNotEmpty()) {
                    println("\nTestes mais lentos (> 1s):")
                    slowTests.sortedByDescending { it.second }
                        .take(10)
                        .forEach { (name, time) ->
                            println("  - ${time}ms: $name")
                        }
                }
            }
        }
        override fun beforeTest(testDescriptor: TestDescriptor) {}
        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
            val duration = result.endTime - result.startTime
            if (duration > 1000) {
                slowTests.add("${testDescriptor.className} > ${testDescriptor.name}" to duration)
            }
        }
    })

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

tasks.named<Test>("test") {
    description = "Executa TODOS os testes (Unitários e Integração)."
}

tasks.register<Test>("unitTest") {
    description = "Executa APENAS testes unitários (exclui tag 'integration')."
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        excludeTags("integration")
    }
}

tasks.register<Test>("integrationTest") {
    description = "Executa APENAS testes de integração (tag 'integration')."
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        includeTags("integration")
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.named("test"))
    
    // Relatório consome dados de qualquer tarefa de teste que rodou
    executionData.setFrom(fileTree(layout.buildDirectory.get().asFile).include("/jacoco/*.exec"))
    
    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(false)
    }
    
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/*MapperImpl*",
                    "sgc/Sgc.class",
                    "sgc/**/*Config.class",
                    "sgc/**/*Dto.class",
                    "sgc/**/*Exception.class",
                    "sgc/notificacao/NotificacaoModelosServiceMock.class",
                    "sgc/e2e/E2eController.class",
                    "sgc/seguranca/autenticacao/AcessoAdClient.class"
                )
            }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = "BRANCH"
                minimum = "0.85".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "LINE"
                minimum = "0.95".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

configure<info.solidsoft.gradle.pitest.PitestPluginExtension> {
    junit5PluginVersion.set("1.2.1")
    targetClasses.set(setOf("sgc.mapa.*"))
    excludedClasses.set(setOf(
        "sgc.Sgc",
        "sgc.**.*Config",
        "sgc.**.*Dto",
        "sgc.**.*Exception",
        "sgc.**.*Repo",
        "sgc.**.*MapperImpl"
    ))
    threads.set(8)
    outputFormats.set(setOf("XML", "HTML"))
}

// Configuração de Checkstyle
checkstyle {
    toolVersion = "10.12.4"
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 0
}

// Configuração de PMD
pmd {
    toolVersion = "7.0.0"
    isConsoleOutput = true
    isIgnoreFailures = false
    ruleSets = listOf() // Usar configuração customizada
    ruleSetFiles = files("config/pmd/pmd-ruleset.xml")
}

tasks.withType<Checkstyle> {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.withType<Pmd> {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

