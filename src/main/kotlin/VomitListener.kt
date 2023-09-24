import com.example.AdventureBaseListener
import com.example.AdventureListener
import com.example.AdventureParser

class VomitListener(val output: StringBuilder) : AdventureBaseListener() {
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
        //indent() ; print("adventure {") ; indentLength++
    }

    override fun exitAdventure(ctx: AdventureParser.AdventureContext?) {
        //indentLength-- ; indent() ; print("}//adventure")
    }

    override fun enterLocation(ctx: AdventureParser.LocationContext?) {
        output.append("location ${ctx?.ID()?.text} {")
        indentLength++
        indent()
    }

    override fun exitLocation(ctx: AdventureParser.LocationContext?) {
        indentLength--
        realign()
        output.append("}//location")
        indent()
    }

    override fun enterNamedEvent(ctx: AdventureParser.NamedEventContext?) {
        output.append("event ${ctx?.ID()?.text} {")
        indentLength++
        indent()
    }

    override fun exitNamedEvent(ctx: AdventureParser.NamedEventContext?) {
        indentLength--
        realign()
        output.append("}//namedEvent")
        indent()
    }

    override fun enterIntroduction(ctx: AdventureParser.IntroductionContext?) {
        output.append("introduction {")
        indentLength++
        indent()
    }

    override fun exitIntroduction(ctx: AdventureParser.IntroductionContext?) {
        indentLength--
        realign()
        output.append("}//introduction")
        indent()
    }

    override fun enterUnnamedEvent(ctx: AdventureParser.UnnamedEventContext?) {
        if (ctx?.STORY() != null) {
            output.append(ctx.STORY().text + " ")
        }
        output.append("event {")
        indentLength++
        indent()
    }

    override fun exitUnnamedEvent(ctx: AdventureParser.UnnamedEventContext?) {
        indentLength--
        realign()
        output.append("}//event")
        indent()
    }

    override fun enterConditionsBlock(ctx: AdventureParser.ConditionsBlockContext?) {
        output.append("conditions {")
        indentLength++
        indent()

        ctx?.children?.filterIsInstance<AdventureParser.ExpressionContext>()?.map { it.text }?.forEach {
            output.append(it)
            indent()
        }
    }

    override fun exitConditionsBlock(ctx: AdventureParser.ConditionsBlockContext?) {
        indentLength--
        realign()
        output.append("}//conditions")
        indent()
    }

    override fun enterStatementBlock(ctx: AdventureParser.StatementBlockContext?) {
        output.append("{")
        indentLength++
        indent()
    }

    override fun exitStatementBlock(ctx: AdventureParser.StatementBlockContext?) {
        indentLength--
        realign()
        output.append("}//block")
        indent()
    }

    override fun enterChoicesBlock(ctx: AdventureParser.ChoicesBlockContext?) {
        output.append("choices {")
        indentLength++
        indent()
    }

    override fun exitChoicesBlock(ctx: AdventureParser.ChoicesBlockContext?) {
        indentLength--
        realign()
        output.append("}//choices")
        indent()
    }

    override fun enterChoice(ctx: AdventureParser.ChoiceContext?) {
        output.append(ctx?.start?.text + " ")
    }

    override fun exitChoice(ctx: AdventureParser.ChoiceContext?) {
        //do nothing
    }

    override fun enterVariable(ctx: AdventureParser.VariableContext?) {
        output.append(ctx?.VAR()?.text + " " + ctx?.ID() + " " + ctx?.EQ()?.text + " " + ctx?.expression()?.text)
    }

    override fun exitVariable(ctx: AdventureParser.VariableContext?) {
        indent()
    }

    override fun enterAssignment(ctx: AdventureParser.AssignmentContext?) {
        output.append(ctx?.ID()?.text + " " + ctx?.EQ()?.text + " " + ctx?.expression()?.text)
    }

    override fun exitAssignment(ctx: AdventureParser.AssignmentContext?) {
        indent()
    }

    override fun enterPrint(ctx: AdventureParser.PrintContext?) {
        output.append(ctx?.STRING()?.text + " " + (ctx?.CONTINUE_SIGN()?.text ?: "") + (ctx?.REPLACE_SIGN()?.text ?: "") + " ")
    }

    override fun exitPrint(ctx: AdventureParser.PrintContext?) {
        indent()
    }

    override fun enterUntriggerEvent(ctx: AdventureParser.UntriggerEventContext?) {
        output.append(ctx?.UNTRIGGER()?.text)
    }

    override fun exitUntriggerEvent(ctx: AdventureParser.UntriggerEventContext?) {
        indent()
    }

    override fun enterJumpLocation(ctx: AdventureParser.JumpLocationContext?) {
        output.append(ctx?.GOTO()?.text + " " + ctx?.ID())
    }

    override fun exitJumpLocation(ctx: AdventureParser.JumpLocationContext?) {
        indent()
    }

    override fun enterTriggerEvent(ctx: AdventureParser.TriggerEventContext?) {
        output.append(ctx?.TRIGGER()?.text + " " + ctx?.ID())
    }

    override fun exitTriggerEvent(ctx: AdventureParser.TriggerEventContext?) {
        indent()
    }

    override fun enterFinishEvent(ctx: AdventureParser.FinishEventContext?) {
        output.append(ctx?.FINISH_EVENT()?.text)
    }

    override fun exitFinishEvent(ctx: AdventureParser.FinishEventContext?) {
        indent()
    }

    override fun enterEndStory(ctx: AdventureParser.EndStoryContext?) {
        output.append(ctx?.END_STORY()?.text)
    }

    override fun exitEndStory(ctx: AdventureParser.EndStoryContext?) {
        indent()
    }

    override fun enterBranch(ctx: AdventureParser.BranchContext?) {
        output.append("branch {")
        indentLength++
        indent()
    }

    override fun exitBranch(ctx: AdventureParser.BranchContext?) {
        indentLength--
        realign()
        output.append("}//branch")
        indent()
    }
}