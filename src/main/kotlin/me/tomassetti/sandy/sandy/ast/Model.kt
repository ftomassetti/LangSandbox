package me.tomassetti.sandy.sandy.ast

import java.util.*
import kotlin.reflect.KParameter
import kotlin.reflect.memberProperties
import kotlin.reflect.primaryConstructor

//
// Generic part: valid for all languages
//

interface Node

fun Node.process(operation: (Node) -> Unit) {
    operation(this)
    this.javaClass.kotlin.memberProperties.forEach { p ->
        val v = p.get(this)
        when (v) {
            is Node -> v.process(operation)
            is Collection<*> -> v.forEach { if (it is Node) it.process(operation) }
        }
    }
}

fun Node.transform(operation: (Node) -> Node) : Node {
    operation(this)
    val changes = HashMap<String, Any>()
    this.javaClass.kotlin.memberProperties.forEach { p ->
        val v = p.get(this)
        when (v) {
            is Node -> {
                val newValue = v.transform(operation)
                if (newValue != v) changes[p.name] = newValue
            }
            is Collection<*> -> {
                val newValue = v.map { if (it is Node) it.transform(operation) else it }
                if (newValue != v) changes[p.name] = newValue
            }
        }
    }
    var instanceToTransform = this
    if (!changes.isEmpty()) {
        val constructor = this.javaClass.kotlin.primaryConstructor!!
        val params = HashMap<KParameter, Any?>()
        constructor.parameters.forEach { param ->
            if (changes.containsKey(param.name)) {
                params[param] = changes[param.name]
            } else {
                params[param] = this.javaClass.kotlin.memberProperties.find { param.name == it.name }!!.get(this)
            }
        }
        instanceToTransform = constructor.callBy(params)
    }
    return operation(instanceToTransform)
}

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

object IntType : Type

object DecimalType : Type

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
