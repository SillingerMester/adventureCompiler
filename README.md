# Adventure compiler

A tool for creating games with the AdventureScript DSL.

## Build instructions

- Import project into IntelliJ Idea
- Select either the **MainKt** class or **gradle run** in the IDE
- To get the JAR
  - open the gradle panel from the IDE
  - execute **tasks** --> **build** --> **jar**
  - the file is **build/libs/adventureCompiler.jar**
- To run the tests
  - you will need to get a Kotlin compiler (see below)
  - place it in the project root directory
  - select **TestKt** from run configurations

## Usage

- Install java, if you haven't already
- Download a Kotlin compiler from the GitHub releases
  - https://kotlinlang.org/docs/command-line.html
  - grab a multiplatform zip, such as
    - https://github.com/JetBrains/kotlin/releases/download/v2.0.0/kotlin-compiler-2.0.0.zip
  - extract it into the project root
- Prepare a directory which contains
  - **adventureCompiler.jar** (which you've built)
  - **kotlinc** (which you've extracted)
  - **adventureCompiler** (the convenience script)
  - any game files you wish to compile
- Using the convenience script
  - adventureCompiler inFile [outFile]
  - **outFile** is optional, defaults to **Generated.kt**
  - the product is a jar, such as **Generated.kt.jar**
  - **java -jar Generated.kt.jar** runs the game
- Manually (replace bold with custom names)
  - java -jar adventureCompiler.jar **game.txt** **game.kt**
  - kotlinc/bin/kotlinc -include-runtime **game.kt** -d **game.jar**
  - jar --update --file=**game.jar** --main-class=Generated
  - java -jar **game.jar**