import com.example.AdventureBaseListener
import com.example.AdventureLexer
import com.example.AdventureParser
import com.example.AdventureParser.AdventureContext
import org.antlr.v4.runtime.RuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode
import java.util.*

class SemanticAnalyzingListener : AdventureBaseListener() {
    var error = false
    var warning = false

    private val symbolTable = Stack<MutableSet<String>>()
    init {
        symbolTable.push(mutableSetOf())
    }

    private fun isSymbolDefined(name: String): Boolean {
        // Check if the symbol is defined in any of the active scopes (from innermost to outermost)
        for (symbolTable in symbolTable.asReversed()) {
            if (symbolTable.contains(name)) {
                return true
            }
        }
        return false
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
    override fun enterAdventure(ctx: AdventureParser.AdventureContext?) {
        ctx?.children?.filterIsInstance<AdventureParser.LocationContext>()?.forEach {
            if (symbolTable.peek().contains(it.ID().text)) {
                errorAlreadyDefined(it.ID().symbol)
            } else {
                symbolTable.peek().add(it.ID().text)
            }
        }
        ctx?.children?.filterIsInstance<AdventureParser.Named_eventContext>()?.forEach {
            if (symbolTable.peek().contains(it.text)) {
                ctx.start.line
                errorAlreadyDefined(it.ID().symbol)
            } else {
                symbolTable.peek().add(it.ID().text)
            }
        }
    }
    override fun enterVariable(ctx: AdventureParser.VariableContext?) {
        val name = ctx?.ID()?.text!!
        if (symbolTable.peek().contains(name)) {
            errorAlreadyDefined(ctx.ID().symbol)
        } else {
            symbolTable.peek().add(name)
        }
    }

    override fun visitTerminal(node: TerminalNode?) {
        if (node?.symbol?.type == AdventureLexer.ID) {
            if (
                node.parent is AdventureParser.LiteralContext ||
                node.parent is AdventureParser.Trigger_eventContext ||
                node.parent is AdventureParser.Jump_locationContext
            ) {
                val name = node.text
                if (!isSymbolDefined(name)) {
                    errorNotDefined(node.symbol)
                }
            }
        }
    }


    private fun findEnclosingEvent(ctx: RuleContext): RuleContext? {
        var parentCtx: RuleContext = ctx
        while (parentCtx !is AdventureParser.AdventureContext) {
            if (parentCtx is AdventureParser.Unnamed_eventContext || parentCtx is AdventureParser.Named_eventContext) {
                return parentCtx
            }
            parentCtx = parentCtx.parent
        }
        return null
    }
    override fun enterFinish_event(ctx: AdventureParser.Finish_eventContext?) {
        val theEvent = findEnclosingEvent(ctx!!)
        if (theEvent == null) {
            val token = ctx.FINISH_EVENT().symbol
            printError(token, "${token.text} can only be used in event definitions.")
        }
    }

    override fun enterUntrigger_event(ctx: AdventureParser.Untrigger_eventContext?) {
        val theEvent = findEnclosingEvent(ctx!!)
        if (theEvent == null || theEvent !is AdventureParser.Unnamed_eventContext || theEvent.STORY() == null) {
            printWarning(ctx.start, "${ctx.UNTRIGGER()} is not inside a story event. Are you sure?")
        }
    }

    override fun enterLocation(ctx: AdventureParser.LocationContext?) {
        symbolTable.push(mutableSetOf())
    }

    override fun exitLocation(ctx: AdventureParser.LocationContext?) {
        symbolTable.pop()
    }

    override fun enterNamed_event(ctx: AdventureParser.Named_eventContext?) {
        symbolTable.push(mutableSetOf())
    }

    override fun exitNamed_event(ctx: AdventureParser.Named_eventContext?) {
        symbolTable.pop()
    }

    override fun enterUnnamed_event(ctx: AdventureParser.Unnamed_eventContext?) {
        symbolTable.push(mutableSetOf())
    }

    override fun exitUnnamed_event(ctx: AdventureParser.Unnamed_eventContext?) {
        symbolTable.pop()
    }

    override fun enterStatement_block(ctx: AdventureParser.Statement_blockContext?) {
        symbolTable.push(mutableSetOf())
    }

    override fun exitStatement_block(ctx: AdventureParser.Statement_blockContext?) {
        symbolTable.pop()
    }

    override fun enterBranch(ctx: AdventureParser.BranchContext?) {
        symbolTable.push(mutableSetOf())
    }

    override fun exitBranch(ctx: AdventureParser.BranchContext?) {
        symbolTable.pop()
    }
}