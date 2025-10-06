import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    java
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
    if (project.hasProperty("withFrontend") && project.property("withFrontend").toString() == "true") {
        dependsOn(":copyFrontend")
    }
}

tasks.withType<BootJar> {
    enabled = true
    // Copy frontend artifacts into backend jar only when explicitly requested.
    if (project.hasProperty("withFrontend") && project.property("withFrontend").toString() == "true") {
        dependsOn(":copyFrontend")
    }
}

tasks.withType<BootRun> {
    isEnabled = true
}

// ============================================
// AGENT-OPTIMIZED TEST CONFIGURATION
// ============================================

tasks.withType<Test> {
    useJUnitPlatform()

    // Performance optimization for agent iterations
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    setForkEvery(100) // Restart JVM every 100 tests to prevent memory leaks

    // JVM settings for stability and suppress Hibernate logging
    jvmArgs = listOf(
        "-Xmx2g",
        "-XX:+UseParallelGC",
        "-Dlogging.level.org.hibernate=ERROR",
        "-Dlogging.level.org.springframework.orm.jpa=ERROR",
        "-Dlogging.level.org.springframework.jdbc=ERROR",
        "-Dspring.jpa.show-sql=false",
        "-Dspring.jpa.properties.hibernate.show_sql=false",
        "-Dspring.jpa.properties.hibernate.format_sql=false",
        "-Dmockito.ext.disable=true", // Desabilita o self-attaching do Mockito
        "-XX:+EnableDynamicAgentLoading", // Suprime o warning de carregamento dinâmico de agentes
        "-Xshare:off" // Desabilita o compartilhamento de classes para suprimir o warning do OpenJDK
    )

    // Adicionar o Mockito Java Agent
    val byteBuddyAgentFile = project.configurations.getByName("testRuntimeClasspath").files.find { it.name.contains("byte-buddy-agent") }

    doFirst {
        if (byteBuddyAgentFile != null) {
            jvmArgs("-javaagent:${byteBuddyAgentFile.path}")
        } else {
            logger.warn("byte-buddy-agent not found in testRuntimeClasspath. Mockito warnings might persist.")
        }
    }

    // Minimal console noise
    testLogging {
        events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = false // Suppress System.out/err during tests
    }

    // Custom test listener for agent-friendly output
    addTestListener(object : TestListener {
        private val failures = mutableListOf<TestFailure>()
        private val skipped = mutableListOf<String>()

        override fun beforeSuite(suite: TestDescriptor) {}
        override fun beforeTest(testDescriptor: TestDescriptor) {}

        override fun afterTest(desc: TestDescriptor, result: TestResult) {
            when (result.resultType) {
                TestResult.ResultType.FAILURE -> {
                    val exception = result.exception
                    failures.add(
                        TestFailure(
                            testClass = desc.className ?: "Unknown",
                            testMethod = desc.name,
                            errorMessage = exception?.message ?: "Unknown error",
                            errorType = exception?.javaClass?.simpleName ?: "Exception",
                            stackTrace = filterStackTrace(exception)
                        )
                    )
                }

                TestResult.ResultType.SKIPPED -> {
                    skipped.add("${desc.className ?: "Unknown"}.${desc.name}")
                }

                else -> {}
            }
        }

        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            // Only output summary for root suite (all tests completed)
            if (suite.parent == null) {
                outputAgentSummary(result, failures, skipped)
            }
        }

        private fun filterStackTrace(exception: Throwable?): List<String> {
            if (exception == null) return emptyList()

            return exception.stackTrace
                .filter { element ->
                    val className = element.className
                    // Include: Your app code, Spring framework (but not test internals)
                    // TODO: Replace "com.yourcompany" with your actual package name
                    (className.startsWith("com.yourcompany") ||
                            className.startsWith("org.springframework.web") ||
                            className.startsWith("org.springframework.data") ||
                            className.startsWith("org.springframework.boot") && !className.contains(".test.")) &&
                            // Exclude: Build tool internals
                            !className.contains("gradle") &&
                            !className.contains("junit.platform.launcher")
                }
                .take(5) // Limit depth to most relevant frames
                .map { element ->
                    "    at ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})"
                }
        }

        private fun outputAgentSummary(
            result: TestResult,
            failures: List<TestFailure>,
            skipped: List<String>
        ) {
            println("AGENT TEST SUMMARY")
            println()
            println("Status: ${if (result.failedTestCount == 0L) "✅ PASSED" else "❌ FAILED"}")
            println("Total:   ${result.testCount}")
            println("Passed:  ${result.successfulTestCount}")
            println("Failed:  ${result.failedTestCount}")
            println("Skipped: ${result.skippedTestCount}")
            println("Time:    ${result.endTime - result.startTime}ms")

            if (failures.isNotEmpty()) {
                println("\n" + "-".repeat(80))
                println("FAILURES (${failures.size})")
                println("-".repeat(80))

                failures.forEachIndexed { index, failure ->
                    println("\n${index + 1}. ${failure.testClass}.${failure.testMethod}")
                    println("   Error: ${failure.errorType}: ${failure.errorMessage}")

                    if (failure.stackTrace.isNotEmpty()) {
                        println("   Stack trace (application code only):")
                        failure.stackTrace.forEach { line ->
                            println(line)
                        }
                    }
                }
            }

            if (skipped.isNotEmpty()) {
                println("SKIPPED (${skipped.size})")
                println("-".repeat(80))
                skipped.forEach { println("  • $it") }
            }

            println("\n" + "=".repeat(80))
            println()

            // Machine-readable JSON output for advanced agent parsing
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
                ${failures.joinToString(",\n    ") { it.toJson() }}
              ],
              "skipped": [
                ${skipped.joinToString(",\n    ") { "\"$it\"" }}
              ]
            }
            """.trimIndent()

            println("---JSON_START---")
            println(json)
            println("---JSON_END---")
        }
    })

    // Generate reports but don't open in browser
    reports {
        html.required.set(false)
        junitXml.required.set(true)
    }

    // Fail fast option - stop on first failure (useful for debugging)
    failFast = project.hasProperty("failFast")
}

// Data class for test failures
data class TestFailure(
    val testClass: String,
    val testMethod: String,
    val errorMessage: String,
    val errorType: String,
    val stackTrace: List<String>
) {
    fun toJson(): String = """
        {
          "class": "$testClass",
          "method": "$testMethod",
          "error_type": "$errorType",
          "error_message": "${errorMessage.replace("\"", "\\\"").replace("\n", "\\n")}",
          "stack_trace": [
            ${stackTrace.joinToString(",\n      ") { "\"${it.replace("\"", "\\\"")}\"" }}
          ]
        }
    """.trimIndent()
}

// ============================================
// BUILD OPTIMIZATION FOR AGENT ITERATIONS
// ============================================

// Enable incremental compilation
tasks.withType<JavaCompile> {
    options.apply {
        isIncremental = true
        isFork = true
        encoding = "UTF-8"
    }
}

// Build cache for faster rebuilds
tasks.named("build") {
    outputs.cacheIf { true }
}

// ============================================
// AGENT HELPER TASKS
// ============================================

// Quick test with minimal output - perfect for agents
tasks.register("agentTest") {
    group = "verification"
    description = "Run tests with agent-optimized output"
    dependsOn("test")
    doLast {
        println("\n✅ Agent test complete. Check output above for summary.")
    }
}

// Test single class - useful for agent iteration
tasks.register<Test>("testClass") {
    group = "verification"
    description = "Run a single test class: ./gradlew testClass -PtestClass=YourTestClass"

    useJUnitPlatform()

    filter {
        val className = project.findProperty("testClass") as? String
        if (className != null) {
            includeTestsMatching("*.$className")
        }
    }

    testLogging {
        events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.FULL
        showStackTraces = true
    }
}