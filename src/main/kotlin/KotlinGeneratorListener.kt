import com.example.AdventureBaseListener
import com.example.AdventureListener
import com.example.AdventureParser
import com.example.AdventureParser.ExpressionContext
import com.example.AdventureParser.Unnamed_eventContext

class KotlinGeneratorListener(val output: StringBuilder) : AdventureBaseListener() {
    private var indentLength = 0
    private fun indent() {
        output.append("\n")
        output.append("".padEnd(indentLength, '\t'))
    }

    private fun realign() {
        output.append("\r" + "".padEnd(indentLength, '\t'))
    }

    override fun enterAdventure(ctx: AdventureParser.AdventureContext?) {
        output.append("""
                interface Location {
                    val here get() = this
                    fun execute()
                }

                class LocationChangeException(val newLocation: Location) : Exception()
                class GameOverException : Exception()

                fun end() {
                   throw GameOverException()
                }
                
                fun goto(where: Location): Nothing = throw LocationChangeException(where)
                
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
                        Thread.sleep(waitBetweenLines)
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
                if (ctx?.expression()?.unary_expression()?.literal()?.INTRODUCTION() != null) ".here" else ""
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
              """.trimIndent());
        indentLength += 2
        indent()
    }

    override fun exitIntroduction(ctx: AdventureParser.IntroductionContext?) {
        indentLength -= 2
        indent()
        output.append("""
                  }
              }
              """.trimIndent())
        indent()
    }

    override fun enterLocation(ctx: AdventureParser.LocationContext?) {
        output.append("""
                object ${ctx?.ID()?.text} : Location {
                    override fun execute() {
              """.trimIndent());
        indentLength += 2
        indent()
    }

    override fun exitLocation(ctx: AdventureParser.LocationContext?) {
        indentLength -= 2
        indent()
        output.append("""
                  }
              }
              """.trimIndent())
        indent()
    }

    override fun enterNamed_event(ctx: AdventureParser.Named_eventContext?) {
        output.append("fun ${ctx?.ID()?.text}Event(here: Location) {");
        indentLength++
        indent()
    }

    override fun exitNamed_event(ctx: AdventureParser.Named_eventContext?) {
        indentLength--
        indent()
        output.append("}")
        indent()
    }

    override fun enterUnnamed_event(ctx: AdventureParser.Unnamed_eventContext?) {
        output.append("(fun (here: Location) {");
        indentLength++
        indent()
    }

    override fun exitUnnamed_event(ctx: AdventureParser.Unnamed_eventContext?) {
        //handle the indent created by conditions block
        if (ctx?.conditions_block() != null) {
            indentLength--
            indent()
            output.append("}")
        }
        indentLength--
        indent()
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
        if (ctx?.conditions_block() != null) {
            indentLength--
            indent()
            output.append("}//if")
        }
        indent()
    }

    var storyEventCounter = 0
    override fun enterConditions_block(ctx: AdventureParser.Conditions_blockContext?) {
        output.append("if (")
        indentLength++
        indent()
        ctx!!.children.filterIsInstance<ExpressionContext>().map { it.text }.forEach {
            output.append("($it) &&")
            indent()
        }
        output.append("true")
        indentLength--
        indent()
    }

    override fun exitConditions_block(ctx: AdventureParser.Conditions_blockContext?) {
        output.append(") {")
        indentLength++
        indent()
    }

    override fun enterChoices_block(ctx: AdventureParser.Choices_blockContext?) {
        val allTheChoices = ctx?.children?.filterIsInstance<AdventureParser.ChoiceContext>()?.map { it.STRING().text }
        val choiceMap = (0..<allTheChoices!!.size).associate { (it + 1) to allTheChoices[it] }
        output.append("while (true) {")
        indentLength++
        indent()
        output.append("val choiceMap = mapOf(") ;
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
    }

    override fun exitChoices_block(ctx: AdventureParser.Choices_blockContext?) {
        indent() ; output.append("else -> {") ; indentLength++
        indent() ; output.append("println(\">>>No such choice exists\")"); indentLength--
        indent() ; output.append("}//else") //else
        indentLength--
        indent() ; output.append("}//when") //when
        indentLength--
        indent() ; output.append("}//while") //while
    }

    override fun enterChoice(ctx: AdventureParser.ChoiceContext?) {
        indent()
        output.append("${ctx?.STRING()} -> ")
    }

    override fun exitChoice(ctx: AdventureParser.ChoiceContext?) {
        // do nothing
    }

    override fun enterStatement_block(ctx: AdventureParser.Statement_blockContext?) {
        output.append(" {")
        indentLength++
        indent()
    }

    override fun exitStatement_block(ctx: AdventureParser.Statement_blockContext?) {
        indent()
        indentLength--
        realign()
        output.append("}//block")
        indent()
    }

    override fun enterJump_location(ctx: AdventureParser.Jump_locationContext?) {
        output.append("goto(" + ctx?.ID()?.text + ")")
    }

    override fun exitJump_location(ctx: AdventureParser.Jump_locationContext?) {
        indent()
    }

    override fun enterTrigger_event(ctx: AdventureParser.Trigger_eventContext?) {
        output.append(ctx?.ID()?.text + "Event(here)")
    }

    override fun exitTrigger_event(ctx: AdventureParser.Trigger_eventContext?) {
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

    override fun enterFinish_event(ctx: AdventureParser.Finish_eventContext?) {
        output.append("return")
    }

    override fun exitFinish_event(ctx: AdventureParser.Finish_eventContext?) {
        indent()
    }

    override fun enterEnd_story(ctx: AdventureParser.End_storyContext?) {
        output.append("end()")
    }

    override fun exitEnd_story(ctx: AdventureParser.End_storyContext?) {
        indent()
    }
}