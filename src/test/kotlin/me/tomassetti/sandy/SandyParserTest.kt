package me.tomassetti.sandy

import me.tomassetti.langsandbox.SandyLexer
import me.tomassetti.langsandbox.SandyParser
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import java.io.StringReader
import java.util.*
import kotlin.test.assertEquals
import org.junit.Test as test

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

    fun parseResource(resourceName: String) : SandyParser.SandyFileContext = SandyParser(CommonTokenStream(lexerForResource(resourceName))).sandyFile()

    @org.junit.Test fun parseAdditionAssignment() {
        assertEquals(
"""SandyFile
  Line
    AssignmentStatement
      Assignment
        T[a]
        T[=]
        BinaryOperation
          IntLiteral
            T[1]
          T[+]
          IntLiteral
            T[2]
    T[<EOF>]
""",
                toParseTree(parseResource("addition_assignment")).multiLineString())
    }

    @org.junit.Test fun parseSimplestVarDecl() {
        assertEquals(
"""SandyFile
  Line
    VarDeclarationStatement
      VarDeclaration
        T[var]
        Assignment
          T[a]
          T[=]
          IntLiteral
            T[1]
    T[<EOF>]
""",
                toParseTree(parseResource("simplest_var_decl")).multiLineString())
    }

    @org.junit.Test fun parsePrecedenceExpressions() {
        assertEquals(
"""SandyFile
  Line
    VarDeclarationStatement
      VarDeclaration
        T[var]
        Assignment
          T[a]
          T[=]
          BinaryOperation
            BinaryOperation
              IntLiteral
                T[1]
              T[+]
              BinaryOperation
                IntLiteral
                  T[2]
                T[*]
                IntLiteral
                  T[3]
            T[-]
            IntLiteral
              T[4]
    T[<EOF>]
""",
                toParseTree(parseResource("precedence_expression")).multiLineString())
    }


}
