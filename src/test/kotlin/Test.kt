import adventure.AdventureLexer
import adventure.AdventureParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.tree.TerminalNode
import java.io.File
import kotlin.io.path.Path

fun main() {
    Generated.executeStory()
}



fun reDigestTest(inFile: String, outFile: String) {
    val parser = AdventureParser(CommonTokenStream(AdventureLexer(CharStreams.fromPath(Path(inFile)))))
    val tree = parser.adventure()
    println("Parsing complete")

    val listener = VomitListener(StringBuilder())
    ParseTreeWalker.DEFAULT.walk(listener, tree)
    File(outFile).writeText(listener.output.toString())

    print("Testing vomit and re-digest: ")
    val tree2 = AdventureParser(CommonTokenStream(AdventureLexer(CharStreams.fromString(listener.output.toString())))).adventure()
    val vomitCheck = compareParseTrees(tree, tree2)
    if (vomitCheck) {
        println("trees match")
    } else {
        println("trees don't match")
    }
    val list2 = VomitListener(StringBuilder())
    ParseTreeWalker.DEFAULT.walk(list2, tree2)
}

fun compareParseTrees(tree1: ParserRuleContext, tree2: ParserRuleContext): Boolean {
    if (tree1::class != tree2::class || tree1.children.size != tree2.children.size) {
        return false
    }

    for (it in 0..<tree1.children.size) {
        val child1 = tree1.children[it]
        val child2 = tree2.children[it]

        if (child1 is TerminalNode && child2 is TerminalNode) {
            if (child1.symbol.text != child1.symbol.text) {
                return false
            }
            else {
                continue
            }
        }

        if (!compareParseTrees(child1 as ParserRuleContext, child2 as ParserRuleContext)) {
            return false
        }
    }
    return true
}