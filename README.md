# Adventure compiler

A tool for creating games with the AdventureScript DSL.

## Build instructions:

- Import project into IntelliJ Idea
- Install ANTLR plugin into the IDE
- Open the Adventure.g4 file, right click, select "Configure ANTLR"
  - set output directory to **src/gen**
  - set input directory to **src/main/antlr**
  - set package name to **adventure**
  - set language to **Java**
- Select MainKt as the "run configuration"
- To get the JAR
  - open the gradle panel from the IDE
  - execute **tasks** --> **build** --> **jar**
  - the file is in **build/libs**

