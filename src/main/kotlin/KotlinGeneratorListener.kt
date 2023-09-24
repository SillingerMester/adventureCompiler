import com.example.AdventureBaseListener
import com.example.AdventureParser
import com.example.AdventureParser.ExpressionContext
import com.example.AdventureParser.UnnamedEventContext

class KotlinGeneratorListener(val output: StringBuilder) : AdventureBaseListener() {
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
            throw IllegalStateException("Error in generator: attempted to delete nom-empty line")
        }
    }

    override fun enterAdventure(ctx: AdventureParser.AdventureContext?) {
        output.append("""
                import java.util.Stack
                
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
        """.trimIndent())
        output.append("\n")
        indent()
    }

    override fun exitAdventure(ctx: AdventureParser.AdventureContext?) {
        output.append("// adventure end\n")
        indent()
    }

    override fun enterVariable(ctx: AdventureParser.VariableContext?) {
        val initializer = ctx?.expression()?.text +
                if (ctx?.expression()?.unaryExpression()?.literal()?.INTRODUCTION() != null) ".here" else ""
        output.append("var " + ctx?.ID()?.text + " = " + initializer)
        indent()
    }

    override fun exitVariable(ctx: AdventureParser.VariableContext?) {
        //do nothing
    }

    override fun enterIntroduction(ctx: AdventureParser.IntroductionContext?) {
        output.append("""
                object introduction : Location {
                    //val introduction get() = this
                    override fun execute() {
              """.trimIndent())
        indentLength += 2
        indent()
    }

    override fun exitIntroduction(ctx: AdventureParser.IntroductionContext?) {
        indentLength -= 2
        realign()
        output.append("""
                  }//execute
              }//introduction
              """.trimIndent())
        indent()
    }

    override fun enterLocation(ctx: AdventureParser.LocationContext?) {
        output.append("""
                object ${ctx?.ID()?.text} : Location {
                    override fun execute() {
              """.trimIndent())
        indentLength += 2
        indent()
    }

    override fun exitLocation(ctx: AdventureParser.LocationContext?) {
        indentLength -= 2
        realign()
        output.append("""
                  }//execute
              }//location
              """.trimIndent())
        indent()
    }

    override fun enterNamedEvent(ctx: AdventureParser.NamedEventContext?) {
        output.append("fun ${ctx?.ID()?.text}Event(here: Location) {")
        indentLength++
        indent()
    }

    override fun exitNamedEvent(ctx: AdventureParser.NamedEventContext?) {
        indentLength--
        realign()
        output.append("}//event")
        indent()
    }

    override fun enterUnnamedEvent(ctx: UnnamedEventContext?) {
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
        //do nothing
    }

    override fun exitStatement(ctx: AdventureParser.StatementContext?) {
        //do nothing
    }

    override fun enterAssignment(ctx: AdventureParser.AssignmentContext?) {
        output.append(ctx?.ID()?.text + " = " + ctx?.expression()?.text)
        indent()
    }

    override fun exitAssignment(ctx: AdventureParser.AssignmentContext?) {
        //do nothing
    }

    override fun enterBranch(ctx: AdventureParser.BranchContext?) {
        //do nothing, if() will be produced by conditions block
    }

    override fun exitBranch(ctx: AdventureParser.BranchContext?) {
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

        ctx.children.filterIsInstance<ExpressionContext>().map { it.text }.forEach {
            output.append("($it) &&")
            indent()
        }
        output.append("true")
        indentLength--
        indent()
    }

    override fun exitConditionsBlock(ctx: AdventureParser.ConditionsBlockContext?) {
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
        output.append("${ctx?.STRING()} -> ")
    }

    override fun exitChoice(ctx: AdventureParser.ChoiceContext?) {
        //all valid statements indent() after themselves
    }

    override fun enterStatementBlock(ctx: AdventureParser.StatementBlockContext?) {
        output.append(" {")
        indentLength++
        indent()
    }

    override fun exitStatementBlock(ctx: AdventureParser.StatementBlockContext?) {
        indentLength--
        realign()
        output.append("}//block")
        indent()
    }

    override fun enterJumpLocation(ctx: AdventureParser.JumpLocationContext?) {
        output.append("goto(" + ctx?.ID()?.text + ")")
    }

    override fun exitJumpLocation(ctx: AdventureParser.JumpLocationContext?) {
        indent()
    }

    override fun enterTriggerEvent(ctx: AdventureParser.TriggerEventContext?) {
        output.append(ctx?.ID()?.text + "Event(here)")
    }

    override fun exitTriggerEvent(ctx: AdventureParser.TriggerEventContext?) {
        indent()
    }

    override fun enterPrint(ctx: AdventureParser.PrintContext?) {
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
        indent()
    }

    override fun enterFinishEvent(ctx: AdventureParser.FinishEventContext?) {
        output.append("return")
    }

    override fun exitFinishEvent(ctx: AdventureParser.FinishEventContext?) {
        indent()
    }

    override fun enterEndStory(ctx: AdventureParser.EndStoryContext?) {
        output.append("end()")
    }

    override fun exitEndStory(ctx: AdventureParser.EndStoryContext?) {
        indent()
    }

    override fun enterUntriggerEvent(ctx: AdventureParser.UntriggerEventContext?) {
        output.append("clearedStoryEvents.pop()")
    }

    override fun exitUntriggerEvent(ctx: AdventureParser.UntriggerEventContext?) {
        indent()
    }
}