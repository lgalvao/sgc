val isWindows = System.getProperty("os.name").lowercase().contains("win")

// Auxiliar para comandos NPM
fun npmCommand(vararg args: String) = if (isWindows) {
    listOf("cmd", "/c", "npm") + args
} else {
    listOf("npm") + args
}

tasks.register<Exec>("install") {
    group = "setup"
    description = "Instala as dependências do frontend (npm install)"
    workingDir = projectDir
    inputs.file("package.json")
    inputs.file("package-lock.json")
    commandLine = npmCommand("install")
}

tasks.register<Exec>("dev") {
    group = "application"
    description = "Inicia o servidor de desenvolvimento do frontend (Vite)"
    dependsOn("install")
    workingDir = projectDir
    commandLine = npmCommand("run", "dev")
}

tasks.register<Exec>("buildVue") {
    group = "build"
    description = "Gera o build de produção do frontend Vue"
    dependsOn("install")
    workingDir = projectDir

    // Otimização: Só roda se houver mudanças nestes arquivos/pastas
    inputs.dir("src")
    inputs.file("index.html")
    inputs.file("package.json")
    inputs.file("tsconfig.json")
    inputs.file("vite.config.ts")
    outputs.dir("dist")

    commandLine = npmCommand("run", "build")
}

tasks.register<Exec>("quality") {
    group = "verification"
    description = "Executa verificações de qualidade do frontend (lint, tests, typecheck)"
    dependsOn("install")
    workingDir = projectDir
    commandLine = npmCommand("run", "quality:all")
    isIgnoreExitValue = true
}

tasks.register<Exec>("test") {
    group = "verification"
    description = "Executa apenas os testes do frontend"
    dependsOn("install")
    workingDir = projectDir
    commandLine = npmCommand("run", "quality:test")
    isIgnoreExitValue = true
}

// Estende a task padrão 'clean' do Gradle (se o plugin base/java estiver aplicado)
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
