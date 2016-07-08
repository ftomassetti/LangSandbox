package me.tomassetti.sandy

import me.tomassetti.langsandbox.SandyLexer
import me.tomassetti.langsandbox.SandyParser
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import java.io.*
import java.util.*
import org.junit.Test as test
import kotlin.test.*

class SandyParserTest {

    fun lexerForCode(code: String) = SandyLexer(ANTLRInputStream(StringReader(code)))

    fun lexerForResource(resourceName: String) = SandyLexer(ANTLRInputStream(this.javaClass.getResourceAsStream("/${resourceName}.sandy")))

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

    fun parse(lexer: SandyLexer) : SandyParser.SandyFileContext = SandyParser(CommonTokenStream(lexer)).sandyFile()

    @test fun parseAdditionAssignment() {
        println(parse(lexerForResource("addition_assignment")))
    }

    @test fun parseSimplestVarDecl() {
        println(parse(lexerForResource("simplest_var_decl")))
    }

    @test fun parsePrecedenceExpressions() {
        println(parse(lexerForResource("precedence_expression")))
    }


}