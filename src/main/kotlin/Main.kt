import com.example.AdventureLexer
import com.example.AdventureParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.tree.TerminalNode
import java.io.File
import kotlin.io.path.Path


fun main() {

    val parser = AdventureParser(CommonTokenStream(AdventureLexer(CharStreams.fromPath(Path("example.txt")))))
    val tree = parser.adventure()
    println("Parsing complete")

    val vomitter = VomitListener(StringBuilder())
    ParseTreeWalker.DEFAULT.walk(vomitter, tree)
    //print(vomitter.output)

    print("Testing vomit and re-digest: ")
    val tree2 = AdventureParser(CommonTokenStream(AdventureLexer(CharStreams.fromString(vomitter.output.toString())))).adventure()
    val vomitCheck = compareParseTrees(tree, tree2)
    if (vomitCheck) {
        println("trees match")
    } else {
        println("trees don't match")
    }
    val list2 = VomitListener(StringBuilder())
    ParseTreeWalker.DEFAULT.walk(list2, tree2)

    println("Doing semantic analysis...")
    val checker = SemanticAnalyzingListener()
    ParseTreeWalker.DEFAULT.walk(checker, tree)

    if (vomitCheck && !checker.error && !checker.warning) {
        println("Everything is OK. Generating file...")

        val generator = KotlinGeneratorListener(StringBuilder(), checker.symbolTable)
        ParseTreeWalker.DEFAULT.walk(generator, tree)
        File("src/main/kotlin/Generated.kt").writeText(generator.output.toString())
        println("Generation finished without error.")
    } else {
        println(vomitter.output.toString())
    }
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