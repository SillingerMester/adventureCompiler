import com.example.AdventureParser
import java.util.*

class SymbolTable {
    fun peek(): MutableMap<String, ExpressionType> = symbolTable.peek()
    fun pop(): MutableMap<String, ExpressionType> = symbolTable.pop()
    fun push(): MutableMap<String, ExpressionType> = symbolTable.push(mutableMapOf())
    enum class ExpressionType {
        INT, STRING, BOOL, LOCATION, EVENT, ITEM, UNDEFINED;
        val kotlinName get() = when (this) {
            ExpressionType.INT -> ": Int"
            ExpressionType.STRING -> ": String"
            ExpressionType.BOOL -> ": Boolean"
            ExpressionType.LOCATION -> ":Location"
            ExpressionType.EVENT -> ""
            ExpressionType.ITEM -> ": Item"
            ExpressionType.UNDEFINED -> ": Any"
        }
    }

    val symbolTable = Stack<MutableMap<String, ExpressionType>>()
    init {
        symbolTable.push(mutableMapOf())
    }

    fun getSymbolType(name: String): ExpressionType {
        // Check if the symbol is defined in any of the active scopes (from innermost to outermost)
        for (scope in symbolTable.asReversed()) {
            if (scope.containsKey(name)) {
                return scope[name] ?: ExpressionType.UNDEFINED
            }
        }
        return ExpressionType.UNDEFINED
    }

    fun getExpressionType(ctx: AdventureParser.ExpressionContext): ExpressionType {
        if(ctx.boolExpression() != null) {
            return ExpressionType.BOOL
        }
        if (ctx.intExpression() != null) {
            return ExpressionType.INT
        }
        if (ctx.otherExpression() != null) {
            val actualExpression = ctx.otherExpression().otherExpressionU()
            if (actualExpression.STRING() != null || actualExpression.inputText() != null) {
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
        if (ctx.implicitTypedExpr() != null) {
            return getSymbolType(ctx.implicitTypedExpr().ID().text)
        }
        return ExpressionType.UNDEFINED
    }
}