import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

extra["jjwt.version"] = "0.13.0"
extra["mapstruct.version"] = "1.6.3"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    runtimeOnly("org.postgresql:postgresql")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("io.jsonwebtoken:jjwt-api:${property("jjwt.version")}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jjwt.version")}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jjwt.version")}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${property("mapstruct.version")}")
    implementation("org.mapstruct:mapstruct:${property("mapstruct.version")}")

    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")
    testImplementation("org.awaitility:awaitility:4.2.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
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
    mainClass.set("sgc.SgcApplication")
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    forkEvery = 100L
    jvmArgs = listOf(
        "-Xmx2g",
        "-XX:+UseParallelGC",
        "-Dlogging.level.root=ERROR",
        "-Dlogging.level.sgc=ERROR",
        "-Dlogging.level.org.hibernate=ERROR",
        "-Dlogging.level.org.springframework=ERROR",
        "-Dspring.jpa.show-sql=false",
        "-Dspring.jpa.properties.hibernate.show_sql=false",
        "-Dspring.jpa.properties.hibernate.format_sql=false",
        "-Xshare:off",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "-Dmockito.ext.disable=true",
        "-XX:+EnableDynamicAgentLoading",
        "--enable-preview"
    )

    val byteBuddyAgentFile =
        project.configurations.getByName("testRuntimeClasspath").files.find { it.name.contains("byte-buddy-agent") }

    doFirst {
        if (byteBuddyAgentFile != null) {
            jvmArgs("-javaagent:${byteBuddyAgentFile.path}")
        } else {
            logger.warn("byte-buddy-agent not found in testRuntimeClasspath. Mockito warnings might persist.")
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
                            errorMessage = rootCause?.message ?: exception?.message ?: "Unknown error",
                            errorType = rootCause?.javaClass?.simpleName ?: exception?.javaClass?.simpleName
                            ?: "Exception",
                            stackTrace = filterStackTrace(rootCause ?: exception),
                            fullStackTrace = getFullStackTrace(rootCause ?: exception)
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
            if (suite.parent == null) {
                outputAgentSummary(result, failures, skipped)
            }
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

        private fun getFullStackTrace(exception: Throwable?): List<String> {
            if (exception == null) return emptyList()
            return exception.stackTrace
                .take(30)
                .map { element ->
                    "${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})"
                }
        }

        private fun outputAgentSummary(
            result: TestResult,
            failures: List<TestFailure>,
            skipped: List<String>
        ) {
            val status = if (result.failedTestCount == 0L) "✅ SUCESSO" else "❌ FALHA"
            val durationSec = (result.endTime - result.startTime) / 1000.0

            println("\n${"=".repeat(80)}")
            println("RESUMO DOS TESTES")
            println("=".repeat(80))
            println()
            println("Status: $status")
            println("Total:    ${result.testCount}")
            println("Sucesso:  ${result.successfulTestCount}")
            println("Falhas:   ${result.failedTestCount}")
            println("Ignorados: ${result.skippedTestCount}")
            println("Duração:  %.2fs".format(durationSec))

            if (failures.isNotEmpty()) {
                println("\n${"-".repeat(80)}")
                println("TESTES QUE FALHARAM (${failures.size})")
                println("-".repeat(80))
                failures.forEachIndexed { index, failure ->
                    println("\n${index + 1}. ${failure.testClass}")
                    println("   Método: ${failure.testMethod}")
                    println("   Erro: [${failure.errorType}] ${failure.errorMessage}")
                    if (failure.stackTrace.isNotEmpty()) {
                        println("   Stack trace relevante:")
                        failure.stackTrace.forEach { line -> println("   $line") }
                    } else {
                        println("   (sem stack trace da aplicação)")
                    }
                }
            }

            if (skipped.isNotEmpty()) {
                println("\n${"-".repeat(80)}")
                println("TESTES IGNORADOS (${skipped.size})")
                println("-".repeat(80))
                skipped.forEach { println("  • $it") }
            }

            println("\n${"=".repeat(80)}")

            outputJsonSummary(result, failures, skipped)
        }

        private fun outputJsonSummary(
            result: TestResult,
            failures: List<TestFailure>,
            skipped: List<String>
        ) {
            val json = """
{
  "status": "${if (result.failedTestCount == 0L) "passed" else "failed"}",
  "summary": {
    "total": ${result.testCount},
    "passed": ${result.successfulTestCount},
    "failed": ${result.failedTestCount},
    "skipped": ${result.skippedTestCount},
    "duration_ms": ${result.endTime - result.startTime}
  },
  "failures": [
${failures.joinToString(",\n") { "    " + it.toJson().prependIndent("    ").trim() }}
  ],
  "skipped": [
${skipped.joinToString(",\n") { "    \"$it\"" }}
  ]
}
            """.trimIndent()

            println("\n---JSON_START---")
            println(json)
            println("---JSON_END---")
        }
    })

    reports {
        html.required.set(false)
        junitXml.required.set(true)
    }

    failFast = project.hasProperty("failFast")
}

data class TestFailure(
    val testClass: String,
    val testMethod: String,
    val errorMessage: String,
    val errorType: String,
    val stackTrace: List<String>,
    val fullStackTrace: List<String> = emptyList()
) {
    fun toJson(): String {
        val escapedMessage = errorMessage
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

        return """
{
  "class": "$testClass",
  "method": "$testMethod",
  "error_type": "$errorType",
  "error_message": "$escapedMessage",
  "stack_trace_relevant": [
         ${stackTrace.joinToString(",\n") { "    \"${it.replace("\"", "\\\"")}\"" }}
  ],
  "stack_trace_full": [
         ${fullStackTrace.joinToString(",\n") { "    \"${it.replace("\"", "\\\"")}\"" }}
  ]
}
        """.trimIndent()
    }
}

tasks.withType<JavaCompile> {
    options.apply {
        isIncremental = true
        isFork = true
        encoding = "UTF-8"
        compilerArgs.add("--enable-preview")
    }
}

tasks.named("build") {
    outputs.cacheIf { true }
}

tasks.register("agentTest") {
    group = "verification"
    description = "Run tests with agent-optimized output"
    dependsOn("test")
}

tasks.register<Test>("testClass") {
    group = "verification"
    description = "Run a single test class: ./gradlew testClass -PtestClass=YourTestClass"

    useJUnitPlatform()
    filter {
        val className = project.findProperty("testClass") as? String
        if (className != null) {
            includeTestsMatching("*$className")
        }
    }

    testLogging {
        events = setOf(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.STANDARD_OUT,
            TestLogEvent.STANDARD_ERROR
        )
        exceptionFormat = TestExceptionFormat.FULL
        showStackTraces = true
        showExceptions = true
        showCauses = true
        showStandardStreams = true
    }
}

tasks.register<Test>("testMethod") {
    group = "verification"
    description = "Run a single test method: ./gradlew testMethod -PtestClass=YourTestClass -PtestMethod=yourMethod"

    useJUnitPlatform()
    filter {
        val className = project.findProperty("testClass") as? String
        val methodName = project.findProperty("testMethod") as? String
        if (className != null && methodName != null) {
            includeTestsMatching("*$className*$methodName*")
        }
    }

    testLogging {
        events = setOf(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.STANDARD_OUT,
            TestLogEvent.STANDARD_ERROR
        )
        exceptionFormat = TestExceptionFormat.FULL
        showStackTraces = true
        showExceptions = true
        showCauses = true
        showStandardStreams = true
    }
}