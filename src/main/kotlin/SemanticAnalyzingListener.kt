import com.example.AdventureBaseListener
import com.example.AdventureParser
import com.example.AdventureParser.AdventureContext
import com.example.AdventureParser.BoolExpressionBContext
import com.example.AdventureParser.BoolExpressionUContext
import com.example.AdventureParser.ExpressionContext
import com.example.AdventureParser.IntComparisonContext
import com.example.AdventureParser.IntExpressionUContext
import com.example.AdventureParser.OtherExpressionUContext
import org.antlr.v4.runtime.RuleContext
import org.antlr.v4.runtime.Token
import java.util.*

class SemanticAnalyzingListener : AdventureBaseListener() {
    enum class ExpressionType {
        INT, STRING, BOOL, LOCATION, EVENT, UNDEFINED
    }

    var error = false
    var warning = false

    private val symbolTable = Stack<MutableMap<String, ExpressionType>>()
    init {
        symbolTable.push(mutableMapOf())
    }

    private fun getSymbolType(name: String): ExpressionType {
        // Check if the symbol is defined in any of the active scopes (from innermost to outermost)
        for (scope in symbolTable.asReversed()) {
            if (scope.containsKey(name)) {
                return scope[name] ?: ExpressionType.UNDEFINED
            }
        }
        return ExpressionType.UNDEFINED
    }

    private fun getExpressionType(ctx: ExpressionContext): ExpressionType {
        if(ctx.boolExpression() != null) {
            return ExpressionType.BOOL
        }
        if (ctx.intExpression() != null) {
            return ExpressionType.INT
        }
        if (ctx.otherExpression() != null) {
            val actualExpression = ctx.otherExpression().otherExpressionU()
            if (actualExpression.STRING() != null) {
                return ExpressionType.STRING
            }
            if (actualExpression.HERE() != null || actualExpression.INTRODUCTION() != null) {
                return ExpressionType.LOCATION
            }
            if (actualExpression.implicitTypedExpr() != null) {
                return getSymbolType(actualExpression.implicitTypedExpr().ID().text)
            }
            return ExpressionType.UNDEFINED
        }
        return ExpressionType.UNDEFINED
    }

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
        ctx?.children?.filterIsInstance<AdventureParser.LocationContext>()?.forEach {
            if (symbolTable.peek().contains(it.ID().text)) {
                errorAlreadyDefined(it.ID().symbol)
            } else {
                symbolTable.peek()[it.ID().text] = ExpressionType.LOCATION
            }
        }
        ctx?.children?.filterIsInstance<AdventureParser.NamedEventContext>()?.forEach {
            if (symbolTable.peek().contains(it.text)) {
                ctx.start.line
                errorAlreadyDefined(it.ID().symbol)
            } else {
                symbolTable.peek()[it.ID().text] = ExpressionType.EVENT
            }
        }
    }

    override fun enterVariable(ctx: AdventureParser.VariableContext?) {
        val name = ctx?.ID()?.text!!
        if (symbolTable.peek().contains(name)) {
            errorAlreadyDefined(ctx.ID().symbol)
        } else {
            symbolTable.peek()[name] = getExpressionType(ctx.expression())
        }
    }

    override fun enterImplicitTypedExpr(ctx: AdventureParser.ImplicitTypedExprContext?) {
        val type = getSymbolType(ctx!!.ID().text)
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
        if (theEvent == null || theEvent !is AdventureParser.UnnamedEventContext || theEvent.STORY() == null) {
            printWarning(ctx.start, "${ctx.UNTRIGGER()} is not inside a story event. Are you sure?")
        }
    }

    override fun enterJumpLocation(ctx: AdventureParser.JumpLocationContext?) {
        val target = ctx!!.ID().text
        if (getSymbolType(target) != ExpressionType.LOCATION) {
            printError(ctx.ID().symbol, "$target is not a location!")
        }
    }

    override fun enterTriggerEvent(ctx: AdventureParser.TriggerEventContext?) {
        val target = ctx!!.ID().text
        if (getSymbolType(target) != ExpressionType.EVENT) {
            printError(ctx.ID().symbol, "$target is not an event!")
        }
    }

    override fun enterLocation(ctx: AdventureParser.LocationContext?) {
        symbolTable.push(mutableMapOf())
    }

    override fun exitLocation(ctx: AdventureParser.LocationContext?) {
        symbolTable.pop()
    }

    override fun enterNamedEvent(ctx: AdventureParser.NamedEventContext?) {
        symbolTable.push(mutableMapOf())
    }

    override fun exitNamedEvent(ctx: AdventureParser.NamedEventContext?) {
        symbolTable.pop()
    }

    override fun enterUnnamedEvent(ctx: AdventureParser.UnnamedEventContext?) {
        symbolTable.push(mutableMapOf())
    }

    override fun exitUnnamedEvent(ctx: AdventureParser.UnnamedEventContext?) {
        symbolTable.pop()
    }

    override fun enterStatementBlock(ctx: AdventureParser.StatementBlockContext?) {
        symbolTable.push(mutableMapOf())
    }

    override fun exitStatementBlock(ctx: AdventureParser.StatementBlockContext?) {
        symbolTable.pop()
    }

    override fun enterBranch(ctx: AdventureParser.BranchContext?) {
        symbolTable.push(mutableMapOf())
    }

    override fun exitBranch(ctx: AdventureParser.BranchContext?) {
        symbolTable.pop()
    }
}