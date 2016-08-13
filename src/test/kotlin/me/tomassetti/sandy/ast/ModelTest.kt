package me.tomassetti.sandy.ast

import me.tomassetti.sandy.sandy.ast.*
import kotlin.test.assertEquals
import org.junit.Test as test

class ModelTest {

    @org.junit.Test fun transformVarName() {
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

}