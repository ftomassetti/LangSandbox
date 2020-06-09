package me.tomassetti.sandy.ast

import me.tomassetti.sandy.parsing.SandyAntlrParserFacade
import java.util.*
import kotlin.test.assertEquals
import org.junit.Test as test

class ModelTest {

    @test fun transformVarName() {
        val startTree = SandyFile(listOf(
                VarDeclaration("A", IntLit("10")),
                Assignment("A", IntLit("11")),
                Print(VarReference("A"))))
        val expectedTransformedTree = SandyFile(listOf(
                VarDeclaration("B", IntLit("10")),
                Assignment("B", IntLit("11")),
                Print(VarReference("B"))))
        assertEquals(expectedTransformedTree, startTree.transform {
            when (it) {
                is VarDeclaration -> VarDeclaration("B", it.value)
                is VarReference -> VarReference("B")
                is Assignment -> Assignment("B", it.value)
                else -> it
            }
        })
    }

    fun toAst(code: String) : SandyFile = SandyAntlrParserFacade.parse(code).root!!.toAst()

    @test fun processAllVarDeclarations() {
        val ast = toAst("""var a = 1
                          |a = 2 * 5
                          |var b = a
                          |print(b)
                          |var c = b * b""".trimMargin("|"))
        val varDeclarations = LinkedList<String>()
        ast.specificProcess(VarDeclaration::class.java, { varDeclarations.add(it.varName) })
        assertEquals(listOf("a", "b", "c"), varDeclarations)
    }

}
