import com.example.AdventureBaseListener
import com.example.AdventureParser

class VomitListener : AdventureBaseListener() {
    private var indentLength = 0
    private fun indent() {
        println()
        print("".padEnd(indentLength, '\t'))
    }
    override fun enterAdventure(ctx: AdventureParser.AdventureContext?) {
        indent() ; print("adventure {") ; indentLength++
        super.enterAdventure(ctx)
    }

    override fun exitAdventure(ctx: AdventureParser.AdventureContext?) {
        indentLength-- ; indent() ; print("} //adventure")
        super.exitAdventure(ctx)
    }

    override fun enterLocation(ctx: AdventureParser.LocationContext?) {
        indent() ; print("location ${ctx?.ID()?.text} {") ; indentLength++
        super.enterLocation(ctx)
    }

    override fun exitLocation(ctx: AdventureParser.LocationContext?) {
        indentLength-- ; indent() ; print("} //location")
        super.exitLocation(ctx)
    }

    override fun enterNamed_event(ctx: AdventureParser.Named_eventContext?) {
        indent() ; print("event ${ctx?.ID()?.text} {") ; indentLength++
        super.enterNamed_event(ctx)
    }

    override fun exitNamed_event(ctx: AdventureParser.Named_eventContext?) {
        indentLength-- ; indent() ; print("} //namedEvent")
        super.exitNamed_event(ctx)
    }

    override fun enterIntroduction(ctx: AdventureParser.IntroductionContext?) {
        indent() ; print("introduction {") ; indentLength++
        super.enterIntroduction(ctx)
    }

    override fun exitIntroduction(ctx: AdventureParser.IntroductionContext?) {
        indentLength-- ; indent() ; print("} //introduction")
        super.exitIntroduction(ctx)
    }

    override fun enterUnnamed_event(ctx: AdventureParser.Unnamed_eventContext?) {
        indent() ; print("event {") ; indentLength++
        super.enterUnnamed_event(ctx)
    }

    override fun exitUnnamed_event(ctx: AdventureParser.Unnamed_eventContext?) {
        indentLength-- ; indent() ; println("} //event")
        super.exitUnnamed_event(ctx)
    }

    override fun enterConditions_block(ctx: AdventureParser.Conditions_blockContext?) {
        indent() ; print("conditions {") ; indentLength++
        super.enterConditions_block(ctx)
    }

    override fun exitConditions_block(ctx: AdventureParser.Conditions_blockContext?) {
        indentLength-- ; indent() ; print("} //conditions")
        super.exitConditions_block(ctx)
    }

    override fun enterStatement_block(ctx: AdventureParser.Statement_blockContext?) {
        indent() ; print("{") ; indentLength++
        super.enterStatement_block(ctx)
    }

    override fun exitStatement_block(ctx: AdventureParser.Statement_blockContext?) {
        indentLength-- ; indent() ; print("}")
        super.exitStatement_block(ctx)
    }

    override fun enterChoices_block(ctx: AdventureParser.Choices_blockContext?) {
        indent() ; print("choices {") ; indentLength++
        super.enterChoices_block(ctx)
    }

    override fun exitChoices_block(ctx: AdventureParser.Choices_blockContext?) {
        indentLength-- ; indent() ; print("} //choices")
        super.exitChoices_block(ctx)
    }

    override fun enterChoice(ctx: AdventureParser.ChoiceContext?) {
        indent() ; print(ctx?.start?.text + " ")
        super.enterChoice(ctx)
    }

    override fun enterVariable(ctx: AdventureParser.VariableContext?) {
        indent() ; print(ctx?.VAR()?.text + " " + ctx?.ID() + " " + ctx?.EQ()?.text + " ")
        super.enterVariable(ctx)
    }

    override fun enterAssignment(ctx: AdventureParser.AssignmentContext?) {
        indent() ; print(ctx?.ID()?.text + " " + ctx?.EQ()?.text + " ")
        super.enterAssignment(ctx)
    }

    override fun enterExpression(ctx: AdventureParser.ExpressionContext?) {
        print(ctx?.text + " ")
        super.enterExpression(ctx)
    }

    override fun enterPrint(ctx: AdventureParser.PrintContext?) {
        indent() ; print(ctx?.STRING()?.text + " " + (ctx?.CONTINUE_SIGN()?.text ?: "") + (ctx?.REPLACE_SIGN()?.text ?: "") + " ")
        super.enterPrint(ctx)
    }

    override fun enterJump_location(ctx: AdventureParser.Jump_locationContext?) {
        indent() ; print(ctx?.GOTO()?.text + " " + ctx?.ID())
        super.enterJump_location(ctx)
    }

    override fun enterTrigger_event(ctx: AdventureParser.Trigger_eventContext?) {
        indent() ; print(ctx?.TRIGGER()?.text + " " + ctx?.ID())
        super.enterTrigger_event(ctx)
    }

    override fun enterBranch(ctx: AdventureParser.BranchContext?) {
        indent() ; print("branch {") ; indentLength++
        super.enterBranch(ctx)
    }

    override fun exitBranch(ctx: AdventureParser.BranchContext?) {
        indentLength-- ; indent() ; print("} //branch")
        super.exitBranch(ctx)
    }
}