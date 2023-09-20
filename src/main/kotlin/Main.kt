import com.example.AdventureLexer
import com.example.AdventureParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import kotlin.io.path.Path


fun main() {
    executeStory()
    return

    val parser = AdventureParser(CommonTokenStream(AdventureLexer(CharStreams.fromPath(Path("example.txt")))))
    val tree = parser.adventure()
    val listener = KotlinGeneratorListener()
    ParseTreeWalker.DEFAULT.walk(listener, tree)

}

object adventureKotlinCompiler {
    fun emitAdventureBegin() {
        print("""
                interface Location {
                    val here get() = this
                    fun execute()
                }

                class LocationChangeException(val newLocation: Location) : Exception()
                class GameOverException : Exception()

                fun end() {
                   throw GameOverException()
                }

                fun executeStory() {
                    var here : Location = introduction
                    while (true) {
                        try {
                            here.execute()
                            return
                        }
                        catch (ex : LocationChangeException) {
                            here = ex.newLocation
                        }
                        catch (ex: GameOverException) {
                            println(">>>GAME OVER")
                            print("Would you like to restart? y/N")
                            if (readln() == "y") {
                                here = introduction
                                continue
                            }
                            else {
                                return
                            }
                        }
                    }
                }
        """.trimIndent())
    }
    fun emitPrint(text: String, keepGoing: Boolean, replacePrevious: Boolean) {
        if (replacePrevious) print("print(\"\\r$text\")\n")
        else print("print(\"$text\")\n")
    }
}