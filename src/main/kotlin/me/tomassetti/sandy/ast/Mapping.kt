package me.tomassetti.sandy.ast

import me.tomassetti.langsandbox.SandyParser.*
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token

interface ParseTreeToAstMapper<in PTN : ParserRuleContext, out ASTN : Node> {
    fun map(parseTreeNode: PTN) : ASTN
}

fun SandyFileContext.toAst(considerPosition: Boolean = false) : SandyFile = SandyFile(this.line().map { it.statement().toAst(considerPosition) }, toPosition(considerPosition))

fun Token.startPoint() = Point(line, charPositionInLine)

fun Token.endPoint() = Point(line, charPositionInLine + text.length)

fun ParserRuleContext.toPosition(considerPosition: Boolean) : Position? {
    return if (considerPosition) Position(start.startPoint(), stop.endPoint()) else null
}

fun StatementContext.toAst(considerPosition: Boolean = false) : Statement = when (this) {
    is VarDeclarationStatementContext -> VarDeclaration(varDeclaration().assignment().ID().text, varDeclaration().assignment().expression().toAst(considerPosition), toPosition(considerPosition))
    is AssignmentStatementContext -> Assignment(assignment().ID().text, assignment().expression().toAst(considerPosition), toPosition(considerPosition))
    is PrintStatementContext -> Print(print().expression().toAst(considerPosition), toPosition(considerPosition))
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun ExpressionContext.toAst(considerPosition: Boolean = false) : Expression = when (this) {
    is BinaryOperationContext -> toAst(considerPosition)
    is IntLiteralContext -> IntLit(text, toPosition(considerPosition))
    is DecimalLiteralContext -> DecLit(text, toPosition(considerPosition))
    is ParenExpressionContext -> expression().toAst(considerPosition)
    is VarReferenceContext -> VarReference(text, toPosition(considerPosition))
    is TypeConversionContext -> TypeConversion(expression().toAst(considerPosition), targetType.toAst(considerPosition), toPosition(considerPosition))
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun TypeContext.toAst(considerPosition: Boolean = false) : Type = when (this) {
    is IntegerContext -> IntType(toPosition(considerPosition))
    is DecimalContext -> DecimalType(toPosition(considerPosition))
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun BinaryOperationContext.toAst(considerPosition: Boolean = false) : Expression = when (operator.text) {
    "+" -> SumExpression(left.toAst(considerPosition), right.toAst(considerPosition), toPosition(considerPosition))
    "-" -> SubtractionExpression(left.toAst(considerPosition), right.toAst(considerPosition), toPosition(considerPosition))
    "*" -> MultiplicationExpression(left.toAst(considerPosition), right.toAst(considerPosition), toPosition(considerPosition))
    "/" -> DivisionExpression(left.toAst(considerPosition), right.toAst(considerPosition), toPosition(considerPosition))
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

class SandyParseTreeToAstMapper : ParseTreeToAstMapper<SandyFileContext, SandyFile> {
    override fun map(parseTreeNode: SandyFileContext): SandyFile = parseTreeNode.toAst()
}
