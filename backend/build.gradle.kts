@file:Suppress("KotlinPrintToLogpoint")

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun
import java.net.HttpURLConnection
import java.net.URI

val argumentosJvmSemAvisoUnsafe = emptyList<String>()

plugins {
    java
    jacoco
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.pitest)
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
    runtimeOnly(libs.h2)
    implementation(libs.springdoc.openapi)

    val envAmbiente = project.findProperty("ENV")?.toString()
    if (envAmbiente != "hom" && envAmbiente != "e2e") {
        developmentOnly(libs.spring.boot.devtools)
    }

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

tasks.named<ProcessResources>("processResources") {
    exclude("static/**")
}

val atualizarFrontend = tasks.register<Copy>("atualizarFrontend") {
    group = "build"
    description = "Gera o build do frontend e copia para os recursos do backend"
    dependsOn(":frontend:buildVue")
    mustRunAfter(tasks.named("processResources"))
    from(rootProject.layout.projectDirectory.dir("frontend/dist"))
    into(layout.buildDirectory.dir("resources/main/static"))
}

tasks.withType<BootJar> {
    dependsOn(atualizarFrontend)
    enabled = true
    mainClass.set("sgc.Sgc")
}

springBoot {
    buildInfo()
}

tasks.named<BootRun>("bootRun") {
    if (project.findProperty("skipFrontend")?.toString() != "true") {
        dependsOn(atualizarFrontend)
    }
    mainClass.set("sgc.Sgc")
    val env = (project.findProperty("ENV") ?: System.getProperty("spring.profiles.active"))?.toString() ?: "e2e"
    val envFile = rootProject.file(".env.$env")
    val envLocalFile = rootProject.file(".env.$env.local")
    systemProperty("spring.profiles.active", env)
    println("Perfil Spring ativado: $env")

    fun carregarConfiguracoesEnv(arquivo: File, sobrescreverExistente: Boolean) {
        println("Carregando configurações de: ${arquivo.name}")
        arquivo.useLines { lines ->
            lines.filter { it.isNotBlank() && !it.trim().startsWith("#") }
                .forEach { line ->
                    val partes = line.split("=", limit = 2)
                    if (partes.size == 2) {
                        val chave = partes[0].trim()
                        val valor = partes[1].trim()
                        val jaDefinidoNoAmbiente = environment.containsKey(chave) || System.getenv().containsKey(chave)
                        if (sobrescreverExistente || !jaDefinidoNoAmbiente) {
                            environment(chave, valor)
                        }
                    }
                }
        }
    }

    if (envFile.exists()) {
        carregarConfiguracoesEnv(envFile, false)
    } else {
        println("Arquivo .env.$env não encontrado, usando configurações padrão do application.yml")
    }

    if (envLocalFile.exists()) {
        carregarConfiguracoesEnv(envLocalFile, true)
    }

    doFirst {
        val tmpDir = layout.buildDirectory.dir("tmp").get().asFile
        if (!tmpDir.exists()) {
            tmpDir.mkdirs()
        }
        systemProperty("java.io.tmpdir", tmpDir.absolutePath)
    }
}

tasks.withType<Test> {
    ignoreFailures = false
    useJUnitPlatform()
    maxHeapSize = "2g"

    testLogging {
        events(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
        showStackTraces = true
        showCauses = true
        showStandardStreams = false
    }

    val slowTests = mutableListOf<Pair<String, Long>>()
    // Flag de depuração: mude para true localmente para exibir os 5 testes mais lentos após a execução.
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

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.register<JacocoReport>("jacocoIntegrationTestReport") {
    group = "verification"
    description = "Gera o relatório de cobertura do Jacoco exclusivamente para os testes de integração."
    dependsOn(tasks.named("integrationTest"))

    executionData.setFrom(fileTree(layout.buildDirectory).include("jacoco/integrationTest.exec"))
    classDirectories.setFrom(files(sourceSets["main"].output.classesDirs))
    sourceDirectories.setFrom(files(sourceSets["main"].allSource.srcDirs))

    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/integrationTest/html"))
    }
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

pitest {
    junit5PluginVersion.set("1.2.1")
    targetClasses.set(listOf("sgc.*.service.*"))
    targetTests.set(listOf("sgc.*"))
    threads.set(Runtime.getRuntime().availableProcessors())
    outputFormats.set(listOf("HTML", "XML"))
    timestampedReports.set(false)
    mutationThreshold.set(0)
    coverageThreshold.set(0)
}

tasks.register("fuzz") {
    group = "verification"
    description = "Executa o WuppieFuzz contra a API local do SGC de forma automatizada."
    dependsOn("bootJar")

    doLast {
        val buildDirFile = layout.buildDirectory.get().asFile
        val logFuzz = File(buildDirFile, "tmp/sgc-fuzz-backend.log")
        logFuzz.parentFile.mkdirs()

        println("-----------------------------------------------------------------")
        println("Iniciando backend do SGC em background (perfil 'e2e')...")
        println("Logs do servidor serão salvos em: ${logFuzz.absolutePath}")

        val jarFile = tasks.named<BootJar>("bootJar").get().archiveFile.get().asFile

        // Comando para levantar o JAR
        val processoBuilder = ProcessBuilder(
            "java",
            "-Dspring.profiles.active=e2e",
            "-jar",
            jarFile.absolutePath
        )

        // Define o diretório de trabalho como o diretório do subprojeto 'backend' para resolver caminhos relativos corretamente
        processoBuilder.directory(project.projectDir)

        processoBuilder.redirectOutput(logFuzz)
        processoBuilder.redirectError(logFuzz)

        val backendProcess = processoBuilder.start()
        println("Backend iniciado. Aguardando a porta 10000 ficar disponível...")

        var conectado = false
        val timeoutMs = 45000L
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                val url = URI("http://localhost:10000/swagger-ui.html").toURL()
                val conexao = url.openConnection() as HttpURLConnection
                conexao.connectTimeout = 1000
                conexao.readTimeout = 1000
                val responseCode = conexao.responseCode
                if (responseCode in 100..599) {
                    conectado = true
                    break
                }
            } catch (e: Exception) {
                // Ignora e espera a porta subir (ex: Connection Refused)
            }
            Thread.sleep(1500)
        }

        if (!conectado) {
            backendProcess.destroyForcibly()
            throw GradleException("Erro: O backend do SGC nao ficou disponivel na porta 10000 dentro de 45 segundos. Verifique os logs em: ${logFuzz.absolutePath}")
        }

        println("Backend pronto! Executando o script para obter token JWT de teste...")

        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val scriptComando = if (isWindows) {
            listOf(
                "powershell",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                "${project.rootDir}/etc/fuzzing/obter-token.ps1"
            )
        } else {
            listOf("bash", "${project.rootDir}/etc/fuzzing/obter-token.sh")
        }

        val processoToken = ProcessBuilder(scriptComando).start()
        val saidaToken = processoToken.inputStream.bufferedReader().readText()
        processoToken.waitFor()

        val regexJwt = Regex("Authorization: Bearer (\\S+)")
        val matchToken = regexJwt.find(saidaToken)

        if (matchToken == null) {
            backendProcess.destroyForcibly()
            println(saidaToken)
            throw GradleException("Erro: Falha ao obter o token JWT de teste a partir dos scripts utilitarios.")
        }

        val tokenJwt = matchToken.groupValues[1]
        println("Token JWT de teste obtido com sucesso.")

        val pastaResultados = File(buildDirFile, "reports/fuzz")
        pastaResultados.mkdirs()

        println("Iniciando o WuppieFuzz contra a API local...")
        println("Diretorio de destino dos relatorios: ${pastaResultados.absolutePath}")

        val comandoFuzzer = listOf(
            "wuppiefuzz",
            "-o", "http://localhost:10000/api-docs",
            "-h", "Authorization: Bearer $tokenJwt",
            "-d", pastaResultados.absolutePath
        )

        try {
            println("Tentando executar o WuppieFuzz nativo...")
            val processoFuzz = ProcessBuilder(comandoFuzzer)
                .inheritIO()
                .start()

            val exitCode = processoFuzz.waitFor()
            println("WuppieFuzz finalizado com codigo de saida: $exitCode")
        } catch (e: Exception) {
            println("\n[Aviso] Nao foi possivel executar o comando 'wuppiefuzz' nativo: ${e.message}")
            println("Tentando executar o WuppieFuzz via Docker (imagem 'wuppiefuzz')...")

            // Se o WuppieFuzz não estiver nativo, tenta rodar o contêiner docker usando a rede host
            val comandoDocker = listOf(
                "docker", "run", "--rm",
                "--network", "host",
                "-v", "${pastaResultados.absolutePath}:/reports",
                "wuppiefuzz",
                "-o", "http://localhost:10000/api-docs",
                "-h", "Authorization: Bearer $tokenJwt",
                "-d", "/reports"
            )

            try {
                val processoDocker = ProcessBuilder(comandoDocker)
                    .inheritIO()
                    .start()

                val exitCode = processoDocker.waitFor()
                println("WuppieFuzz (via Docker) finalizado com codigo de saida: $exitCode")
            } catch (dockerEx: Exception) {
                println("\n[Erro] Nao foi possivel executar o WuppieFuzz via Docker: ${dockerEx.message}")
                println("Certifique-se de que o Docker esta ativo e que a imagem 'wuppiefuzz' foi criada localmente.")
                println("Instrucoes completas de uso do Docker ou instalacao manual em: ${project.rootDir}/etc/fuzzing/README.md")
            }
        } finally {
            println("-----------------------------------------------------------------")
            println("Finalizando o backend do SGC local...")
            backendProcess.destroy()
            if (!backendProcess.waitFor(5, TimeUnit.SECONDS)) {
                backendProcess.destroyForcibly()
            }
            println("Porta 10000 liberada com sucesso.")
            println("-----------------------------------------------------------------")
        }
    }
}

