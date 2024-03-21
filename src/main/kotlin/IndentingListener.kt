import adventure.AdventureBaseListener
import adventure.AdventureLexer
import adventure.AdventureParser
import org.antlr.v4.runtime.tree.TerminalNode

class IndentingListener(val output: StringBuilder) : AdventureBaseListener() { /* formerly known as VomitListener */
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
        if (ctx?.STORY() != null) {
            output.append(ctx.STORY().text + " ")
        }
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
        output.append(ctx?.VAR()?.text + " " + ctx?.ID() + " " + ctx?.ASSIGN()?.text + " " + ctx?.expression()?.text)
    }

    override fun exitVariable(ctx: AdventureParser.VariableContext?) {
        indent()
    }

    override fun enterAssignment(ctx: AdventureParser.AssignmentContext?) {
        output.append(ctx?.ID()?.text + " " + ctx?.ASSIGN()?.text + " " + ctx?.expression()?.text)
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

    override fun enterCodeInjection(ctx: AdventureParser.CodeInjectionContext?) {
        val injectedCode = ctx!!.CODE_INJECTION().text.drop(2).dropLast(2).trim()
        if (injectedCode.contains('\r') || injectedCode.contains('\n')) {
            val lines = injectedCode.lines()
            output.append("@[")
            indentLength++
            indent()
            lines.forEach {
                output.append(it)
                indent()
            }
            indentLength--
            realign()
            output.append("]@")
            indent()
        } else {
            output.append("@[ $injectedCode ]@")
            indent()
        }
    }

    override fun exitCodeInjection(ctx: AdventureParser.CodeInjectionContext?) {
        //do nothing
    }

    override fun visitTerminal(node: TerminalNode?) {
        if (node?.symbol?.type == AdventureLexer.COMMENT) {
            output.append(node.text)
        }
    }

    override fun enterStatsBlock(ctx: AdventureParser.StatsBlockContext?) {
        output.append(ctx!!.STATS().text + " " + ctx.CURLY_LEFT().text)
        indentLength++
        indent()
        ctx.ID().forEach {
            output.append(it.text)
            output.append(" ")
        }
        indent()
    }
    override fun exitStatsBlock(ctx: AdventureParser.StatsBlockContext?) {
        indentLength--
        realign()
        output.append("}//stats")
        indent()
    }

    override fun enterInventoryBlock(ctx: AdventureParser.InventoryBlockContext?) {
        output.append(ctx!!.INVENTORY().text + " " + ctx.CURLY_LEFT().text)
        indentLength++
        indent()
        ctx.ID().forEach {
            output.append(it.text)
            indent()
        }
    }

    override fun exitInventoryBlock(ctx: AdventureParser.InventoryBlockContext?) {
        indentLength--
        realign()
        output.append("} //inventory")
        indent()
    }

    override fun enterItem(ctx: AdventureParser.ItemContext?) {
        output.append(ctx!!.ITEM().text + " " + ctx.ID().text + " " + ctx.CURLY_LEFT().text)
        indentLength++
        indent()
        output.append(ctx.DESCRIPTION().text + " " + ctx.STRING().text)
        indent()
    }

    override fun exitItem(ctx: AdventureParser.ItemContext?) {
        indentLength--
        realign()
        output.append("} //item")
        indent()
    }

    override fun enterItemFunction(ctx: AdventureParser.ItemFunctionContext?) {
        output.append(ctx!!.start.text + " ")
        //statement | statementBlock should vomit itself
    }

    override fun exitItemFunction(ctx: AdventureParser.ItemFunctionContext?) {
        //do nothing
    }

    override fun enterLoadGame(ctx: AdventureParser.LoadGameContext?) {
        output.append(ctx?.LOAD()?.text)
    }

    override fun exitLoadGame(ctx: AdventureParser.LoadGameContext?) {
        indent()
    }

    override fun enterSaveGame(ctx: AdventureParser.SaveGameContext?) {
        output.append(ctx?.SAVE()?.text)
    }

    override fun exitSaveGame(ctx: AdventureParser.SaveGameContext?) {
        indent()
    }

    override fun enterConsumeItem(ctx: AdventureParser.ConsumeItemContext?) {
        output.append(ctx!!.CONSUME().text)
    }

    override fun exitConsumeItem(ctx: AdventureParser.ConsumeItemContext?) {
        indent()
    }

    override fun enterEquipItem(ctx: AdventureParser.EquipItemContext?) {
        output.append(ctx!!.EQUIP().text)
    }

    override fun exitEquipItem(ctx: AdventureParser.EquipItemContext?) {
        indent()
    }

    override fun enterUnequipItem(ctx: AdventureParser.UnequipItemContext?) {
        output.append(ctx!!.UNEQUIP().text)
    }

    override fun exitUnequipItem(ctx: AdventureParser.UnequipItemContext?) {
        indent()
    }

    override fun enterGetItem(ctx: AdventureParser.GetItemContext?) {
        output.append(ctx!!.GET_ITEM().text + " " + ctx.ID().text)
    }

    override fun exitGetItem(ctx: AdventureParser.GetItemContext?) {
        indent()
    }

    override fun enterBuiltinMax(ctx: AdventureParser.BuiltinMaxContext?) {
        //do NOT vomit here, parent statement will vomit instead
        /*output.append(
            ctx!!.MAX().text + ctx.PAREN_LEFT().text +
            ctx.expression(0).text + ctx.COMMA().text + " " +
            ctx.expression(1).text + ctx.PAREN_RIGHT().text
        )*/
    }

    override fun exitBuiltinMax(ctx: AdventureParser.BuiltinMaxContext?) {
        //do nothing, it isn't a statement
    }

    override fun enterAfterEvent(ctx: AdventureParser.AfterEventContext?) {
        //do nothing, it isn't a statement
    }

    override fun exitAfterEvent(ctx: AdventureParser.AfterEventContext?) {
        //do nothing, it isn't a statement
    }

    override fun enterHasItem(ctx: AdventureParser.HasItemContext?) {
        //do NOT vomit here, parent statement will vomit instead
        //output.append(ctx!!.HAS_ITEM().text + " " + ctx.ID().text)
    }

    override fun exitHasItem(ctx: AdventureParser.HasItemContext?) {
        //do nothing, it isn't a statement
    }

    override fun enterAfterChoice(ctx: AdventureParser.AfterChoiceContext?) {
        output.append(ctx!!.AFTER_CHOICE().text + " " + ctx.CURLY_LEFT().text)
        indentLength++
        indent()
    }

    override fun exitAfterChoice(ctx: AdventureParser.AfterChoiceContext?) {
        indentLength--
        realign()
        output.append("} //afterEach")
        indent()
    }

    override fun enterReplaceItem(ctx: AdventureParser.ReplaceItemContext?) {
        output.append(ctx!!.REPLACE_ITEM().text + " " + ctx.ID().text)
    }

    override fun exitReplaceItem(ctx: AdventureParser.ReplaceItemContext?) {
        indent()
    }

    override fun enterItemsSubmenu(ctx: AdventureParser.ItemsSubmenuContext?) {
        output.append(ctx!!.ITEMS_SUBMENU().text)
    }

    override fun exitItemsSubmenu(ctx: AdventureParser.ItemsSubmenuContext?) {
        indent()
    }
    override fun enterInputText(ctx: AdventureParser.InputTextContext?) {
        //do nothing, this isn't a statement
    }
    override fun exitInputText(ctx: AdventureParser.InputTextContext?) {
        //do nothing, this isn't a statement
    }
}