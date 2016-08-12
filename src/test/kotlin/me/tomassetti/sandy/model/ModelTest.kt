package me.tomassetti.sandy.model

import me.tomassetti.sandy.sandy.model.*
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

}