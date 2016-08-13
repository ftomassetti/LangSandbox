package me.tomassetti.sandy.ast

data class Error(val message: String, val position: Point)

fun SandyFile.validate() : List<Error> {
    return emptyList<Error>()
}