import org.gradle.api.tasks.JavaExec

val xjcOutputDir = "build/generated-sources/xjc"

tasks.register<JavaExec>("xjcGenerate") {
    group = "code generation"
    description = "Generates Java sources from XSD schemas using XJC."
    mainClass.set("com.sun.tools.xjc.XJCFacade")
    classpath = project.configurations["runtimeClasspath"]
    args = listOf(
        "-extension",
        "-d", xjcOutputDir,
        "-b", "src/main/resources/xsd/binding.xjb"
    ) + project.fileTree("src/main/resources/xsd").matching { include("**/*.xsd") }.files.map { it.path }
    doFirst {
        file(xjcOutputDir).mkdirs()
    }
}
