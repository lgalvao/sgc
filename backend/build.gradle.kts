import org.gradle.api.tasks.testing.logging.*
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

val argumentosJvmSemAvisoUnsafe = listOf("")

plugins {
    java
    jacoco
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spotbugs)
}

tasks.withType<JavaCompile>().configureEach {
    options.isFork = true
    options.forkOptions.jvmArgs?.addAll(argumentosJvmSemAvisoUnsafe)
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:unchecked",
            "-Xlint:deprecation",
            "-XDaddTypeAnnotationsToSymbol=true",
            "-parameters"
        )
    )
}

dependencies {
    implementation(platform(libs.jackson.bom))
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.aspectj)
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.starter.thymeleaf)
    implementation(libs.jakarta.servlet.api)
    implementation(libs.jackson.core)
    implementation(libs.tomcat.embed.core)
    implementation(libs.openpdf)
    implementation(libs.owasp.java.html.sanitizer)
    implementation(libs.jjwt.api)
    implementation(libs.rhino)
    implementation(libs.caffeine)
    implementation(libs.h2)
    implementation(libs.springdoc.openapi)

    developmentOnly(libs.spring.boot.devtools)

    compileOnly(libs.lombok)
    annotationProcessor(libs.spring.boot.configuration.processor)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.hibernate.validator.processor)

    runtimeOnly(libs.ojdbc11)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    runtimeOnly(libs.micrometer.registry.prometheus)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.test.autoconfigure)
    testImplementation(libs.junit.platform.suite.api)
    testImplementation(libs.awaitility)
    testImplementation(libs.archunit)
    testImplementation(libs.archunit.junit5)
    testImplementation(libs.jqwik)
    testImplementation(libs.equalsverifier)
    testImplementation(libs.groovy.all)
    testImplementation(libs.greenmail.junit5)
    testImplementation(libs.swagger.parser)
    testImplementation(libs.swagger.request.validator)

    testRuntimeOnly(libs.junit.platform.suite.engine)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<BootJar> {
    enabled = true
    mainClass.set("sgc.Sgc")
}

springBoot {
    buildInfo()
}

tasks.named<BootRun>("bootRun") {
    mainClass.set("sgc.Sgc")
    val env = (project.findProperty("ENV") ?: System.getProperty("spring.profiles.active"))?.toString() ?: "e2e"
    val envFile = rootProject.file(".env.$env")
    systemProperty("spring.profiles.active", env)
    println("Perfil Spring ativado: $env")

    if (envFile.exists()) {
        println("Carregando configurações de: .env.$env")
        envFile.useLines { lines ->
            lines.filter { it.isNotBlank() && !it.trim().startsWith("#") }
                .forEach { line ->
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) {
                        environment(parts[0].trim(), parts[1].trim())
                    }
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
        events(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
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
            if (suite.parent == null) {
                val output = """
                    |  Result:    ${result.resultType}
                    |  Total:     ${result.testCount} tests run
                    |  + Passed:  ${result.successfulTestCount}
                    |  - Failed:  ${result.failedTestCount}
                    |  ^ Ignored: ${result.skippedTestCount}
                    |  Time:      ${(result.endTime - result.startTime) / 1000.0}s
                """.trimMargin()
                println(output)

                if (showSlowTests && slowTests.isNotEmpty()) {
                    println("\nSlowest tests (>2s):")
                    slowTests.sortedByDescending { it.second }
                        .take(5)
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
    description = "Executa testes Unitários e Integração."
}

tasks.register<Test>("unitTest") {
    description = "Executa apenas testes unitários (exclui 'integration')."
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        excludeTags("integration")
    }
}

tasks.register<Test>("integrationTest") {
    description = "Executa apenas testes de integração."
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        includeTags("integration")
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

spotbugs {
    toolVersion.set(libs.versions.spotbugs.get())
    ignoreFailures.set(false)
    excludeFilter.set(file("etc/config/spotbugs/exclude.xml"))
}

tasks.named<com.github.spotbugs.snom.SpotBugsTask>("spotbugsTest") {
    enabled = false
}

tasks.register("qualityCheck") {
    group = "quality"
    description = "Runs all backend quality checks (tests, coverage, spotbugs)"
    dependsOn("check", "spotbugsMain")
}

tasks.register("qualityCheckFast") {
    group = "quality"
    description = "Runs fast backend quality checks (tests, coverage)"
    dependsOn("test", "jacocoTestCoverageVerification")
}
