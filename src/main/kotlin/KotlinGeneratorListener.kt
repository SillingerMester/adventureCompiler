import com.example.AdventureBaseListener
import com.example.AdventureParser
import com.example.AdventureParser.ExpressionContext
import com.example.AdventureParser.UnnamedEventContext
import SymbolTable.ExpressionType

class KotlinGeneratorListener(
    val output: StringBuilder,
    val symbolTable: SymbolTable
) : AdventureBaseListener() {
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
        }
    }

    override fun enterAdventure(ctx: AdventureParser.AdventureContext?) {
        super.enterAdventure(ctx)
        output.append("import java.util.Stack\n")
        indent()
        output.append("object Generated {")
        indentLength++
        indent()
        val boilerplate = """
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
        """.trimIndent()
        boilerplate.lines().forEach {
            output.append(it)
            indent()
        }
        indent()
    }

    override fun exitAdventure(ctx: AdventureParser.AdventureContext?) {
        super.exitAdventure(ctx)
        indentLength--
        realign()
        output.append("}// adventure end\n")
        indent()
    }

    override fun enterVariable(ctx: AdventureParser.VariableContext?) {
        super.enterVariable(ctx)
        val varName = ctx?.ID()?.text!!
        val typeOfInitializer = symbolTable.getSymbolType(varName)
        val typeOfVariable:String = when (typeOfInitializer) {
            ExpressionType.INT -> ": Int"
            ExpressionType.STRING -> ": String"
            ExpressionType.BOOL -> ": Boolean"
            ExpressionType.LOCATION -> ":Location"
            ExpressionType.EVENT -> ""
            ExpressionType.UNDEFINED -> ": Any"
        }
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
        output.append("fun ${ctx?.ID()?.text}Event(here: Location) {")
        indentLength++
        indent()
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
        output.append("(fun (here: Location) {")
        indentLength++
        indent()

        //handle story event where there are no conditions
        if (ctx!!.EVENT() != null && ctx.conditionsBlock() == null) {
            output.append("if(clearedStoryEvents.contains(\"@storyEvent$storyEventCounter\")) return")
            indent()
            output.append("clearedStoryEvents.push(\"@storyEvent$storyEventCounter\")")
            storyEventCounter++
            indent()
        }
    }

    override fun exitUnnamedEvent(ctx: UnnamedEventContext?) {
        super.exitUnnamedEvent(ctx)
        //handle the indent created by conditions block
        if (ctx?.conditionsBlock() != null) {
            indentLength--
            indent()
            output.append("}")
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

    var storyEventCounter = 0
    override fun enterConditionsBlock(ctx: AdventureParser.ConditionsBlockContext?) {
        super.enterConditionsBlock(ctx)
        output.append("if (")
        indentLength++
        indent()

        if (ctx!!.parent is UnnamedEventContext) {
            val theEvent = ctx.parent as UnnamedEventContext
            if (theEvent.EVENT() != null) {
                output.append("!clearedStoryEvents.contains(\"@storyEvent$storyEventCounter\") &&")
                indent()
            }
        }

        ctx.children.filterIsInstance<ExpressionContext>().map {
            if (it.text.startsWith("@[") && it.text.endsWith("]@")) {
                it.text.drop(2).dropLast(2)
            } else {
                it.text
            }
        }.forEach {
            output.append("($it) &&")
            indent()
        }
        output.append("true")
        indentLength--
        indent()
    }

    override fun exitConditionsBlock(ctx: AdventureParser.ConditionsBlockContext?) {
        super.exitConditionsBlock(ctx)
        output.append(") {")
        indentLength++
        indent()

        if (ctx!!.parent is UnnamedEventContext) {
            val theEvent = ctx.parent as UnnamedEventContext
            if (theEvent.EVENT() != null) {
                output.append("clearedStoryEvents.push(\"@storyEvent$storyEventCounter\")")
                storyEventCounter++
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
        output.append("else -> {") ; indentLength++
        indent() ; output.append("println(\">>>No such choice exists\")"); indentLength--
        indent() ; output.append("}//else") //else
        indentLength--
        indent() ; output.append("}//when") //when
        indentLength--
        indent() ; output.append("}//while") //while
        indent()
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
        val injectedCode = ctx!!.CODE_INJECTION().text.drop(2).dropLast(2).trim()
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
}