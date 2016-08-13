package me.tomassetti.sandy.ast

import me.tomassetti.sandy.sandy.ast.*
import me.tomassetti.sandy.sandy.parsing.SandyParserFacade
import kotlin.test.assertEquals
import org.junit.Test as test

class MappingTest {

    @test fun mapSimpleFile() {
        val code = """var a = 1 + 2
                     |a = 7 * (2 / 3)""".trimMargin("|")
        val ast = SandyParserFacade.parse(code).root!!.toAst()
        val expectedAst = SandyFile(listOf(
                VarDeclaration("a", SumExpression(IntLit("1"), IntLit("2"))),
                Assignment("a", MultiplicationExpression(
                        IntLit("7"),
                        DivisionExpression(
                                IntLit("2"),
                                IntLit("3"))))))
        assertEquals(expectedAst, ast)
    }

    @test fun mapCastInt() {
        val code = "a = 7 as Int"
        val ast = SandyParserFacade.parse(code).root!!.toAst()
        val expectedAst = SandyFile(listOf(Assignment("a", TypeConversion(IntLit("7"), IntType))))
        assertEquals(expectedAst, ast)
    }

    @test fun mapCastDecimal() {
        val code = "a = 7 as Decimal"
        val ast = SandyParserFacade.parse(code).root!!.toAst()
        val expectedAst = SandyFile(listOf(Assignment("a", TypeConversion(IntLit("7"), DecimalType))))
        assertEquals(expectedAst, ast)
    }

    @test fun mapPrint() {
        val code = "print(a)"
        val ast = SandyParserFacade.parse(code).root!!.toAst()
        val expectedAst = SandyFile(listOf(Print(VarReference("a"))))
        assertEquals(expectedAst, ast)
    }

}