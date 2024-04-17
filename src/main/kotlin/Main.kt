import adventure.AdventureLexer
import adventure.AdventureParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.io.File
import kotlin.io.path.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    println("Adventure compiler args=[${args.joinToString(", ")}]")
    val helpText = "Usage: adventureCompiler <input file> [<output file>]"
    if (args.contains("--help") || args.contains("-h")) {
        println(helpText)
        exitProcess(1)
    }
    when(args.size) {
        0 -> {
            println(helpText)
            exitProcess(1)
        }
        1 -> generateKotlin(args[0], "Generated.kt")
        2 -> generateKotlin(args[0], args[1])
        else -> {
            println(helpText)
            exitProcess(1)
        }
    }
}

fun generateKotlin(inFile: String, outFile: String): Boolean {
    val parser = AdventureParser(CommonTokenStream(AdventureLexer(CharStreams.fromPath(Path(inFile)))))
    val tree = parser.adventure()
    println("Parsing complete")

    println("Analyzing...")
    val checker = SemanticAnalyzingListener()
    ParseTreeWalker.DEFAULT.walk(checker, tree)

    if (!checker.error && !checker.warning) {
        println("Everything is OK. Generating file...")

        val generator = KotlinGeneratorListener(StringBuilder())
        ParseTreeWalker.DEFAULT.walk(generator, tree)
        File(outFile).writeText(generator.output.toString())
        println("Generation finished without error.")
        return true
    } else {
        println("Errors in source. Exiting.")
        return false
    }
}
