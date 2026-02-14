tasks.register<Exec>("dev") {
    group = "application"
    description = "Inicia o servidor de desenvolvimento do frontend (Vite)"
    workingDir = project.projectDir
    commandLine = if (System.getProperty("os.name").lowercase().contains("win")) listOf(
        "cmd",
        "/c",
        "npm",
        "run",
        "dev"
    ) else listOf("npm", "run", "dev")
}

tasks.register<Exec>("buildVue") {
    group = "build"
    description = "Gera o build de produção do frontend Vue"
    workingDir = project.projectDir
    commandLine = if (System.getProperty("os.name").lowercase().contains("win")) listOf(
        "cmd",
        "/c",
        "npm",
        "run",
        "build"
    ) else listOf("npm", "run", "build")
}
