import com.example.AdventureBaseListener
import com.example.AdventureParser
import com.example.AdventureParser.ExpressionContext
import com.example.AdventureParser.UnnamedEventContext
import com.example.AdventureParser.StatsBlockContext

class KotlinGeneratorListener(
    val output: StringBuilder,
) : SemanticAnalyzingListener() {
    private var indentLength = 0
    private fun indent() {
        output.append("\n")
        output.append("".padEnd(indentLength, '\t'))
    }

    private fun realign() {
        //output.append("\r" + "".padEnd(indentLength, '\t')) //CR only works in console
        val lastNewline = output.lastIndexOf('\n').coerceAtLeast(0)
        if (output.substring(lastNewline).isBlank()) {
            output.delete(lastNewline, output.length)
            indent()
        } else {
            throw IllegalStateException("Error in generator: attempted to delete non-empty line")
            //indent()
            //realign()
        }
    }

    private val statsVariables = mutableListOf<String>()
    private fun generateSaveStruct() {
        var inputLinesCnt = 3 //to generate file read logic
        val code = """
            var lastSave: SaveStruct = SaveStruct.save(introduction)
            class SaveStruct(
                val location: Location,
                val storyState: List<String>,
                val inventory: List<Item>,
                ${statsVariables.joinToString(",\n                ") { "val " + it + symbolTable.getSymbolType(it).kotlinName }}
            ) {
                fun writeToFile(): String {
                    return location::class.simpleName + "\n" + 
                        storyState.joinToString(" ") + "\n" +
                        inventory.joinToString(" ") + "\n" +
                        ${
                            statsVariables.map {
                                return@map when (symbolTable.getSymbolType(it)) {
                                    SymbolTable.ExpressionType.INT -> it
                                    SymbolTable.ExpressionType.STRING -> it
                                    SymbolTable.ExpressionType.BOOL -> it
                                    SymbolTable.ExpressionType.LOCATION -> "$it::class.simpleName"
                                    SymbolTable.ExpressionType.ITEM -> "$it::class.simpleName"
                                    SymbolTable.ExpressionType.EVENT -> TODO()
                                    SymbolTable.ExpressionType.UNDEFINED -> TODO()
                                }
                            }.joinToString(" + \"\\n\" +\n                        ")
                        }
                }
                companion object {
                    fun load() {
                        clearedStoryEvents.clear()
                        for (event in lastSave.storyState.reversed()) {
                            clearedStoryEvents.push(event)
                        }
                        inventory.clear()
                        inventory.addAll(lastSave.inventory)
                        ${statsVariables.map { "$it = lastSave.$it" }.joinToString("\n                \t\t")}
                        throw LocationChangeException(lastSave.location)
                    }

                    fun save(location: Location):SaveStruct {
                        return SaveStruct(
                            location,
                            clearedStoryEvents.toMutableList(),
                            inventory.toMutableList(),
                            ${statsVariables.joinToString(",\n                \t\t\t")}
                        )
                    }
                    
                    val locationsByName: Map<String, Location> = mapOf(${
                        symbolTable.symbolTable.last()
                            .filter { it.value == SymbolTable.ExpressionType.LOCATION }
                            .map { "\"${it.key}\" to ${it.key}," }
                            .joinToString(" ")
                    })
                    val itemsByName: Map<String, Item> = mapOf(${
                        symbolTable.symbolTable.last()
                            .filter { it.value == SymbolTable.ExpressionType.ITEM }
                            .map { "\"${it.key}\" to ${it.key}," }
                            .joinToString(" ")
                    })
                    
                    fun readFromFile(lines: List<String>): SaveStruct {
                        return SaveStruct(
                            locationsByName[lines[0]]!!,
                            lines[1].split(' '),
                            lines[2].split(' ').map { itemsByName[it]!! },
                            ${
                                statsVariables.map {
                                    return@map when(symbolTable.getSymbolType(it)) {
                                        SymbolTable.ExpressionType.INT -> "lines[${inputLinesCnt++}].toInt(),"
                                        SymbolTable.ExpressionType.STRING -> "lines[${inputLinesCnt++}],"
                                        SymbolTable.ExpressionType.BOOL -> "lines[${inputLinesCnt++}].toBoolean(),"
                                        SymbolTable.ExpressionType.LOCATION -> "locationsByName[lines[${inputLinesCnt++}]]!!,"
                                        SymbolTable.ExpressionType.ITEM -> "itemsByName[lines[${inputLinesCnt++}]]!!,"
                                        SymbolTable.ExpressionType.EVENT -> TODO()
                                        SymbolTable.ExpressionType.UNDEFINED -> TODO()
                                    }
                                }.joinToString ("\n                            ")
                            }
                        )
                    }
                }
            }
        """.trimIndent()
        code.lines().forEach {
            output.append(it)
            indent()
        }
    }

    override fun enterAdventure(ctx: AdventureParser.AdventureContext?) {
        super.enterAdventure(ctx)
        output.append("@file:Suppress(\"NAME_SHADOWING\", \"REDUNDANT_ANONYMOUS_FUNCTION\", \"ClassName\", \"RedundantLambdaOrAnonymousFunction\", \"CanBeVal\", \"RedundantExplicitType\", \"RemoveSingleExpressionStringTemplate\", \"UNUSED_PARAMETER\", \"ReplaceWithOperatorAssignment\", \"MemberVisibilityCanBePrivate\", \"SpellCheckingInspection\", \"UNUSED_ANONYMOUS_PARAMETER\", \"MayBeConstant\")\n")
        indent()
        output.append("import java.util.Stack")
        indent()
        output.append("import java.io.File")
        indent()
        indent()
        output.append("object Generated {")
        indentLength++
        indent()
        val boilerplate = """
                // FRAMEWORK CODE
                //<editor-fold desc="Framework code">
                interface Location {
                    val here get() = this
                    fun execute()
                }

                class LocationChangeException(val newLocation: Location) : Exception()
                
                class GameOverException : Exception()

                fun end(): Nothing = throw GameOverException()
                
                fun goto(where: Location): Nothing = throw LocationChangeException(where)
                
                val clearedStoryEvents = Stack<String>()
                
                fun displayText(text: String, keepGoing: Boolean = false, vanishing: Boolean = false) {
                    val placeholder = "(...)\r"
                    val waitBetweenLines:Long = 1000
                    if (!keepGoing && !vanishing) {
                        //print the text, wait for [enter]
                        print(text)
                        readln()
                    }
                    if (keepGoing && !vanishing) {
                        //print the text, and a newline
                        println(text)

                        //print a placeholder, and move cursor to the beginning
                        print(placeholder)
                        //Thread.sleep(waitBetweenLines)
                    }
                    //
                    if (!keepGoing && vanishing) {
                        //print the text, and move cursor to the beginning
                        print(text + "\r")

                        //make text disappear
                        Thread.sleep(waitBetweenLines)
                        print(" ")

                        //wait for [enter]
                        readln()
                    }
                    if (keepGoing && vanishing) {
                        //print the text, and move cursor to the beginning
                        print(text + "\r")
                        Thread.sleep(waitBetweenLines)
                    }
                }
                
                @Suppress("unused")
                fun executeStory() {
                    var here : Location = introduction
                    while (true) {
                        try {
                            here.execute()
                            return
                        }
                        catch (ex : LocationChangeException) {
                            here = ex.newLocation
                        }
                        catch (ex: GameOverException) {
                            println(">>>GAME OVER")
                            print("Would you like to restart? y/N")
                            if (readln() == "y") {
                                here = introduction
                                continue
                            }
                            else {
                                return
                            }
                        }
                    }
                }
                
                fun max(int1: Int, int2: Int) = int1.coerceAtLeast(int2)
                
                fun after(storyEvent: String) = clearedStoryEvents.contains(storyEvent)
                
                interface Item {
                    val description: String
                    fun use() { }
                    fun equip() { }
                    fun unequip() { }
                }
                fun has_item(item: Item) = inventory.contains(item)
                val inventory = mutableListOf<Item>() 
                
                fun showItemsMenu() {
                    while (true) {
                        val itemMap = (0..<inventory.size).associateWith { inventory[it] }
                        println("Choose an action and an item, such as: info 15")
                        println("Available actions: use, info, drop, exit")
                        println("Your items:")
                        itemMap.forEach {
                            println("${'$'}{it.key} ${'$'}{it.value::class.simpleName}")
                        }
                        print("Your choice : ")
                        try {
                            val choice = readln().split(' ')
                            when (choice[0]) {
                                "exit" -> return
                                "use" -> itemMap[choice[1].toInt()]!!.use()
                                "info" -> println(itemMap[choice[1].toInt()]!!.description)
                                "drop" -> {
                                    val item = itemMap[choice[1].toInt()]!!
                                    inventory.remove(item)
                                    println("You dropped the ${'$'}{item::class.simpleName}")
                                }
                            }
                        } catch (_: Exception) {
                            println("No such choice exists")
                        }
                    }
                }
                fun showItemsSubmenu() : Boolean {
                    val itemMap = (0..<inventory.size).associateWith { inventory[it] }
                    println("Your items:")
                    itemMap.forEach {
                        println("${'$'}{it.key} ${'$'}{it.value::class.simpleName}")
                    }
                    while (true) {
                        try {
                            println("Choose an item to use (-1 to quit): ")
                            val choice = readln().toInt()
                            if (choice != -1) {
                                itemMap[choice]!!.use()
                                return true
                            } else {
                                return false
                            }
                        } catch (_: Exception) {
                            println("No such choice exists")
                        }
                    }
                }
                
                fun allTrue(vararg args:Boolean):Boolean = args.all { it }
                fun input_text(message: String):String { print(message) ; return readln() }
                
                fun showMainMenu() {
                    while (true) {
                        val choiceMap = mapOf(
                            0 to "Quit",
                            1 to "Back to game",
                            2 to "Load save from file",
                            3 to "Write save to file",
                        )
                        println(">>>CHOICES")
                        choiceMap.entries.forEach {
                            println("    " + it.key + ") " + it.value)
                        }
                        print(">>>Your choice: ")
                        val choiceNum = try { readln().toInt() } catch (_:NumberFormatException) { -1 }
                        when (choiceMap[choiceNum]) {
                            "Quit" -> {
                                print("Are you sure? (y/N)")
                                if (readln() == "y") end()
                            }
                            "Back to game" -> return
                            "Load save from file" ->  {
                                loadDialog() 
                            }
                            "Write save to file" ->  {
                                saveDialog() 
                            }
                            else -> {
                                println(">>>No such choice exists")
                            }
                        }
                    }
                }
                fun loadDialog() {
                    println("Files in current directory:")
                    val filesInCurrentDir = File(".").listFiles() ?: emptyArray()
                    filesInCurrentDir.forEach {
                        println(it.name)
                    }
                    while (true) {
                        try {
                            print("File to load (empty to quit): ")
                            val filename = readln()
                            if (filename == "") break
                            if (!filesInCurrentDir.contains(File(filename))) {
                                print("This file des not exist.")
                            }
                            lastSave = SaveStruct.readFromFile(File(filename).readLines())
                            SaveStruct.load()
                        } catch(_: Exception) {
                            println("Could not load save.")
                        }
                    }
                }
                fun saveDialog() {
                    println("Files in current directory:")
                    val filesInCurrentDir = File(".").listFiles() ?: emptyArray()
                    filesInCurrentDir.forEach {
                        println(it.name)
                    }
                    while (true) {
                        try {
                            print("Name of new file (empty to quit): ")
                            val filename = readln()
                            if (filename == "") break
                            if (filesInCurrentDir.contains(File(filename))) {
                                print("This file already exists. Overwirte? (y/N) ")
                                if (readln() != "y") {
                                    println("Not saving")
                                    continue
                                }
                            }
                            
                            File(filename).writeText(lastSave.writeToFile())
                        } catch(_: Exception) {
                            println("Could not create save")
                        }
                    }
                }
                
                //</editor-fold>
        """.trimIndent()
        boilerplate.lines().forEach {
            output.append(it)
            indent()
        }
        indent()
        output.append("// GENERATED FROM SOURCE")
        indent()
        output.append("//<editor-fold desc=\"Generated from source\">")
        indent()

        if (ctx!!.children.filterIsInstance<StatsBlockContext>().isEmpty()) {
            generateSaveStruct()
            indent()
        }
    }

    override fun exitAdventure(ctx: AdventureParser.AdventureContext?) {
        super.exitAdventure(ctx)
        output.append("//</editor-fold>")
        indent()
        indentLength--
        realign()
        output.append("}// adventure end\n")
        indent()
    }

    override fun enterVariable(ctx: AdventureParser.VariableContext?) {
        super.enterVariable(ctx)
        val varName = ctx?.ID()?.text!!
        val typeOfInitializer = symbolTable.getSymbolType(varName)
        val typeOfVariable:String = typeOfInitializer.kotlinName
        val initializer = ctx.expression()?.text
        output.append("var $varName$typeOfVariable = $initializer")
        indent()
    }

    override fun exitVariable(ctx: AdventureParser.VariableContext?) {
        super.exitVariable(ctx)
        //do nothing
    }

    override fun enterIntroduction(ctx: AdventureParser.IntroductionContext?) {
        super.enterIntroduction(ctx)
        output.append("object introduction : Location {")
        indentLength++
        indent()
        output.append("override fun execute() {")
        indentLength++
        indent()
    }

    override fun exitIntroduction(ctx: AdventureParser.IntroductionContext?) {
        super.exitIntroduction(ctx)
        indentLength -= 2
        realign()
        val footers = """
                  }//execute
              }//introduction
              """.trimIndent()
        footers.lines().forEach {
            output.append(it)
            indent()
        }
    }

    override fun enterLocation(ctx: AdventureParser.LocationContext?) {
        super.enterLocation(ctx)
        output.append("object ${ctx?.ID()?.text} : Location {")
        indentLength++
        indent()
        output.append("override fun execute() {")
        indentLength++
        indent()
    }

    override fun exitLocation(ctx: AdventureParser.LocationContext?) {
        super.exitLocation(ctx)
        indentLength -= 2
        realign()
        val footers = """
                  }//execute
              }//location
              """.trimIndent()
        footers.lines().forEach {
            output.append(it)
            indent()
        }
    }

    override fun enterNamedEvent(ctx: AdventureParser.NamedEventContext?) {
        super.enterNamedEvent(ctx)
        if (ctx?.STORY() != null) {
            output.append("val ${ctx.ID().text} = \"${ctx.ID().text}\"")
            indent()
        }
        output.append("fun ${ctx?.ID()?.text}Event(here: Location) {")
        indentLength++
        indent()

        //handle story events
        //there is no conditions block here
        //handle story event where there are no conditions
        if (ctx!!.STORY() != null) {
            output.append("if(clearedStoryEvents.contains(${ctx.ID().text})) return")
            indent()
            output.append("clearedStoryEvents.push(${ctx.ID().text})")
            indent()
        }
    }

    override fun exitNamedEvent(ctx: AdventureParser.NamedEventContext?) {
        super.exitNamedEvent(ctx)
        indentLength--
        realign()
        output.append("}//event")
        indent()
    }

    override fun enterUnnamedEvent(ctx: UnnamedEventContext?) {
        super.enterUnnamedEvent(ctx)
        unnamedEventCounter++
        output.append("(fun (here: Location) {")
        indentLength++
        indent()

        //handle story event where there are no conditions
        if (ctx!!.STORY() != null && ctx.conditionsBlock() == null) {
            output.append("if(clearedStoryEvents.contains(\"@unnamedEvent$unnamedEventCounter\")) return")
            indent()
            output.append("clearedStoryEvents.push(\"@unnamedEvent$unnamedEventCounter\")")
            indent()
        }
    }

    override fun exitUnnamedEvent(ctx: UnnamedEventContext?) {
        super.exitUnnamedEvent(ctx)
        //handle the indent created by conditions block
        if (ctx?.conditionsBlock() != null) {
            indentLength--
            realign()
            output.append("}")
            indent()
        }
        indentLength--
        realign()
        output.append("}) (here)")
        indent()
    }

    override fun enterStatement(ctx: AdventureParser.StatementContext?) {
        super.enterStatement(ctx)
        //do nothing
    }

    override fun exitStatement(ctx: AdventureParser.StatementContext?) {
        super.exitStatement(ctx)
        //do nothing
    }

    override fun enterAssignment(ctx: AdventureParser.AssignmentContext?) {
        super.enterAssignment(ctx)
        output.append(ctx?.ID()?.text + " = " + ctx?.expression()?.text)
        indent()
    }

    override fun exitAssignment(ctx: AdventureParser.AssignmentContext?) {
        super.exitAssignment(ctx)
        //do nothing
    }

    override fun enterBranch(ctx: AdventureParser.BranchContext?) {
        super.enterBranch(ctx)
        //do nothing, if() will be produced by conditions block
    }

    override fun exitBranch(ctx: AdventureParser.BranchContext?) {
        super.exitBranch(ctx)
        //handle the indent created by conditions block
        if (ctx?.conditionsBlock() != null) {
            indentLength--
            realign()
            output.append("}//if")
        }
        indent()
    }

    var unnamedEventCounter = 0
    override fun enterConditionsBlock(ctx: AdventureParser.ConditionsBlockContext?) {
        super.enterConditionsBlock(ctx)
        output.append("if (allTrue(")
        indentLength++
        indent()

        if (ctx!!.parent is UnnamedEventContext) {
            val theEvent = ctx.parent as UnnamedEventContext
            if (theEvent.STORY() != null) {
                output.append("!clearedStoryEvents.contains(\"@unnamedEvent$unnamedEventCounter\"),")
                indent()
            }
        }

        ctx.children.filterIsInstance<ExpressionContext>().map {
            if (it.text.startsWith("@[") && it.text.endsWith("]@")) {
                "(" + it.text.drop(2).dropLast(2) + ")"
            } else {
                it.text
            }
        }.forEach {
            output.append("$it,")
            indent()
        }
    }

    override fun exitConditionsBlock(ctx: AdventureParser.ConditionsBlockContext?) {
        super.exitConditionsBlock(ctx)
        indentLength--
        realign()
        output.append(")) {")
        indentLength++
        indent()

        if (ctx!!.parent is UnnamedEventContext) {
            val theEvent = ctx.parent as UnnamedEventContext
            if (theEvent.STORY() != null) {
                output.append("clearedStoryEvents.push(\"@unnamedEvent$unnamedEventCounter\")")
                indent()
            }
        }
    }

    override fun enterChoicesBlock(ctx: AdventureParser.ChoicesBlockContext?) {
        super.enterChoicesBlock(ctx)
        val allTheChoices = ctx?.children?.filterIsInstance<AdventureParser.ChoiceContext>()?.map { it.STRING().text }
        val choiceMap = (0..<allTheChoices!!.size).associate { (it + 1) to allTheChoices[it] }
        output.append("while (true) {")
        indentLength++
        indent()
        output.append("val choiceMap = mapOf(")
        indentLength++
        for (choice in choiceMap) {
            indent() ; output.append("${choice.key} to ${choice.value},")
        }
        indentLength-- ; indent() ; output.append(")") //map
        indent() ; output.append("println(\">>>CHOICES\")")
        indent() ; output.append("choiceMap.entries.forEach {") ; indentLength++
        indent() ; output.append("println(\"    \" + it.key + \") \" + it.value)") ; indentLength--
        indent() ; output.append("}")
        indent() ; output.append("print(\">>>Your choice: \")")
        indent() ; output.append("val choiceNum = try { readln().toInt() } catch (_:NumberFormatException) { -1 }")
        indent() ; output.append("when (choiceMap[choiceNum]) {") ; indentLength++
        indent()
    }

    override fun exitChoicesBlock(ctx: AdventureParser.ChoicesBlockContext?) {
        super.exitChoicesBlock(ctx)
        if (ctx?.afterChoice() == null)
        {
            output.append("else -> {"); indentLength++
            indent(); output.append("println(\">>>No such choice exists\")"); indentLength--
            indent(); output.append("}//else") //else
            indentLength--
            indent(); output.append("}//when") //when
            indent()
        }
        indentLength--
        realign()
        output.append("}//while") //while
        indent()
    }
    override fun enterAfterChoice(ctx: AdventureParser.AfterChoiceContext?) {
        super.enterAfterChoice(ctx)
        output.append("else -> {"); indentLength++
        indent(); output.append("println(\">>>No such choice exists\")"); indentLength--
        indent(); output.append("}//else") //else
        indentLength--
        indent(); output.append("}//when") //when
        indent()
    }

    override fun exitAfterChoice(ctx: AdventureParser.AfterChoiceContext?) {
        super.exitAfterChoice(ctx)
        //do nothing
    }
    override fun enterChoice(ctx: AdventureParser.ChoiceContext?) {
        super.enterChoice(ctx)
        output.append("${ctx?.STRING()} -> ")
    }

    override fun exitChoice(ctx: AdventureParser.ChoiceContext?) {
        super.exitChoice(ctx)
        //all valid statements indent() after themselves
    }

    override fun enterStatementBlock(ctx: AdventureParser.StatementBlockContext?) {
        super.enterStatementBlock(ctx)
        output.append(" {")
        indentLength++
        indent()
    }

    override fun exitStatementBlock(ctx: AdventureParser.StatementBlockContext?) {
        super.exitStatementBlock(ctx)
        indentLength--
        realign()
        output.append("}//block")
        indent()
    }

    override fun enterJumpLocation(ctx: AdventureParser.JumpLocationContext?) {
        super.enterJumpLocation(ctx)
        output.append("goto(" + ctx?.ID()?.text + ")")
    }

    override fun exitJumpLocation(ctx: AdventureParser.JumpLocationContext?) {
        super.exitJumpLocation(ctx)
        indent()
    }

    override fun enterTriggerEvent(ctx: AdventureParser.TriggerEventContext?) {
        super.enterTriggerEvent(ctx)
        output.append(ctx?.ID()?.text + "Event(here)")
    }

    override fun exitTriggerEvent(ctx: AdventureParser.TriggerEventContext?) {
        super.exitTriggerEvent(ctx)
        indent()
    }

    override fun enterPrint(ctx: AdventureParser.PrintContext?) {
        super.enterPrint(ctx)
        if (ctx!!.REPLACE_SIGN() == null) {
            if (ctx.CONTINUE_SIGN() == null) {
                output.append("displayText(" + ctx.STRING().text + ")")
            } else {
                output.append("displayText(" + ctx.STRING().text + ", keepGoing=true)")
            }
        } else {
            output.append("displayText(" + ctx.STRING().text + ", vanishing=true, keepGoing=true)")
        }
    }

    override fun exitPrint(ctx: AdventureParser.PrintContext?) {
        super.exitPrint(ctx)
        indent()
    }

    override fun enterFinishEvent(ctx: AdventureParser.FinishEventContext?) {
        super.enterFinishEvent(ctx)
        output.append("return")
    }

    override fun exitFinishEvent(ctx: AdventureParser.FinishEventContext?) {
        super.exitFinishEvent(ctx)
        indent()
    }

    override fun enterEndStory(ctx: AdventureParser.EndStoryContext?) {
        super.enterEndStory(ctx)
        output.append("end()")
    }

    override fun exitEndStory(ctx: AdventureParser.EndStoryContext?) {
        super.exitEndStory(ctx)
        indent()
    }

    override fun enterUntriggerEvent(ctx: AdventureParser.UntriggerEventContext?) {
        super.enterUntriggerEvent(ctx)
        output.append("clearedStoryEvents.pop()")
    }

    override fun exitUntriggerEvent(ctx: AdventureParser.UntriggerEventContext?) {
        super.exitUntriggerEvent(ctx)
        indent()
    }

    override fun enterCodeInjection(ctx: AdventureParser.CodeInjectionContext?) {
        super.enterCodeInjection(ctx)
        val injectedCode = ctx!!.CODE_INJECTION().text.drop(2).dropLast(2).trimIndent()
        if (injectedCode.contains('\r') || injectedCode.contains('\n')) {
            val lines = injectedCode.lines()
            lines.forEach {
                output.append(it)
                indent()
            }
        } else {
            output.append(injectedCode)
            indent()
        }
    }

    override fun exitCodeInjection(ctx: AdventureParser.CodeInjectionContext?) {
        super.exitCodeInjection(ctx)
        //do nothing
    }

    override fun enterLoadGame(ctx: AdventureParser.LoadGameContext?) {
        super.enterLoadGame(ctx)
        output.append("SaveStruct.load()")
    }

    override fun exitLoadGame(ctx: AdventureParser.LoadGameContext?) {
        super.exitLoadGame(ctx)
        indent()
    }

    override fun enterSaveGame(ctx: AdventureParser.SaveGameContext?) {
        super.enterSaveGame(ctx)
        output.append("SaveStruct.save(here)")
    }

    override fun exitSaveGame(ctx: AdventureParser.SaveGameContext?) {
        super.exitSaveGame(ctx)
        indent()
    }

    override fun enterStatsBlock(ctx: StatsBlockContext?) {
        super.enterStatsBlock(ctx)
        statsVariables.addAll(ctx!!.ID().map { it.text })
        generateSaveStruct()
    }

    override fun exitStatsBlock(ctx: StatsBlockContext?) {
        super.exitStatsBlock(ctx)
        indent()
    }

    override fun enterConsumeItem(ctx: AdventureParser.ConsumeItemContext?) {
        super.enterConsumeItem(ctx)
        output.append("inventory.remove(this)")
    }

    override fun exitConsumeItem(ctx: AdventureParser.ConsumeItemContext?) {
        super.exitConsumeItem(ctx)
        indent()
    }

    override fun enterEquipItem(ctx: AdventureParser.EquipItemContext?) {
        super.enterEquipItem(ctx)
        output.append(ctx!!.EQUIP().text + "()")
    }

    override fun exitEquipItem(ctx: AdventureParser.EquipItemContext?) {
        super.exitEquipItem(ctx)
        indent()
    }

    override fun enterUnequipItem(ctx: AdventureParser.UnequipItemContext?) {
        super.enterUnequipItem(ctx)
        output.append(ctx!!.UNEQUIP().text + "()")
    }

    override fun exitUnequipItem(ctx: AdventureParser.UnequipItemContext?) {
        super.exitUnequipItem(ctx)
        indent()
    }

    override fun enterGetItem(ctx: AdventureParser.GetItemContext?) {
        super.enterGetItem(ctx)
        output.append("inventory.add(${ctx!!.ID().text})")
    }

    override fun exitGetItem(ctx: AdventureParser.GetItemContext?) {
        super.exitGetItem(ctx)
        indent()
    }

    override fun enterBuiltinMax(ctx: AdventureParser.BuiltinMaxContext?) {
        super.enterBuiltinMax(ctx)
        //do nothing, this is not a statement
    }

    override fun exitBuiltinMax(ctx: AdventureParser.BuiltinMaxContext?) {
        super.exitBuiltinMax(ctx)
        //do nothing, this is not a statement
    }

    override fun enterAfterEvent(ctx: AdventureParser.AfterEventContext?) {
        super.enterAfterEvent(ctx)
        //do nothing, handled by framework
    }

    override fun exitAfterEvent(ctx: AdventureParser.AfterEventContext?) {
        super.exitAfterEvent(ctx)
        //do nothing, handled by framework
    }

    override fun enterHasItem(ctx: AdventureParser.HasItemContext?) {
        super.enterHasItem(ctx)
        //do nothing, handled by framework
    }

    override fun exitHasItem(ctx: AdventureParser.HasItemContext?) {
        super.exitHasItem(ctx)
        //do nothing, handled by framework
    }

    override fun enterInputText(ctx: AdventureParser.InputTextContext?) {
        super.enterInputText(ctx)
        //do nothing, handled by framework
    }

    override fun exitInputText(ctx: AdventureParser.InputTextContext?) {
        super.exitInputText(ctx)
        //do nothing, handled by framework
    }

    override fun enterInventoryBlock(ctx: AdventureParser.InventoryBlockContext?) {
        super.enterInventoryBlock(ctx)
        output.append("init {")
        indentLength++
        indent()
        ctx!!.ID().forEach {
            output.append("inventory.add(${it.text})")
            indent()
        }
    }

    override fun exitInventoryBlock(ctx: AdventureParser.InventoryBlockContext?) {
        super.exitInventoryBlock(ctx)
        indentLength--
        realign()
        output.append("}//inventory")
        indent()
    }

    override fun enterItem(ctx: AdventureParser.ItemContext?) {
        super.enterItem(ctx)
        output.append("object ${ctx!!.ID().text} : Item {")
        indentLength++
        indent()
        output.append("override val description = " + ctx.STRING())
        indent()
    }

    override fun exitItem(ctx: AdventureParser.ItemContext?) {
        super.exitItem(ctx)
        indentLength--
        realign()
        output.append("}//item")
        indent()
    }
    override fun enterItemFunction(ctx: AdventureParser.ItemFunctionContext?) {
        super.enterItemFunction(ctx)
        val functionName = ctx!!.start.text
        if (listOf("use", "equip", "unequip").contains(functionName)) {
            output.append("override ")
        }
        output.append("fun $functionName()")
        if (ctx.statement() != null) {
            output.append(" = ")
        }
    }

    override fun exitItemFunction(ctx: AdventureParser.ItemFunctionContext?) {
        super.exitItemFunction(ctx)
        //do nothing
    }

    override fun enterReplaceItem(ctx: AdventureParser.ReplaceItemContext?) {
        super.enterReplaceItem(ctx)
        val varName = ctx!!.ID().text
        output.append("$varName.unequip()")
        indent()
        output.append("inventory.add($varName)")
        indent()
        output.append("$varName = this")
        indent()
        output.append("inventory.remove(this)")
    }

    override fun exitReplaceItem(ctx: AdventureParser.ReplaceItemContext?) {
        super.exitReplaceItem(ctx)
        indent()
    }

    override fun enterItemsSubmenu(ctx: AdventureParser.ItemsSubmenuContext?) {
        super.enterItemsSubmenu(ctx)
        output.append("if( !showItemsSubmenu() ) continue")
    }

    override fun exitItemsSubmenu(ctx: AdventureParser.ItemsSubmenuContext?) {
        super.exitItemsSubmenu(ctx)
        indent()
    }
}
