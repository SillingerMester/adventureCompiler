# Adventure compiler

A tool for creating games with the AdventureScript DSL.

## Build instructions

- Import project into IntelliJ Idea
- Install ANTLR plugin into the IDE
- Open the Adventure.g4 file, right click, select "Configure ANTLR"
  - set output directory to **src/gen**
  - set input directory to **src/main/antlr**
  - set package name to **adventure**
  - set language to **Java**
- Use the plugin to "Generate ANTLR recognizer"
- Select **MainKt** as the "run configuration"
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
- Download a Kotlin compiler
  - https://kotlinlang.org/docs/command-line.html
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