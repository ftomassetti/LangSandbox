package me.tomassetti.sandy.sandy.ast

//
// Generic part: valid for all languages
//

interface Node {
    val position: Position?
}

data class Point(val line: Int, val column: Int)

data class Position(val start: Point, val end: Point)

fun pos(startLine:Int, startCol:Int, endLine:Int, endCol:Int) = Position(Point(startLine,startCol),Point(endLine,endCol))

//
// Sandy specific part
//

data class SandyFile(val statements : List<Statement>, override val position: Position? = null) : Node

interface Statement : Node { }

interface Expression : Node { }

interface Type : Node { }

//
// Types
//

data class IntType(override val position: Position? = null) : Type

data class DecimalType(override val position: Position? = null) : Type

//
// Expressions
//

interface BinaryExpression : Expression {
    val left: Expression
    val right: Expression
}

data class SumExpression(override val left: Expression, override val right: Expression, override val position: Position? = null) : BinaryExpression

data class SubtractionExpression(override val left: Expression, override val right: Expression, override val position: Position? = null) : BinaryExpression

data class MultiplicationExpression(override val left: Expression, override val right: Expression, override val position: Position? = null) : BinaryExpression

data class DivisionExpression(override val left: Expression, override val right: Expression, override val position: Position? = null) : BinaryExpression

data class UnaryMinusExpression(val value: Expression, override val position: Position? = null) : Expression

data class TypeConversion(val value: Expression, val targetType: Type, override val position: Position? = null) : Expression

data class VarReference(val varName: String, override val position: Position? = null) : Expression

data class IntLit(val value: String, override val position: Position? = null) : Expression

data class DecLit(val value: String, override val position: Position? = null) : Expression

//
// Statements
//

data class VarDeclaration(val varName: String, val value: Expression, override val position: Position? = null) : Statement

data class Assignment(val varName: String, val value: Expression, override val position: Position? = null) : Statement

data class Print(val value: Expression, override val position: Position? = null) : Statement
