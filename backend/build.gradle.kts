import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import info.solidsoft.gradle.pitest.PitestTask
import org.gradle.api.tasks.testing.logging.*
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    jacoco
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.spotbugs") version "6.4.8"
    id("info.solidsoft.pitest") version "1.19.0-rc.3"
    id("com.github.ben-manes.versions") version "0.53.0"
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
    testImplementation("org.springframework.security:spring-security-test:7.0.3")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Banco de Dados
    runtimeOnly("com.oracle.database.jdbc:ojdbc11:23.26.1.0.0")
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
    testImplementation("nl.jqno.equalsverifier:equalsverifier:4.3.1")
    testImplementation("io.rest-assured:rest-assured-all:6.0.0")
    testImplementation("org.apache.groovy:groovy-all:5.0.4")
    testImplementation("com.icegreen:greenmail-junit5:2.1.3")
    
    // Testes de Mutação
    testImplementation("org.pitest:pitest-junit5-plugin:1.2.3")

    // Testes de Contrato (Pact)
    testImplementation("au.com.dius.pact.provider:junit5:4.6.14")
    testImplementation("au.com.dius.pact.provider:spring:4.6.14")

    // Documentação da API
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")
    testImplementation("io.swagger.parser.v3:swagger-parser:2.1.37")
    implementation("org.mozilla:rhino:1.9.0")
    testImplementation("com.atlassian.oai:swagger-request-validator-mockmvc:2.46.0")

    // Dependências básicas com versões mais recentes que as definidas pelo Spring (reduz CVEs)
    implementation("org.apache.commons:commons-lang3:3.20.0")
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
        exceptionFormat = TestExceptionFormat.FULL
        showStackTraces = true
        showCauses = true
        showStandardStreams = false
    }

    val slowTests = mutableListOf<Pair<String, Long>>()
    val showSlowTests = false

    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            // Exibir resumo apenas para a suite raiz (nível do projeto)
            if (suite.parent == null) {
                val output = """
                    |  Resultado: ${result.resultType}
                    |  Total:     ${result.testCount} testes executados
                    |  ✓ Passou:   ${result.successfulTestCount}
                    |  ✗ Falhou:   ${result.failedTestCount}
                    |  ○ Ignorado: ${result.skippedTestCount}
                    |  Tempo:     ${(result.endTime - result.startTime) / 1000.0}s
                """.trimMargin()
                println(output)

                if (showSlowTests && slowTests.isNotEmpty()) {
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
        html.required.set(false)
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
                minimum = "0.99".toBigDecimal()
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

pitest {
    pitestVersion.set("1.22.1")
    junit5PluginVersion.set("1.2.3")
    targetClasses.set(listOf("sgc.organizacao.*"))
    targetTests.set(listOf("sgc.organizacao.*"))
    
    excludedClasses.set(listOf(
        "sgc.config.*",              // Configurações Spring
        "sgc.*Exception",            // Classes de exceção
        "sgc.*Erro*",                // Todas as classes de erro
        "sgc.*MapperImpl",           // Mappers MapStruct (gerados)
        "sgc.*Request",              // Request DTOs
        "sgc.*Response",             // Response DTOs
        "sgc.*Query",                // Query objects
        "sgc.*Command",              // Command objects
        "sgc.*View",                 // View objects
        "sgc.*Evento*",               // Event classes (domain events)
        "sgc.Sgc",                   // Classe main
    ))
    
    // Métodos ignorados (getters/setters já são excluídos por padrão)
    excludedMethods.set(listOf(
        "hashCode",
        "equals",
        "toString"
    ))
    
    mutators.set(listOf("ALL"))
    outputFormats.set(listOf("CSV"))
    timestampedReports.set(false)
    threads.set(Runtime.getRuntime().availableProcessors())
    timeoutFactor.set(BigDecimal("5.0"))
    verbose.set(true)
    failWhenNoMutations.set(false)
    
    val agentFile = project.configurations.getByName("testRuntimeClasspath").files.find {
        it.name.contains("byte-buddy-agent")
    }
    val pitestJvmArgs = mutableListOf(
        "-Xmx4096m",
        "-Xms512m",
        "-XX:+EnableDynamicAgentLoading",
        "-Xshare:off",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-opens=jdk.unsupported/sun.misc=ALL-UNNAMED"
    )
    agentFile?.let { pitestJvmArgs.add("-javaagent:${it.path}") }
    jvmArgs.set(pitestJvmArgs)
}

tasks.withType<DependencyUpdatesTask> {
    revision = "release"
    outputFormatter = "plain"
    checkConstraints = true
}

fun isNonStable(version: String): Boolean {
    val nonStableKeywords = listOf("ALPHA", "BETA", "RC", "CR", "M", "PREVIEW", "BUILD", "SNAPSHOT")
    val upperVersion = version.uppercase()
    val hasNonStableKeyword = nonStableKeywords.any { upperVersion.contains(it) }
    
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { upperVersion.contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    
    return hasNonStableKeyword || (!stableKeyword && !regex.matches(version))
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
            ?: throw GradleException("Especifique o módulo com -PtargetModule=<modulo> (ex: processo, subprocesso, mapa)")

        println("🎯 Analisando módulo: sgc.$targetModule.*")
        
        tasks.named<PitestTask>("pitest") {
            targetClasses.set(listOf("sgc.$targetModule.*"))
        }
    }
    
    finalizedBy("pitest")
}
