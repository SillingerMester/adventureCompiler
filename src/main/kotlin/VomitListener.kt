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

    override fun enterNamedEvent(ctx: AdventureParser.NamedEventContext?) {
        indent() ; print("event ${ctx?.ID()?.text} {") ; indentLength++
        super.enterNamedEvent(ctx)
    }

    override fun exitNamedEvent(ctx: AdventureParser.NamedEventContext?) {
        indentLength-- ; indent() ; print("} //namedEvent")
        super.exitNamedEvent(ctx)
    }

    override fun enterIntroduction(ctx: AdventureParser.IntroductionContext?) {
        indent() ; print("introduction {") ; indentLength++
        super.enterIntroduction(ctx)
    }

    override fun exitIntroduction(ctx: AdventureParser.IntroductionContext?) {
        indentLength-- ; indent() ; print("} //introduction")
        super.exitIntroduction(ctx)
    }

    override fun enterUnnamedEvent(ctx: AdventureParser.UnnamedEventContext?) {
        indent() ; print("event {") ; indentLength++
        super.enterUnnamedEvent(ctx)
    }

    override fun exitUnnamedEvent(ctx: AdventureParser.UnnamedEventContext?) {
        indentLength-- ; indent() ; println("} //event")
        super.exitUnnamedEvent(ctx)
    }

    override fun enterConditionsBlock(ctx: AdventureParser.ConditionsBlockContext?) {
        indent() ; print("conditions {") ; indentLength++
        super.enterConditionsBlock(ctx)
    }

    override fun exitConditionsBlock(ctx: AdventureParser.ConditionsBlockContext?) {
        indentLength-- ; indent() ; print("} //conditions")
        super.exitConditionsBlock(ctx)
    }

    override fun enterStatementBlock(ctx: AdventureParser.StatementBlockContext?) {
        indent() ; print("{") ; indentLength++
        super.enterStatementBlock(ctx)
    }

    override fun exitStatementBlock(ctx: AdventureParser.StatementBlockContext?) {
        indentLength-- ; indent() ; print("}")
        super.exitStatementBlock(ctx)
    }

    override fun enterChoicesBlock(ctx: AdventureParser.ChoicesBlockContext?) {
        indent() ; print("choices {") ; indentLength++
        super.enterChoicesBlock(ctx)
    }

    override fun exitChoicesBlock(ctx: AdventureParser.ChoicesBlockContext?) {
        indentLength-- ; indent() ; print("} //choices")
        super.exitChoicesBlock(ctx)
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

    override fun enterJumpLocation(ctx: AdventureParser.JumpLocationContext?) {
        indent() ; print(ctx?.GOTO()?.text + " " + ctx?.ID())
        super.enterJumpLocation(ctx)
    }

    override fun enterTriggerEvent(ctx: AdventureParser.TriggerEventContext?) {
        indent() ; print(ctx?.TRIGGER()?.text + " " + ctx?.ID())
        super.enterTriggerEvent(ctx)
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