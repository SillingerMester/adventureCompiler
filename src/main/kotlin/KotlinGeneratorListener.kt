import com.example.AdventureBaseListener
import com.example.AdventureListener
import com.example.AdventureParser
import com.example.AdventureParser.ExpressionContext
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.TerminalNode

class KotlinGeneratorListener : AdventureBaseListener() {
    private var indentLength = 0
    private fun indent() {
        println()
        print("".padEnd(indentLength, '\t'))
    }

    private fun realign() {
        print("\r" + "".padEnd(indentLength, '\t'))
    }

    override fun enterAdventure(ctx: AdventureParser.AdventureContext?) {
        print("""
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
        println()
        indent()
    }

    override fun exitAdventure(ctx: AdventureParser.AdventureContext?) {
        println("// adventure end")
        indent()
    }

    override fun enterVariable(ctx: AdventureParser.VariableContext?) {
        val initializer = ctx?.expression()?.text +
                if (ctx?.expression()?.unary_expression()?.literal()?.INTRODUCTION() != null) ".here" else ""
        print("var " + ctx?.ID()?.text + " = " + initializer)
        indent()
    }

    override fun exitVariable(ctx: AdventureParser.VariableContext?) {
        //do nothing
    }

    override fun enterIntroduction(ctx: AdventureParser.IntroductionContext?) {
        print("""
                object introduction : Location {
                    val introduction get() = this
                    override fun execute() {
              """.trimIndent());
        indentLength += 2
        indent()
    }

    override fun exitIntroduction(ctx: AdventureParser.IntroductionContext?) {
        indentLength -= 2
        indent()
        print("""
                  }
              }
              """.trimIndent())
        indent()
    }

    override fun enterLocation(ctx: AdventureParser.LocationContext?) {
        print("""
                object ${ctx?.ID()?.text} : Location {
                    override fun execute() {
              """.trimIndent());
        indentLength += 2
        indent()
    }

    override fun exitLocation(ctx: AdventureParser.LocationContext?) {
        indentLength -= 2
        indent()
        print("""
                  }
              }
              """.trimIndent())
        indent()
    }

    override fun enterNamed_event(ctx: AdventureParser.Named_eventContext?) {
        print("fun ${ctx?.ID()?.text}Event(here: Location) {");
        indentLength++
        indent()
    }

    override fun exitNamed_event(ctx: AdventureParser.Named_eventContext?) {
        indentLength--
        indent()
        print("}")
        indent()
    }

    override fun enterUnnamed_event(ctx: AdventureParser.Unnamed_eventContext?) {
        print("(fun (here: Location) {");
        indentLength++
        indent()
    }

    override fun exitUnnamed_event(ctx: AdventureParser.Unnamed_eventContext?) {
        //handle the indent created by conditions block
        if (ctx?.conditions_block() != null) {
            indentLength--
            indent()
            print("}")
        }
        indentLength--
        indent()
        print("}) (here)")
        indent()
    }

    override fun enterStatement(ctx: AdventureParser.StatementContext?) {
        //do nothing
    }

    override fun exitStatement(ctx: AdventureParser.StatementContext?) {
        //do nothing
    }

    override fun enterAssignment(ctx: AdventureParser.AssignmentContext?) {
        print(ctx?.ID()?.text + " = " + ctx?.expression()?.text)
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
            print("}//if")
        }
        indent()
    }

    override fun enterConditions_block(ctx: AdventureParser.Conditions_blockContext?) {
        print("if (")
        indentLength++
        indent()
        ctx!!.children.filterIsInstance<ExpressionContext>().map { it.text }.forEach {
            print("($it) &&")
        }
        print("true")
        indentLength--
        indent()
    }

    override fun exitConditions_block(ctx: AdventureParser.Conditions_blockContext?) {
        print(") {")
        indentLength++
        indent()
    }

    override fun enterChoices_block(ctx: AdventureParser.Choices_blockContext?) {
        val allTheChoices = ctx?.children?.filterIsInstance<AdventureParser.ChoiceContext>()?.map { it.STRING().text }
        val choiceMap = (0..<allTheChoices!!.size).associate { (it + 1) to allTheChoices[it] }
        print("while (true) {")
        indentLength++
        indent()
        print("val choiceMap = mapOf(") ;
        indentLength++
        for (choice in choiceMap) {
            indent() ; print("${choice.key} to ${choice.value},")
        }
        indentLength-- ; indent() ; print(")") //map
        indent() ; print("println(\">>>CHOICES\")")
        indent() ; print("choiceMap.entries.forEach {") ; indentLength++
        indent() ; print("println(\"    \" + it.key + \") \" + it.value)") ; indentLength--
        indent() ; print("}")
        indent() ; print("print(\">>>Your choice: \")")
        indent() ; print("val choiceNum = try { readln().toInt() } catch (_:NumberFormatException) { -1 }")
        indent() ; print("when (choiceMap[choiceNum]) {") ; indentLength++
    }

    override fun exitChoices_block(ctx: AdventureParser.Choices_blockContext?) {
        indent() ; print("else -> {") ; indentLength++
        indent() ; print("println(\">>>No such choice exists\")"); indentLength--
        indent() ; print("}//else") //else
        indentLength--
        indent() ; print("}//when") //when
        indentLength--
        indent() ; print("}//while") //while
    }

    override fun enterChoice(ctx: AdventureParser.ChoiceContext?) {
        indent()
        print("${ctx?.STRING()} -> ")
    }

    override fun exitChoice(ctx: AdventureParser.ChoiceContext?) {
        // do nothing
    }

    override fun enterStatement_block(ctx: AdventureParser.Statement_blockContext?) {
        print(" {")
        indentLength++
        indent()
    }

    override fun exitStatement_block(ctx: AdventureParser.Statement_blockContext?) {
        indent()
        indentLength--
        realign()
        print("}//block")
        indent()
    }

    override fun enterJump_location(ctx: AdventureParser.Jump_locationContext?) {
        print("goto(" + ctx?.ID()?.text + ")")
    }

    override fun exitJump_location(ctx: AdventureParser.Jump_locationContext?) {
        indent()
    }

    override fun enterTrigger_event(ctx: AdventureParser.Trigger_eventContext?) {
        print(ctx?.ID()?.text + "Event(here)")
    }

    override fun exitTrigger_event(ctx: AdventureParser.Trigger_eventContext?) {
        indent()
    }

    override fun enterPrint(ctx: AdventureParser.PrintContext?) {
        if (ctx!!.REPLACE_SIGN() == null) {
            print("print(" + ctx.STRING()?.text + ")")
        } else {
            print("print(\"\\r\" + " + ctx.STRING()?.text + ")")
        }

        if (ctx.CONTINUE_SIGN() == null) {
            print(" ; readln()")
        } else {
            print(" ; println()")
        }
    }

    override fun exitPrint(ctx: AdventureParser.PrintContext?) {
        indent()
    }

    override fun enterFinish_event(ctx: AdventureParser.Finish_eventContext?) {
        print("return")
    }

    override fun exitFinish_event(ctx: AdventureParser.Finish_eventContext?) {
        indent()
    }

    override fun enterEnd_story(ctx: AdventureParser.End_storyContext?) {
        print("end()")
    }

    override fun exitEnd_story(ctx: AdventureParser.End_storyContext?) {
        indent()
    }
}