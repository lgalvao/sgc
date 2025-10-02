import org.gradle.api.tasks.Exec

tasks.register<Exec>("dev") {
    workingDir = project.projectDir
    commandLine = if (System.getProperty("os.name").lowercase().contains("win")) listOf("cmd", "/c", "npm", "run", "dev") else listOf("npm", "run", "dev")
}

tasks.register<Exec>("buildVue") {
    workingDir = project.projectDir
    commandLine = if (System.getProperty("os.name").lowercase().contains("win")) listOf("cmd", "/c", "npm", "run", "build") else listOf("npm", "run", "build")
}
