package me.tomassetti.sandy.parsing

import me.tomassetti.langsandbox.SandyLexer
import me.tomassetti.langsandbox.SandyParser
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import java.io.File
import java.io.FileInputStream
import java.util.*

data class Error(val message: String)

data class ParsingResult(val root : SandyParser.SandyFileContext?, val errors: List<Error>) {
    fun isCorrect() = errors.isEmpty() && root != null
}

object SandyParserFacade {

    fun parse(file: File) : ParsingResult {
        val errors = LinkedList<Error>()
        val errorListener = object : ANTLRErrorListener {
            override fun reportAmbiguity(p0: Parser?, p1: DFA?, p2: Int, p3: Int, p4: Boolean, p5: BitSet?, p6: ATNConfigSet?) {
                // Ignored for now
            }

            override fun reportAttemptingFullContext(p0: Parser?, p1: DFA?, p2: Int, p3: Int, p4: BitSet?, p5: ATNConfigSet?) {
                // Ignored for now
            }

            override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any, line: Int, charPositionInline: Int, msg: String, ex: RecognitionException?) {
                errors.add(Error("Error at L$line:$charPositionInline: $msg"))
            }

            override fun reportContextSensitivity(p0: Parser?, p1: DFA?, p2: Int, p3: Int, p4: Int, p5: ATNConfigSet?) {
                // Ignored for now
            }
        }

        val lexer = SandyLexer(ANTLRInputStream(FileInputStream(file)))
        lexer.removeErrorListeners()
        lexer.addErrorListener(errorListener)
        val parser = SandyParser(CommonTokenStream(lexer))
        parser.removeErrorListeners()
        parser.addErrorListener(errorListener)
        val root = parser.sandyFile()
        return ParsingResult(root, errors)
    }

}