import com.example.AdventureLexer
import com.example.AdventureParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.io.File
import kotlin.io.path.Path
import kotlin.text.StringBuilder


fun main() {
    executeStory()

    val parser = AdventureParser(CommonTokenStream(AdventureLexer(CharStreams.fromPath(Path("example.txt")))))
    val tree = parser.adventure()
    val generatedCode = StringBuilder()
    val listener = KotlinGeneratorListener(generatedCode)
    ParseTreeWalker.DEFAULT.walk(listener, tree)
    File("src/main/kotlin/Generated.kt").writeText(generatedCode.toString())
}