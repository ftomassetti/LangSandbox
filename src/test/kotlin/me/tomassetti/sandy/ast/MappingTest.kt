package me.tomassetti.sandy.ast

import me.tomassetti.sandy.parsing.SandyAntlrParserFacade
import kotlin.test.assertEquals
import org.junit.Test as test

class MappingTest {

    @test fun mapSimpleFileWithoutPositions() {
        val code = """var a = 1 + 2
                     |a = 7 * (2 / 3)""".trimMargin("|")
        val ast = SandyAntlrParserFacade.parse(code).root!!.toAst()
        val expectedAst = SandyFile(listOf(
                VarDeclaration("a", SumExpression(IntLit("1"), IntLit("2"))),
                Assignment("a", MultiplicationExpression(
                        IntLit("7"),
                        DivisionExpression(
                                IntLit("2"),
                                IntLit("3"))))))
        assertEquals(expectedAst, ast)
    }

    @test fun mapSimpleFileWithPositions() {
        val code = """var a = 1 + 2
                     |a = 7 * (2 / 3)""".trimMargin("|")
        val ast = SandyAntlrParserFacade.parse(code).root!!.toAst(considerPosition = true)
        val expectedAst = SandyFile(listOf(
                VarDeclaration("a",
                        SumExpression(
                                IntLit("1", pos(1,8,1,9)),
                                IntLit("2", pos(1,12,1,13)),
                                pos(1,8,1,13)),
                        pos(1,0,1,13)),
                Assignment("a",
                        MultiplicationExpression(
                            IntLit("7", pos(2,4,2,5)),
                            DivisionExpression(
                                    IntLit("2", pos(2,9,2,10)),
                                    IntLit("3", pos(2,13,2,14)),
                                    pos(2,9,2,14)),
                            pos(2,4,2,15)),
                        pos(2,0,2,15))),
                pos(1,0,2,15))
        assertEquals(expectedAst, ast)
    }

    @test fun mapCastInt() {
        val code = "a = 7 as Int"
        val ast = SandyAntlrParserFacade.parse(code).root!!.toAst()
        val expectedAst = SandyFile(listOf(Assignment("a", TypeConversion(IntLit("7"), IntType()))))
        assertEquals(expectedAst, ast)
    }

    @test fun mapCastDecimal() {
        val code = "a = 7 as Decimal"
        val ast = SandyAntlrParserFacade.parse(code).root!!.toAst()
        val expectedAst = SandyFile(listOf(Assignment("a", TypeConversion(IntLit("7"), DecimalType()))))
        assertEquals(expectedAst, ast)
    }

    @test fun mapPrint() {
        val code = "print(a)"
        val ast = SandyAntlrParserFacade.parse(code).root!!.toAst()
        val expectedAst = SandyFile(listOf(Print(VarReference("a"))))
        assertEquals(expectedAst, ast)
    }

}
