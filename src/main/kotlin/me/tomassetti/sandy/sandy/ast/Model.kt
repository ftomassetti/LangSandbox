package me.tomassetti.sandy.sandy.ast

//
// Generic part: valid for all languages
//

interface Node

//
// Sandy specific part
//

data class SandyFile(val statements : List<Statement>) : Node

interface Statement : Node { }

interface Expression : Node { }

interface Type : Node { }

//
// Types
//

object IntType : Type {
    override fun toString() = "IntType"
}

object DecimalType : Type {
    override fun toString() = "DecimalType"
}

//
// Expressions
//

interface BinaryExpression : Expression {
    val left: Expression
    val right: Expression
}

data class SumExpression(override val left: Expression, override val right: Expression) : BinaryExpression

data class SubtractionExpression(override val left: Expression, override val right: Expression) : BinaryExpression

data class MultiplicationExpression(override val left: Expression, override val right: Expression) : BinaryExpression

data class DivisionExpression(override val left: Expression, override val right: Expression) : BinaryExpression

data class UnaryMinusExpression(val value: Expression) : Expression

data class TypeConversion(val value: Expression, val targetType: Type) : Expression

data class VarReference(val varName: String) : Expression

data class IntLit(val value: String) : Expression

data class DecLit(val value: String) : Expression

//
// Statements
//

data class VarDeclaration(val varName: String, val value: Expression) : Statement

data class Assignment(val varName: String, val value: Expression) : Statement

data class Print(val value: Expression) : Statement
