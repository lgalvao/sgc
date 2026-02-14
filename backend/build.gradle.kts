import org.gradle.api.tasks.testing.logging.*
import org.springframework.boot.gradle.tasks.bundling.BootJar
import info.solidsoft.gradle.pitest.PitestTask

plugins {
    java
    jacoco
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.spotbugs") version "6.4.8"
    id("info.solidsoft.pitest") version "1.19.0-rc.3"
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
    testImplementation("io.rest-assured:rest-assured-all:6.0.0")
    testImplementation("org.apache.groovy:groovy-all:5.0.3")
    
    // Mutation Testing
    testImplementation("org.pitest:pitest-junit5-plugin:1.2.1")

    // Documentação da API
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")
    testImplementation("io.swagger.parser.v3:swagger-parser:2.1.37")
    implementation("org.mozilla:rhino:1.9.0")
    testImplementation("com.atlassian.oai:swagger-request-validator-mockmvc:2.46.0")

    // Dependências básicas com versões mais recentes que as definidas pelo Spring (reduz CVEs)
    implementation("org.apache.commons:commons-lang3:3.20.0")
    implementation("ch.qos.logback:logback-classic:1.5.25")
    implementation("ch.qos.logback:logback-core:1.5.25")
    testImplementation("org.assertj:assertj-core:3.27.7")

    // Analise Estatica
    implementation("org.jspecify:jspecify:1.0.0")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}


tasks.withType<BootJar> {
    enabled = true
    mainClass.set("sgc.Sgc")
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    mainClass.set("sgc.Sgc")

    // Carregar variáveis do arquivo .env apropriado baseado na variável ENV
    // Uso: ./gradlew bootRun -PENV=hom (ou test, e2e)
    // Também aceita -Dspring.profiles.active=hom
    val env = (project.findProperty("ENV") ?: System.getProperty("spring.profiles.active"))?.toString() ?: "test"
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
    ignoreFailures = false
    useJUnitPlatform()

    testLogging {
        events(TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.STANDARD_ERROR)
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
                    println("\nTestes mais lentos (>2s):")
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
            if (duration > 2000) {
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

jacoco {
    toolVersion = "0.8.13"
}

tasks.jacocoTestReport {
    dependsOn(tasks.named("test"))
    // Relatório consome dados de qualquer tarefa de teste que rodou
    executionData.setFrom(fileTree(layout.buildDirectory).include("jacoco/*.exec"))

    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(true)
    }

    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    // Bootstrap e configuração
                    "sgc/Sgc.class",
                    "sgc/**/*Config.class",
                    "sgc/**/*Properties.class",
                    
                    // Exceções (maioria simples)
                    "sgc/**/Erro*.class",
                    
                    // Mocks de teste
                    "sgc/notificacao/NotificacaoModelosServiceMock.class",
                    
                    // Enums simples sem lógica de negócio
                    "sgc/**/Status*.class",
                    "sgc/**/Tipo*.class",
                    
                    // Classes geradas pelo MapStruct
                    "sgc/**/*Impl.class"
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
                minimum = "0.95".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "LINE"
                minimum = "0.99".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "INSTRUCTION"
                minimum = "0.99".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

spotbugs {
    toolVersion = "4.9.8"
    ignoreFailures.set(true)
    excludeFilter.set(file("etc/config/spotbugs/exclude.xml"))
}

tasks.register("qualityCheck") {
    group = "quality"
    description = "Runs all backend quality checks (tests, coverage, spotbugs)"
    dependsOn("check", "spotbugsMain", "spotbugsTest")
}

tasks.register("qualityCheckFast") {
    group = "quality"
    description = "Runs fast backend quality checks (tests, coverage)"
    dependsOn("test", "jacocoTestCoverageVerification")
}

// ============================================
// Mutation Testing Configuration (PIT)
// ============================================

pitest {
    // Versão do PIT e plugins
    pitestVersion.set("1.18.1")
    junit5PluginVersion.set("1.2.1")
    
    // Classes alvo - todo o pacote sgc
    targetClasses.set(listOf("sgc.*"))
    targetTests.set(listOf("sgc.*"))
    
    // Exclusões - classes que não agregam valor para mutation testing
    excludedClasses.set(listOf(
        "sgc.config.*",              // Configurações Spring
        "sgc.*Exception",            // Classes de exceção
        "sgc.*Mapper",               // Mappers MapStruct (gerados)
        "sgc.*MapperImpl",           // Mappers MapStruct (gerados)
        "sgc.*.dto.*",               // DTOs (baixa lógica)
        "sgc.Sgc",                   // Classe main
        "sgc.SgcTest"                // Classe de teste da main
    ))
    
    // Métodos ignorados (getters/setters já são excluídos por padrão)
    excludedMethods.set(listOf(
        "hashCode",
        "equals",
        "toString"
    ))
    
    // Mutadores - começar com DEFAULTS (Fase 1-4 do plano)
    // Opções: DEFAULTS, STRONGER, ALL
    mutators.set(listOf("DEFAULTS"))
    
    // Formatos de relatório
    outputFormats.set(listOf("HTML", "XML", "CSV"))
    
    // Relatórios com timestamp desabilitado (facilita comparação)
    timestampedReports.set(false)
    
    // Performance - usar todos os cores disponíveis
    threads.set(Runtime.getRuntime().availableProcessors())
    
    // Verbose output para debug
    verbose.set(false)
    
    // Detectar mutantes não cobertos por testes (failWhenNoMutations = false)
    failWhenNoMutations.set(false)
    
    // Thresholds desabilitados inicialmente (habilitar na Fase 6)
    // mutationThreshold.set(80)
    // coverageThreshold.set(99)
}

// Tarefa customizada para mutation testing completo
tasks.register("mutationTest") {
    group = "quality"
    description = "Executa mutation testing completo com PIT (gera relatório em build/reports/pitest)"
    dependsOn("pitest")
    
    doLast {
        val reportDir = layout.buildDirectory.dir("reports/pitest").get().asFile
        println("════════════════════════════════════════════════════════════")
        println("✅ Mutation Testing Concluído!")
        println("📊 Relatório disponível em: $reportDir/index.html")
        println("════════════════════════════════════════════════════════════")
    }
}

// Tarefa para mutation testing incremental (apenas mudanças recentes)
tasks.register("mutationTestIncremental") {
    group = "quality"
    description = "Mutation testing incremental (apenas classes modificadas recentemente)"
    
    doFirst {
        // Detectar classes modificadas via git
        val gitDiff = providers.exec {
            commandLine("git", "diff", "--name-only", "HEAD~1", "HEAD")
        }.standardOutput.asText.get()
        
        val modifiedClasses = gitDiff.lines()
            .filter { it.startsWith("backend/src/main/java/") && it.endsWith(".java") }
            .map { 
                it.removePrefix("backend/src/main/java/")
                  .removeSuffix(".java")
                  .replace("/", ".")
            }
        
        if (modifiedClasses.isEmpty()) {
            println("⚠️  Nenhuma classe Java modificada detectada")
        } else {
            println("🎯 Analisando ${modifiedClasses.size} classe(s) modificada(s):")
            modifiedClasses.forEach { println("   - $it") }
            
            // Configurar PIT para analisar apenas classes modificadas
            tasks.named<PitestTask>("pitest") {
                targetClasses.set(modifiedClasses)
            }
        }
    }
    
    finalizedBy("pitest")
}

// Tarefa para análise de mutantes por módulo
tasks.register("mutationTestModulo") {
    group = "quality"
    description = "Mutation testing de um módulo específico (use -PtargetModule=processo)"
    
    doFirst {
        val targetModule = project.findProperty("targetModule")?.toString()
        
        if (targetModule == null) {
            throw GradleException("Especifique o módulo com -PtargetModule=<modulo> (ex: processo, subprocesso, mapa)")
        }
        
        println("🎯 Analisando módulo: sgc.$targetModule.*")
        
        tasks.named<PitestTask>("pitest") {
            targetClasses.set(listOf("sgc.$targetModule.*"))
        }
    }
    
    finalizedBy("pitest")
}
