import adventure.AdventureLexer
import adventure.AdventureParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.io.File
import kotlin.io.path.Path

//TODO: document item functions being "hidden" features
//TODO: nem lehet specifikus akciókhoz "költeni" item-eket. Ez nem baj, arra valók a közönséges változók! Leírni, hogy ezt mi motiválja!
//TODO: tényleges build rendszer a generált kódnak
//TODO: document  MainMenu (és hogy miért szerepel mindenhol)
//TODO: főmenüvel lehet csalni, mert akárhány item akciót egynek számol. megindokolni hogy ezzel miért nem foglalkozok
//TODO: vagy kikötöm, hogy a főmenüt ne lehessen akárhol megynitni. megéri?
//TODO: dokumentálni, hogy az after() miatt vannak konstansok az event nevével
//TODO: dokumentálni az after() warning-ját
//TODO: dicument toString() on item, location
//TODO: NO CODE INJECTION IN EXPRESSIONS, unless directly under condsitions block --> expressions are outsourced to Kotlin, and @[ ]@ caanot be implemented as functions

fun main() {
    while(true) {
        val sourceLastModified = File("example.txt").lastModified()
        generateKotlin("example.txt", "src/main/kotlin/Generated.kt")
        while (File("example.txt").lastModified() <= sourceLastModified) {
            Thread.sleep(5)
        }
        println()
    }
}

fun generateKotlin(inFile: String, outFile: String) {
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
    } else {
        println("Errors in source. Exiting.")
    }
}
