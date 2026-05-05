import com.github.gradle.node.pnpm.task.PnpmTask

plugins {
    base
    alias(libs.plugins.node)
}

node {
    // Usa o Node e PNPM instalados no sistema (via nvm no Linux)
    download.set(false)
}

tasks.register<PnpmTask>("install") {
    group = "setup"
    description = "Instala as dependências do frontend (pnpm install)"
    pnpmCommand.set(listOf("install", "--frozen-lockfile"))
    inputs.file("package.json")
    inputs.file("pnpm-lock.yaml")
    outputs.dir("node_modules")
}

tasks.register<PnpmTask>("dev") {
    group = "application"
    description = "Inicia o servidor de desenvolvimento do frontend (Vite)"
    dependsOn("install")
    pnpmCommand.set(listOf("run", "dev"))
}

tasks.register<PnpmTask>("buildVue") {
    group = "build"
    description = "Gera o build de produção do frontend Vue"
    dependsOn("install")

    // Otimização: Só roda se houver mudanças nestes arquivos/pastas
    inputs.dir("src")
    inputs.file("index.html")
    inputs.file("package.json")
    inputs.file("tsconfig.json")
    inputs.file("vite.config.ts")
    outputs.dir("dist")

    pnpmCommand.set(listOf("run", "build"))
}

tasks.register<PnpmTask>("quality") {
    group = "verification"
    description = "Executa verificações de qualidade do frontend (lint, tests, typecheck)"
    dependsOn("install")
    pnpmCommand.set(listOf("run", "quality:all"))
    ignoreExitValue.set(true)
}

tasks.register<PnpmTask>("test") {
    group = "verification"
    description = "Executa apenas os testes do frontend"
    dependsOn("install")
    pnpmCommand.set(listOf("run", "quality:test"))
    ignoreExitValue.set(true)
}

// Estende a task padrão 'clean' do Gradle
tasks.named<Delete>("clean") {
    delete("dist", "coverage")
}

// Faz com que o 'build' padrão do Gradle execute o 'buildVue'
tasks.named("build") {
    dependsOn("buildVue")
}

// Faz com que o 'check' padrão do Gradle execute o 'quality'
tasks.named("check") {
    dependsOn("quality")
}
