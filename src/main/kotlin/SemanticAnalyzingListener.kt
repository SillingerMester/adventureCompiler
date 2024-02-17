import SymbolTable.ExpressionType
import com.example.AdventureBaseListener
import com.example.AdventureParser
import com.example.AdventureParser.*
import org.antlr.v4.runtime.RuleContext
import org.antlr.v4.runtime.Token

open class SemanticAnalyzingListener : AdventureBaseListener() {

    var error = false
    var warning = false

    val symbolTable = SymbolTable()
    val storyEvents = mutableListOf<String>()

    private fun findEnclosingEvent(ctx: RuleContext): RuleContext? {
        var parentCtx: RuleContext = ctx
        while (parentCtx !is AdventureContext) {
            if (parentCtx is AdventureParser.UnnamedEventContext || parentCtx is AdventureParser.NamedEventContext) {
                return parentCtx
            }
            parentCtx = parentCtx.parent
        }
        return null
    }

    private fun findEnclosingIem(ctx: RuleContext): RuleContext? {
        var parentCtx: RuleContext = ctx
        while (parentCtx !is AdventureContext) {
            if (parentCtx is AdventureParser.ItemContext) {
                return parentCtx
            }
            parentCtx = parentCtx.parent
        }
        return null
    }

    private fun printMessage(what: String) {
        print(what + "\n")
    }

    private fun printError(offender: Token, message: String) {
        error = true
        printMessage("line ${offender.line}:${offender.charPositionInLine} $message")
    }

    private fun printWarning(offender: Token, message: String) {
        warning = true
        printMessage("line ${offender.line}:${offender.charPositionInLine} $message")
    }

    private fun errorAlreadyDefined(offender: Token) {
        printError(offender,"Symbol ${offender.text} is already defined!")
    }

    private fun errorNotDefined(offender: Token) {
        printError(offender,"Symbol ${offender.text} is not defined in this scope!")
    }

    // listener interface

    override fun enterAdventure(ctx: AdventureContext?) {
        ctx?.children?.filterIsInstance<AdventureParser.IntroductionContext>()?.forEach {
            val token = it.INTRODUCTION().symbol
            if (symbolTable.peek().contains(token.text)) {
                errorAlreadyDefined(token)
            } else {
                symbolTable.peek()[token.text] = ExpressionType.LOCATION
            }
        }

        var statsBlockExists = false
        ctx?.children?.filterIsInstance<AdventureParser.StatsBlockContext>()?.forEach {
            val token = it.STATS().symbol
            if (statsBlockExists) {
                printError(token, "stats block already defined")
            }
            statsBlockExists = true
        }

        var inventoryBlockExists = false
        ctx?.children?.filterIsInstance<AdventureParser.InventoryBlockContext>()?.forEach {
            val token = it.INVENTORY().symbol
            if (inventoryBlockExists) {
                printError(token, "inventory block already defined")
            }
            inventoryBlockExists = true
        }

        ctx?.children?.filterIsInstance<AdventureParser.LocationContext>()?.forEach {
            if (symbolTable.peek().contains(it.ID().text)) {
                errorAlreadyDefined(it.ID().symbol)
            } else {
                symbolTable.peek()[it.ID().text] = ExpressionType.LOCATION
            }
        }
        ctx?.children?.filterIsInstance<AdventureParser.NamedEventContext>()?.forEach {
            if (symbolTable.peek().contains(it.ID().text)) {
                ctx.start.line
                errorAlreadyDefined(it.ID().symbol)
            } else {
                symbolTable.peek()[it.ID().text] = ExpressionType.EVENT
                storyEvents.add(it.ID().text)
            }
        }
        ctx?.children?.filterIsInstance<AdventureParser.ItemContext>()?.forEach {
            if (symbolTable.peek().contains(it.ID().text)) {
                ctx.start.line
                errorAlreadyDefined(it.ID().symbol)
            } else {
                symbolTable.peek()[it.ID().text] = ExpressionType.ITEM
            }
        }
    }

    override fun enterVariable(ctx: AdventureParser.VariableContext?) {
        val name = ctx?.ID()?.text!!
        if (symbolTable.peek().contains(name)) {
            errorAlreadyDefined(ctx.ID().symbol)
        } else {
            symbolTable.peek()[name] = symbolTable.getExpressionType(ctx.expression())
        }
    }

    override fun enterImplicitTypedExpr(ctx: AdventureParser.ImplicitTypedExprContext?) {
        val type = symbolTable.getSymbolType(ctx!!.ID().text)
        if (type == ExpressionType.UNDEFINED) {
            errorNotDefined(ctx.ID().symbol)
            return
        }
        if (ctx.parent is IntExpressionUContext && type != ExpressionType.INT) {
            printError(ctx.ID().symbol, "int expression required")
        }
        if (ctx.parent is BoolExpressionUContext && type != ExpressionType.BOOL) {
            printError(ctx.ID().symbol, "bool expression required")
        }

        if (ctx.parent is IntComparisonContext && type != ExpressionType.INT) {
            printError(ctx.ID().symbol, "int expression required")
        }
    }

    override fun enterFinishEvent(ctx: AdventureParser.FinishEventContext?) {
        val theEvent = findEnclosingEvent(ctx!!)
        if (theEvent == null) {
            val token = ctx.FINISH_EVENT().symbol
            printError(token, "${token.text} can only be used in event definitions.")
        }
    }

    override fun enterUntriggerEvent(ctx: AdventureParser.UntriggerEventContext?) {
        val theEvent = findEnclosingEvent(ctx!!)
        if (theEvent == null || !(theEvent is AdventureParser.UnnamedEventContext && theEvent.STORY() != null || theEvent is AdventureParser.NamedEventContext && theEvent.STORY() != null)) {
            printWarning(ctx.start, "${ctx.UNTRIGGER()} is not inside a story event. Are you sure?")
        }
    }

    override fun enterJumpLocation(ctx: AdventureParser.JumpLocationContext?) {
        val target = ctx!!.ID().text
        if (symbolTable.getSymbolType(target) != ExpressionType.LOCATION) {
            printError(ctx.ID().symbol, "$target is not a location!")
        }
    }

    override fun enterTriggerEvent(ctx: AdventureParser.TriggerEventContext?) {
        val target = ctx!!.ID().text
        if (symbolTable.getSymbolType(target) != ExpressionType.EVENT) {
            printError(ctx.ID().symbol, "$target is not an event!")
        }
    }

    override fun enterIntroduction(ctx: AdventureParser.IntroductionContext?) {
        symbolTable.push()
    }

    override fun exitIntroduction(ctx: AdventureParser.IntroductionContext?) {
        symbolTable.pop()
    }
    override fun enterLocation(ctx: AdventureParser.LocationContext?) {
        symbolTable.push()
    }

    override fun exitLocation(ctx: AdventureParser.LocationContext?) {
        symbolTable.pop()
    }

    override fun enterNamedEvent(ctx: AdventureParser.NamedEventContext?) {
        symbolTable.push()
    }

    override fun exitNamedEvent(ctx: AdventureParser.NamedEventContext?) {
        symbolTable.pop()
    }

    override fun enterUnnamedEvent(ctx: AdventureParser.UnnamedEventContext?) {
        symbolTable.push()
    }

    override fun exitUnnamedEvent(ctx: AdventureParser.UnnamedEventContext?) {
        symbolTable.pop()
    }

    override fun enterStatementBlock(ctx: AdventureParser.StatementBlockContext?) {
        symbolTable.push()
    }

    override fun exitStatementBlock(ctx: AdventureParser.StatementBlockContext?) {
        symbolTable.pop()
    }

    override fun enterBranch(ctx: AdventureParser.BranchContext?) {
        symbolTable.push()
    }

    override fun exitBranch(ctx: AdventureParser.BranchContext?) {
        symbolTable.pop()
    }
    override fun enterItem(ctx: AdventureParser.ItemContext?) {
        symbolTable.push()
        ctx!!.itemFunction().filterNot {
            it.USE() != null || it.EQUIP() != null || it.UNEQUIP() != null
        }.forEach {
            symbolTable.peek()[it.ID().text] = ExpressionType.EVENT
        }
    }

    override fun exitItem(ctx: AdventureParser.ItemContext?) {
        symbolTable.pop()
    }

    override fun enterItemFunction(ctx: AdventureParser.ItemFunctionContext?) {
        symbolTable.push()
    }

    override fun exitItemFunction(ctx: AdventureParser.ItemFunctionContext?) {
        symbolTable.pop()
    }

    override fun enterConsumeItem(ctx: AdventureParser.ConsumeItemContext?) {
        val itemDef = findEnclosingIem(ctx!!)
        if (itemDef == null) {
            printError(ctx.CONSUME().symbol, "'consume' only works inside item definitions.")
        }
    }

    override fun exitConsumeItem(ctx: AdventureParser.ConsumeItemContext?) {
        //do nothing
    }

    override fun enterEquipItem(ctx: AdventureParser.EquipItemContext?) {
        val itemDef = findEnclosingIem(ctx!!)
        if (itemDef == null) {
            printError(ctx.EQUIP().symbol, "'equip' only works inside item definitions.")
        }
    }

    override fun exitEquipItem(ctx: AdventureParser.EquipItemContext?) {
        //do nothing
    }

    override fun enterUnequipItem(ctx: AdventureParser.UnequipItemContext?) {
        val itemDef = findEnclosingIem(ctx!!)
        if (itemDef == null) {
            printError(ctx.UNEQUIP().symbol, "'unequip' only works inside item definitions.")
        }
    }

    override fun exitUnequipItem(ctx: AdventureParser.UnequipItemContext?) {
        //do nothing
    }

    override fun enterGetItem(ctx: AdventureParser.GetItemContext?) {
        val item = ctx!!.ID().text
        val type = symbolTable.getSymbolType(item)
        if (type != ExpressionType.ITEM) {
            printError(ctx.ID().symbol, "$item is not an item!")
        }
    }

    override fun exitGetItem(ctx: AdventureParser.GetItemContext?) {
        //do nothing
    }

    override fun enterBuiltinMax(ctx: AdventureParser.BuiltinMaxContext?) {
        //do nothing
    }

    override fun exitBuiltinMax(ctx: AdventureParser.BuiltinMaxContext?) {
        //do nothing
    }

    override fun enterInputText(ctx: AdventureParser.InputTextContext?) {
        //do nothing
    }

    override fun exitInputText(ctx: AdventureParser.InputTextContext?) {
        //do nothing
    }

    override fun enterAfterEvent(ctx: AdventureParser.AfterEventContext?) {
        val event = ctx!!.ID().text
        val type = symbolTable.getSymbolType(event)
        if (type != ExpressionType.EVENT) {
            printError(ctx.ID().symbol, "$event is not an event!")
        }
        if (!storyEvents.contains(event)) {
            printWarning(ctx.ID().symbol, "$event is not a story event, after() will always be false.")
        }
    }

    override fun exitAfterEvent(ctx: AdventureParser.AfterEventContext?) {
        //do nothing
    }

    override fun enterHasItem(ctx: AdventureParser.HasItemContext?) {
        val item = ctx!!.ID().text
        val type = symbolTable.getSymbolType(item)
        if (type != ExpressionType.ITEM) {
            printError(ctx.ID().symbol, "$item is not an item!")
        }
    }

    override fun exitHasItem(ctx: AdventureParser.HasItemContext?) {
        //do nothing
    }

    override fun exitCodeInjection(ctx: AdventureParser.CodeInjectionContext?) {
        //do nothing
    }

    override fun enterInventoryBlock(ctx: AdventureParser.InventoryBlockContext?) {
        ctx!!.ID().forEach {
            if (symbolTable.getSymbolType(it.text) != ExpressionType.ITEM) {
                printError(it.symbol, "${it.text} is not an item!")
            }
        }
    }

    override fun exitInventoryBlock(ctx: AdventureParser.InventoryBlockContext?) {
        //do nothing
    }

    override fun enterConditionsBlock(ctx: AdventureParser.ConditionsBlockContext?) {
        ctx!!.expression().forEach {
            if (
                it.codeInjectionExpr() == null && it.boolExpression() == null && (
                        it.implicitTypedExpr() == null ||
                                it.implicitTypedExpr() != null &&
                                symbolTable.getSymbolType(it.implicitTypedExpr().ID().text) != ExpressionType.BOOL
                    )
                ) {
                printError(it.start, "Bool expression required")
            }
        }
    }

    override fun enterReplaceItem(ctx: AdventureParser.ReplaceItemContext?) {
        val itemDef = findEnclosingIem(ctx!!)
        val varType = symbolTable.getSymbolType(ctx.ID().text)
        if (itemDef == null) {
            printError(ctx.REPLACE_ITEM().symbol, "${ctx.REPLACE_ITEM().text} only works inside item definitions.")
        }
        if (varType != ExpressionType.ITEM) {
            printError(ctx.REPLACE_ITEM().symbol, "${ctx.ID().text} is not an item variable.")
        }
    }

    override fun exitReplaceItem(ctx: AdventureParser.ReplaceItemContext?) {
        //do nothing
    }

    override fun enterItemsSubmenu(ctx: AdventureParser.ItemsSubmenuContext?) {
        //do nothing
    }

    override fun exitItemsSubmenu(ctx: AdventureParser.ItemsSubmenuContext?) {
        //do nothing
    }
}