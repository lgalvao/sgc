import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestStackTraceFilter
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    java
    jacoco
    checkstyle
    pmd
    id("com.github.spotbugs") version "6.4.7"
    id("com.diffplug.spotless") version "8.1.0"
    id("com.github.ben-manes.versions") version "0.53.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

extra["jjwt.version"] = "0.13.0"
extra["mapstruct.version"] = "1.6.3"
extra["lombok.version"] = "1.18.42"

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
    runtimeOnly("org.postgresql:postgresql")
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

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:${property("jjwt.version")}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jjwt.version")}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jjwt.version")}")

    // Segurança
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20240325.1")

    // Testes
    testImplementation("org.awaitility:awaitility")
    testImplementation("com.tngtech.archunit:archunit:1.4.1")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testImplementation("net.jqwik:jqwik:1.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Documentação da API
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")
    testImplementation("io.swagger.parser.v3:swagger-parser:2.1.36")
    testImplementation("com.atlassian.oai:swagger-request-validator-mockmvc:2.46.0")

    // Dependências básicas com versões mais recentes que as definidas pelo Spring (reduz CVEs)
    implementation("org.apache.commons:commons-lang3:3.20.0")
    implementation("ch.qos.logback:logback-classic:1.5.21")
    implementation("ch.qos.logback:logback-core:1.5.21")

    // Quality Check Dependencies (SpotBugs)
    spotbugs("com.github.spotbugs:spotbugs:4.9.8")
}

// --- Quality Checks Configurations ---

// Spotless
spotless {
    // EnforceCheck is disabled by default to avoid breaking CI for existing formatting issues.
    // Run './gradlew spotlessApply' to fix.
    isEnforceCheck = false
    java {
        googleJavaFormat("1.33.0").aosp().reflowLongStrings()
        leadingTabsToSpaces(2)
        target("src/*/java/**/*.java")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

// JaCoCo
jacoco {
    toolVersion = "0.8.14"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/test/html"))
    }
    classDirectories.setFrom(
        sourceSets.main.get().output.asFileTree.matching {
            exclude(
                "**/config/**",
                "**/dto/**",
                "**/entity/**",
                "**/mapper/**",
                "**/*Application.class",
                "**/Sgc.class"
            )
        }
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = 0.90.toBigDecimal()
            }
        }
    }
    classDirectories.setFrom(tasks.jacocoTestReport.get().classDirectories)
}

// SpotBugs
spotbugs {
    toolVersion.set("4.9.8")
    excludeFilter.set(file("config/spotbugs/exclude.xml"))
    ignoreFailures.set(true) // Don't fail build
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.MEDIUM)
}

// Checkstyle
checkstyle {
    toolVersion = "12.2.0"
    configFile = file("config/checkstyle/checkstyle.xml")
    maxWarnings = 0
    isIgnoreFailures = true // Don't fail build, we check manually via quality task
}

// PMD
pmd {
    toolVersion = "7.19.0"
    // ruleSets = listOf(file("config/pmd/ruleset.xml").toURI().toString())
    ruleSetFiles = files("config/pmd/ruleset.xml")
    isIgnoreFailures = true // Don't fail build
}

tasks.named("pmdTest") {
    enabled = false
}

// --- Custom Quality Tasks ---

tasks.register("qualityCheck") {
    group = "quality"
    description = "Runs all quality checks (tests, coverage, SpotBugs, Checkstyle, PMD)"

    dependsOn(tasks.test)
    dependsOn(tasks.jacocoTestReport)
    dependsOn(tasks.jacocoTestCoverageVerification)
    dependsOn(tasks.checkstyleMain)
    dependsOn(tasks.checkstyleTest)
    dependsOn(tasks.pmdMain)
    dependsOn(tasks.pmdTest)
    dependsOn(tasks.spotbugsMain)
    dependsOn(tasks.spotbugsTest)

    val buildDir = layout.buildDirectory.get().asFile.absolutePath

    doLast {
        println("\n=== Quality Check Summary ===")
        println("JaCoCo Report: file://$buildDir/reports/jacoco/test/html/index.html")
        println("Checkstyle Main: file://$buildDir/reports/checkstyle/main.html")
        println("Checkstyle Test: file://$buildDir/reports/checkstyle/test.html")
        println("PMD Main: file://$buildDir/reports/pmd/main.html")
        println("PMD Test: file://$buildDir/reports/pmd/test.html")
        println("SpotBugs Main: file://$buildDir/reports/spotbugs/main.html")
        println("SpotBugs Test: file://$buildDir/reports/spotbugs/test.html")
    }
}

tasks.register("qualityCheckFast") {
    group = "quality"
    description = "Runs only tests and coverage"

    dependsOn(tasks.test)
    dependsOn(tasks.jacocoTestReport)
    dependsOn(tasks.jacocoTestCoverageVerification)

    val buildDir = layout.buildDirectory.get().asFile.absolutePath

    doLast {
        println("\n=== Quality Check Fast Summary ===")
        println("JaCoCo Report: file://$buildDir/reports/jacoco/test/html/index.html")
    }
}

// Ensure quality checks don't run on normal 'check'
tasks.named("check") {
    setDependsOn(dependsOn.filter {
        it != tasks.checkstyleMain &&
                it != tasks.checkstyleTest &&
                it != tasks.pmdMain &&
                it != tasks.pmdTest &&
                it != tasks.spotbugsMain &&
                it != tasks.spotbugsTest
    })
}

tasks.withType<BootJar> {
    enabled = true
    mainClass.set("sgc.Sgc")
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        events("skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
        showStackTraces = true
        showCauses = true
        showStandardStreams = false
        stackTraceFilters = setOf(TestStackTraceFilter.ENTRY_POINT)
    }

    jvmArgs = listOf(
        "-Dspring.jpa.show-sql=false",
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
            logger.warn("byte-buddy-agent nao encontrado. Avisos do Mockito podem continuar aparecendo.")
        }
    }
}

tasks.withType<JavaCompile> {
    options.apply {
        isIncremental = true
        isFork = true
        encoding = "UTF-8"
        compilerArgs.add("-Xlint:deprecation")
    }
}