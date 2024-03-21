plugins {
    kotlin("jvm") version "1.9.0"
    antlr
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    antlr("org.antlr:antlr4:4.13.1")
}

sourceSets {
    main {
        kotlin.srcDir(file("src/main"))
        java.srcDir(file("src/gen"))
        antlr.srcDir("src/main/antlr")
    }
}

val antlrGeneratedDir = file("${projectDir}/src/gen/adventure")

tasks {
    generateGrammarSource {
        outputDirectory = outputDirectory.resolve(antlrGeneratedDir)
        arguments = arguments + listOf("-package", "adventure")
    }
    compileKotlin {
        dependsOn(generateGrammarSource)
    }
    generateTestGrammarSource {
        outputDirectory = outputDirectory.resolve(antlrGeneratedDir)
        arguments = arguments + listOf("-package", "adventure")
    }
    compileTestKotlin {
        dependsOn(generateTestGrammarSource)
    }
    jar {
        manifest.attributes["Main-Class"] = "MainKt"
        val dependencies = configurations
            .runtimeClasspath
            .get()
            .map(::zipTree)
        from(dependencies)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    clean {
        delete(antlrGeneratedDir)
    }
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}