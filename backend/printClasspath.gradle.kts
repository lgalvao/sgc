tasks.register("printTestClasspath") {
    doLast {
        configurations["testRuntimeClasspath"].files.forEach { println(it.path) }
    }
}
