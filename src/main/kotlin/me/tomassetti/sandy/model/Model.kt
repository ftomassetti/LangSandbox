package me.tomassetti.sandy.model

data class SandyFile(val statements : List<Statement>)

interface Statement { }

interface Expression { }

interface Type { }

//
// Types
//

object IntType : Type

object DecimalType : Type

//
// Expressions
//

open class BinaryExpression(val left: Expression, val right: Expression) : Expression

class SumExpression(left: Expression, right: Expression) : BinaryExpression(left, right)

class SubtractionExpression(left: Expression, right: Expression) : BinaryExpression(left, right)

class MultiplicationExpression(left: Expression, right: Expression) : BinaryExpression(left, right)

class DivisionExpression(left: Expression, right: Expression) : BinaryExpression(left, right)

class UnaryMinusExpression(value: Expression) : Expression

class TypeConversion(value: Expression, targetType: Type) : Expression

class VarReference(val varName: String) : Expression

class IntLit(val value: String) : Expression

class DecLit(val value: String) : Expression

//
// Statements
//

data class VarDeclaration(val varName: String, val value: Expression) : Statement

data class Assignment(val varName: String, val value: Expression) : Statement

data class Print(val value: Expression) : Statement
