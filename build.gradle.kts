plugins {
    kotlin("jvm") version "1.9.0"
    //antlr
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.antlr:antlr4-runtime:4.10.1")
}

sourceSets {
    main {
        kotlin.srcDir(file("src/main"))
        java.srcDir(file("src/gen"))
    }
}

tasks.jar {
    manifest.attributes["Main-Class"] = "MainKt"
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree)
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
    useJUnitPlatform()
}

//Gradle refuses to handle the source generation
//Use the Antlr plugin inside IntelliJ instead

//val antlrGeneratedDir = file("${buildDir}/src/gen/")
//val antlrGrammar = file("${buildDir}/src/main/antlr/Adventure.g4")

/*
tasks.withType<AntlrTask> {
    outputDirectory = antlrGeneratedDir
    arguments = listOf("-package", "adventure", "-o", antlrGeneratedDir.toString(), antlrGrammar.toString())
}
*/
/*
tasks.register<AntlrTask>("generateGrammarSource") {
    outputDirectory = antlrGeneratedDir
    arguments = listOf("-package", "adventure", "-o", antlrGeneratedDir.toString(), antlrGrammar.toString())
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateGrammarSource")
    source(antlrGeneratedDir)
}
*/

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}