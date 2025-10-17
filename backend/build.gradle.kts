import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    java
    pmd
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
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    // BD
    runtimeOnly("org.postgresql:postgresql")

    // Lombok
    compileOnly("org.projectlombok:lombok:${property("lombok.version")}")
    annotationProcessor("org.projectlombok:lombok:${property("lombok.version")}")
    testAnnotationProcessor("org.projectlombok:lombok:${property("lombok.version")}")

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
    testImplementation("com.h2database:h2")
    testImplementation("org.awaitility:awaitility")
    testImplementation("com.tngtech.archunit:archunit:1.4.1")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Documentação da API
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
    testImplementation("io.swagger.parser.v3:swagger-parser:2.1.35")
    testImplementation("com.atlassian.oai:swagger-request-validator-mockmvc:2.46.0")

    // Dependências básicas com versões mais recentes que as definidas pelo Spring (reduz CVEs)
    implementation("org.apache.commons:commons-lang3:3.19.0")
    implementation("ch.qos.logback:logback-classic:1.5.19")
    implementation("ch.qos.logback:logback-core:1.5.19")
}

tasks.named<ProcessResources>("processResources") {
    if (project.hasProperty("withFrontend") && project.property("withFrontend").toString() == "true") {
        dependsOn(":copyFrontend")
    }
}

tasks.withType<BootJar> {
    enabled = true
    if (project.hasProperty("withFrontend") && project.property("withFrontend").toString() == "true") {
        dependsOn(":copyFrontend")
    }
    mainClass.set("sgc.Sgc")
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2)
    forkEvery = 100L
    jvmArgs = listOf(
        "-Xmx4g",
        "-Dlogging.level.root=ERROR",
        "-Dlogging.level.sgc=ERROR",
        "-Dlogging.level.org.hibernate=ERROR",
        "-Dlogging.level.org.springframework=ERROR",
        "-Dspring.jpa.show-sql=false",
        "-Dspring.jpa.properties.hibernate.show_sql=false",
        "-Dspring.jpa.properties.hibernate.format_sql=false",
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
            logger.warn("byte-buddy-agent nao foi encontrado. Avisos do Mockito podem continuar aparecendo.")
        }
    }

    testLogging {
        events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.SHORT
        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = false
    }

    addTestListener(object : TestListener {
        private val failures = mutableListOf<TestFailure>()
        private val skipped = mutableListOf<String>()
        private var totalTests = 0
        private var passedTests = 0

        override fun beforeSuite(suite: TestDescriptor) {}
        override fun beforeTest(testDescriptor: TestDescriptor) {}

        override fun afterTest(desc: TestDescriptor, result: TestResult) {
            when (result.resultType) {
                TestResult.ResultType.SUCCESS -> {
                    passedTests++
                    totalTests++
                }

                TestResult.ResultType.FAILURE -> {
                    totalTests++
                    val exception = result.exception
                    val rootCause = getRootCause(exception)
                    failures.add(
                        TestFailure(
                            testClass = desc.className ?: "Unknown",
                            testMethod = desc.name,
                            errorMessage = rootCause?.message ?: exception?.message ?: "Erro desconhecido",
                            errorType = rootCause?.javaClass?.simpleName ?: exception?.javaClass?.simpleName
                            ?: "Exception",
                            stackTrace = filterStackTrace(rootCause ?: exception)
                        )
                    )
                }

                TestResult.ResultType.SKIPPED -> {
                    totalTests++
                    skipped.add("${desc.className ?: "Unknown"}.${desc.name}")
                }

                else -> {}
            }
        }

        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if (suite.parent == null) outputAgentSummary(result, failures, skipped)
        }

        private fun getRootCause(exception: Throwable?): Throwable? {
            var cause = exception
            while (cause?.cause != null && cause.cause != cause) {
                cause = cause.cause
            }
            return cause
        }

        private fun filterStackTrace(exception: Throwable?): List<String> {
            if (exception == null) return emptyList()

            return exception.stackTrace
                .filter { element ->
                    val className = element.className
                    (className.startsWith("sgc") ||
                            className.startsWith("org.springframework.web") ||
                            className.startsWith("org.springframework.data") ||
                            className.startsWith("org.hibernate") ||
                            className.startsWith("org.springframework.security") ||
                            (className.startsWith("org.springframework.boot") && !className.contains(".test."))) &&
                            !className.contains("gradle") &&
                            !className.contains("junit") &&
                            !className.contains("mockito")
                }
                .take(15)
                .map { element ->
                    "    ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})"
                }
        }

        private fun outputAgentSummary(
            result: TestResult,
            failures: List<TestFailure>,
            skipped: List<String>
        ) {
            val status = if (result.failedTestCount == 0L) "SUCESSO" else "FALHA"
            val durationSec = (result.endTime - result.startTime) / 1000.0
            val separator = "=".repeat(20)
            val separator2 = "-".repeat(20)

            println("\n$separator")
            println("RESUMO DOS TESTES")
            println(separator)
            println("Situacao: $status")
            println("Total:    ${result.testCount}")
            println("Sucesso:  ${result.successfulTestCount}")
            println("Falhas:   ${result.failedTestCount}")
            println("Ignorados: ${result.skippedTestCount}")
            println("Tempo:  %.2fs".format(durationSec))
            if (failures.isNotEmpty()) {
                println(separator2)
                println("TESTES FALHANDO (${failures.size})")
                println(separator2)
                failures.forEachIndexed { index, failure ->
                    println("\n${index + 1}. ${failure.testClass}")
                    println("   Metodo: ${failure.testMethod}")
                    println("   Erro: [${failure.errorType}] ${failure.errorMessage}")
                    if (failure.stackTrace.isNotEmpty()) {
                        println("   Stack trace filtrado:")
                        failure.stackTrace.forEach { line -> println("   $line") }
                    } else {
                        println("   (sem stack trace da aplicacao)")
                    }
                }
            }
            if (skipped.isNotEmpty()) {
                println("\n$separator2")
                println("TESTES IGNORADOS (${skipped.size})")
                println(separator2)
                skipped.forEach { println("  • $it") }
            }
            println(separator)
        }
    })

    reports {
        html.required.set(false)
        junitXml.required.set(false)
    }

    failFast = project.hasProperty("failFast")
}

data class TestFailure(
    val testClass: String,
    val testMethod: String,
    val errorMessage: String,
    val errorType: String,
    val stackTrace: List<String>
)

tasks.withType<JavaCompile> {
    options.apply {
        isIncremental = true
        isFork = true
        encoding = "UTF-8"
        compilerArgs.add("-Xlint:-options")
    }
    options.forkOptions.jvmArgs = (options.forkOptions.jvmArgs ?: emptyList()) + listOf(
        "--add-opens=jdk.unsupported/sun.misc=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED"
    )
}

tasks.named("build") { outputs.cacheIf { true } }

pmd {
    toolVersion = "7.17.0"
    rulesMinimumPriority = 5
}

tasks.withType<Pmd> {
    ruleSets = listOf()
    ruleSetFiles = files("config/pmd/custom-ruleset.xml")
    reports.xml.required.set(true)
    reports.html.required.set(false)
}

tasks.register("agentTest") {
    group = "verification"
    description = "Rodar testes com saída otimizada para agentes"
    dependsOn("test")
}

tasks.register<Test>("verboseTest") {
    useJUnitPlatform()
    group = "verification"
    description = "Rodar testes com saída completa"
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2)
    jvmArgs.addAll(tasks.withType<Test>().getByName("test").jvmArgs)

    val byteBuddyAgentFile =
        project.configurations.getByName("testRuntimeClasspath").files.find { it.name.contains("byte-buddy-agent") }

    doFirst {
        if (byteBuddyAgentFile != null) {
            jvmArgs("-javaagent:${byteBuddyAgentFile.path}")
        } else {
            logger.warn("byte-buddy-agent nao foi encontrado. Avisos do Mockito podem continuar aparecendo.")
        }
    }

    testLogging {
        events = setOf(TestLogEvent.STARTED, TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
    }

    testClassesDirs = tasks.test.get().testClassesDirs
    classpath = tasks.test.get().classpath
}

// Desabilita a tarefa pmdTest para ignorar os arquivos de teste
tasks.named("pmdTest") {
    enabled = false
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    jvmArgs = listOf("-Djdk.internal.vm.debug=release")
}