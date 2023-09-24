import com.example.AdventureLexer
import com.example.AdventureParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.io.File
import kotlin.io.path.Path


fun main() {
    //executeStory()

    val parser = AdventureParser(CommonTokenStream(AdventureLexer(CharStreams.fromPath(Path("example.txt")))))
    val tree = parser.adventure()
    val generatedCode = StringBuilder()
    val generator = KotlinGeneratorListener(generatedCode)
    val checker = SemanticAnalyzingListener()
    val vomitter = VomitListener(StringBuilder())
    ParseTreeWalker.DEFAULT.walk(checker, tree)

    if (!checker.error && !checker.warning) {
        ParseTreeWalker.DEFAULT.walk(generator, tree)
        File("src/main/kotlin/Generated.kt").writeText(generatedCode.toString())
    }
    ParseTreeWalker.DEFAULT.walk(vomitter, tree)
    print(vomitter.output)
}

