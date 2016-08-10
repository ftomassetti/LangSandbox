package me.tomassetti.sandy.model

data class SandyFile(val statements : List<Statement>)

interface Node {
    fun process(operation:(Node)->Unit) : Unit
    fun transform(operation:(Node)->Node) : Node
}

interface Statement : Node { }

interface Expression : Node { }

interface Type : Node { }

//
// Types
//

object IntType : Type {
    override fun process(operation: (Node) -> Unit) { operation(this) }
    override fun transform(operation: (Node) -> Node): Node = operation(this)
}

object DecimalType : Type {
    override fun process(operation: (Node) -> Unit) { operation(this) }
    override fun transform(operation: (Node) -> Node): Node = operation(this)
}

//
// Expressions
//

open abstract class BinaryExpression(val left: Expression, val right: Expression) : Expression {
    override fun process(operation: (Node) -> Unit) {
        operation(this)
        this.left.process(operation)
        this.right.process(operation)
    }
}

class SumExpression(left: Expression, right: Expression) : BinaryExpression(left, right) {
    override fun transform(operation: (Node) -> Node): Node {
        return operation(SumExpression(operation(this.left) as Expression, operation(this.right) as Expression))
    }
}

class SubtractionExpression(left: Expression, right: Expression) : BinaryExpression(left, right) {
    override fun transform(operation: (Node) -> Node): Node {
        return operation(SubtractionExpression(operation(this.left) as Expression, operation(this.right) as Expression))
    }
}

class MultiplicationExpression(left: Expression, right: Expression) : BinaryExpression(left, right) {
    override fun transform(operation: (Node) -> Node): Node {
        return operation(MultiplicationExpression(operation(this.left) as Expression, operation(this.right) as Expression))
    }
}

class DivisionExpression(left: Expression, right: Expression) : BinaryExpression(left, right) {
    override fun transform(operation: (Node) -> Node): Node {
        return operation(DivisionExpression(operation(this.left) as Expression, operation(this.right) as Expression))
    }
}

class UnaryMinusExpression(val value: Expression) : Expression {
    override fun process(operation: (Node) -> Unit) {
        operation(this)
        value.process(operation)
    }

    override fun transform(operation: (Node) -> Node): Node {
        return operation(UnaryMinusExpression(operation(this.value) as Expression))
    }
}

class TypeConversion(val value: Expression, val targetType: Type) : Expression {
    override fun process(operation: (Node) -> Unit) {
        operation(this)
        value.process(operation)
    }

    override fun transform(operation: (Node) -> Node): Node {
        return operation(UnaryMinusExpression(operation(this.value) as Expression))
    }
}

class VarReference(val varName: String) : Expression {
    override fun process(operation: (Node) -> Unit) {
        operation(this)
    }

    override fun transform(operation: (Node) -> Node): Node {
        return operation(this)
    }
}

class IntLit(val value: String) : Expression  {
    override fun process(operation: (Node) -> Unit) {
        operation(this)
    }

    override fun transform(operation: (Node) -> Node): Node {
        return operation(this)
    }
}

class DecLit(val value: String) : Expression  {
    override fun process(operation: (Node) -> Unit) {
        operation(this)
    }

    override fun transform(operation: (Node) -> Node): Node {
        return operation(this)
    }
}

//
// Statements
//

data class VarDeclaration(val varName: String, val value: Expression) : Statement {
    override fun process(operation: (Node) -> Unit) {
        operation(this)
        value.process(operation)
    }

    override fun transform(operation: (Node) -> Node): Node {
        return operation(VarDeclaration(varName, operation(this.value) as Expression))
    }
}

data class Assignment(val varName: String, val value: Expression) : Statement {
    override fun process(operation: (Node) -> Unit) {
        operation(this)
        value.process(operation)
    }

    override fun transform(operation: (Node) -> Node): Node {
        return operation(Assignment(varName, operation(this.value) as Expression))
    }
}

data class Print(val value: Expression) : Statement {
    override fun process(operation: (Node) -> Unit) {
        operation(this)
        value.process(operation)
    }

    override fun transform(operation: (Node) -> Node): Node {
        return operation(Print(operation(this.value) as Expression))
    }
}
