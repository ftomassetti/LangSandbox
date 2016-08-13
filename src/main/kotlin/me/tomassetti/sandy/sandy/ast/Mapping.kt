package me.tomassetti.sandy.sandy.ast

import me.tomassetti.langsandbox.SandyParser.*
import org.antlr.v4.runtime.ParserRuleContext

interface ParseTreeToAstMapper<in PTN : ParserRuleContext, out ASTN : Node> {
    fun map(parseTreeNode: PTN) : ASTN
}

fun SandyFileContext.toAst() : SandyFile = SandyFile(this.line().map { it.statement().toAst() })

fun StatementContext.toAst() : Statement = when (this) {
    is VarDeclarationStatementContext -> VarDeclaration(varDeclaration().assignment().ID().text, varDeclaration().assignment().expression().toAst())
    is AssignmentStatementContext -> Assignment(assignment().ID().text, assignment().expression().toAst())
    is PrintStatementContext -> Print(print().expression().toAst())
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun  ExpressionContext.toAst() : Expression = when (this) {
    is BinaryOperationContext -> toAst()
    is IntLiteralContext -> IntLit(text)
    is DecimalLiteralContext -> DecLit(text)
    is ParenExpressionContext -> expression().toAst()
    is VarReferenceContext -> VarReference(text)
    is TypeConversionContext -> TypeConversion(expression().toAst(), targetType.toAst())
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun TypeContext.toAst() : Type = when (this) {
    is IntegerContext -> IntType
    is DecimalContext -> DecimalType
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun  BinaryOperationContext.toAst() : Expression = when (operator.text) {
    "+" -> SumExpression(left.toAst(), right.toAst())
    "-" -> SubtractionExpression(left.toAst(), right.toAst())
    "*" -> MultiplicationExpression(left.toAst(), right.toAst())
    "/" -> DivisionExpression(left.toAst(), right.toAst())
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

class SandyParseTreeToAstMapper : ParseTreeToAstMapper<SandyFileContext, SandyFile> {
    override fun map(parseTreeNode: SandyFileContext): SandyFile = parseTreeNode.toAst()
}
