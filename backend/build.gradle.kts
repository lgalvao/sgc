
import org.gradle.api.tasks.testing.logging.*
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

val argumentosJvmSemAvisoUnsafe = emptyList<String>()

plugins {
    java
    jacoco
    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.spotbugs") version "6.4.8"
}

tasks.withType<JavaCompile>().configureEach {
    options.isFork = true
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation", "-XDaddTypeAnnotationsToSymbol=true"))
}

extra["lombok.version"] = "1.18.44"
extra["jjwt.version"] = "0.13.0"
extra["thymeleaf.version"] = "3.1.4.RELEASE"

dependencies {
    implementation(platform("tools.jackson:jackson-bom:3.1.1"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("jakarta.servlet:jakarta.servlet-api")
    implementation("tools.jackson.core:jackson-core:3.1.1")
    implementation("org.apache.tomcat.embed:tomcat-embed-core:11.0.21")
    implementation("org.thymeleaf:thymeleaf-spring6:3.1.4.RELEASE")
    implementation("com.github.librepdf:openpdf:3.0.3")
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20260313.1")
    implementation("io.jsonwebtoken:jjwt-api:${property("jjwt.version")}")
    implementation("org.mozilla:rhino:1.9.1")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.h2database:h2")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    compileOnly("org.projectlombok:lombok:${property("lombok.version")}")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok:${property("lombok.version")}")
    annotationProcessor("org.hibernate.validator:hibernate-validator-annotation-processor:9.1.0.Final")

    runtimeOnly("com.oracle.database.jdbc:ojdbc11:23.26.1.0.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jjwt.version")}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jjwt.version")}")

    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
    testImplementation("org.junit.platform:junit-platform-suite-api:6.0.3")
    testImplementation("org.awaitility:awaitility")
    testImplementation("com.tngtech.archunit:archunit:1.4.1")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testImplementation("net.jqwik:jqwik:1.9.3")
    testImplementation("nl.jqno.equalsverifier:equalsverifier:4.4.2")
    testImplementation("org.apache.groovy:groovy-all:5.0.4")
    testImplementation("com.icegreen:greenmail-junit5:2.1.8")
    testImplementation("io.swagger.parser.v3:swagger-parser:2.1.39")
    testImplementation("com.atlassian.oai:swagger-request-validator-mockmvc:2.46.1")

    testRuntimeOnly("org.junit.platform:junit-platform-suite-engine:6.0.3")
    testCompileOnly("org.projectlombok:lombok:${property("lombok.version")}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.3")
}

tasks.withType<BootJar> {
    enabled = true
    mainClass.set("sgc.Sgc")
}

tasks.named<BootRun>("bootRun") {
    mainClass.set("sgc.Sgc")
    val env = (project.findProperty("ENV") ?: System.getProperty("spring.profiles.active"))?.toString() ?: "e2e"
    val envFile = rootProject.file(".env.$env")
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
        *argumentosJvmSemAvisoUnsafe.toTypedArray(),
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
    toolVersion = "0.8.14"
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