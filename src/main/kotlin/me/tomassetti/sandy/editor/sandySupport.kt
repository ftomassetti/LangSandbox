package me.tomassetti.sandy.editor

import me.tomassetti.kanvas.*
import me.tomassetti.kolasu.parsing.Parser
import me.tomassetti.langsandbox.SandyLexer
import me.tomassetti.sandy.ast.SandyFile
import org.antlr.v4.runtime.Lexer
import org.fife.ui.rsyntaxtextarea.Style
import org.fife.ui.rsyntaxtextarea.SyntaxScheme
import java.awt.Color

object SandySyntaxScheme : SyntaxScheme(true) {
    override fun getStyle(index: Int): Style {
        val style = Style()
        val color = when (index) {
            SandyLexer.VAR -> Color.GREEN
            SandyLexer.ASSIGN -> Color.GREEN
            SandyLexer.ASTERISK, SandyLexer.DIVISION, SandyLexer.PLUS, SandyLexer.MINUS -> Color.WHITE
            SandyLexer.INTLIT, SandyLexer.DECLIT -> Color.BLUE
            SandyLexer.UNMATCHED -> Color.RED
            SandyLexer.ID -> Color.MAGENTA
            SandyLexer.LPAREN, SandyLexer.RPAREN -> Color.WHITE
            else -> null
        }
        if (color != null) {
            style.foreground = color
        }
        return style
    }
}

object SandyLanguageSupport : BaseLanguageSupport<SandyFile>() {
    override val syntaxScheme: SyntaxScheme
        get() = SandySyntaxScheme
    override val antlrLexerFactory: AntlrLexerFactory
        get() = object : AntlrLexerFactory {
            override fun create(code: String): Lexer = SandyLexer(org.antlr.v4.runtime.ANTLRInputStream(code))
        }
    override val parser: Parser<SandyFile>
        get() = TODO("Not yet implemented")
    override val parserData: ParserData?
        get() = TODO("Not yet implemented")
}