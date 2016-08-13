package me.tomassetti.sandy

import me.tomassetti.langsandbox.SandyLexer
import org.antlr.v4.runtime.ANTLRInputStream
import java.io.StringReader
import java.util.*
import kotlin.test.assertEquals
import org.junit.Test as test

class SandyLexerTest {

    fun lexerForCode(code: String) = SandyLexer(ANTLRInputStream(StringReader(code)))

    fun tokens(lexer: SandyLexer): List<String> {
        val tokens = LinkedList<String>()
        do {
           val t = lexer.nextToken()
            when (t.type) {
                -1 -> tokens.add("EOF")
                else -> if (t.type != SandyLexer.WS) tokens.add(lexer.ruleNames[t.type - 1])
            }
        } while (t.type != -1)
        return tokens
    }

    @org.junit.Test fun parseVarDeclarationAssignedAnIntegerLiteral() {
        assertEquals(listOf("VAR", "ID", "ASSIGN", "INTLIT", "EOF"),
                tokens(lexerForCode("var a = 1")))
    }

    @org.junit.Test fun parseVarDeclarationAssignedADecimalLiteral() {
        assertEquals(listOf("VAR", "ID", "ASSIGN", "DECLIT", "EOF"),
                tokens(lexerForCode("var a = 1.23")))
    }

    @org.junit.Test fun parseVarDeclarationAssignedASum() {
        assertEquals(listOf("VAR", "ID", "ASSIGN", "INTLIT", "PLUS", "INTLIT", "EOF"),
                tokens(lexerForCode("var a = 1 + 2")))
    }

    @org.junit.Test fun parseMathematicalExpression() {
        assertEquals(listOf("INTLIT", "PLUS", "ID", "ASTERISK", "INTLIT", "DIVISION", "INTLIT", "MINUS", "INTLIT", "EOF"),
                tokens(lexerForCode("1 + a * 3 / 4 - 5")))
    }

    @org.junit.Test fun parseMathematicalExpressionWithParenthesis() {
        assertEquals(listOf("INTLIT", "PLUS", "LPAREN", "ID", "ASTERISK", "INTLIT", "RPAREN", "MINUS", "DECLIT", "EOF"),
                tokens(lexerForCode("1 + (a * 3) - 5.12")))
    }

    @org.junit.Test fun parseCast() {
        assertEquals(listOf("ID", "ASSIGN", "ID", "AS", "INT", "EOF"),
                tokens(lexerForCode("a = b as Int")))
    }
}