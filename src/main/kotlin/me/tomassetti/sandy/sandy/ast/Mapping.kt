package me.tomassetti.sandy.sandy.ast

import me.tomassetti.langsandbox.SandyParser.*
import org.antlr.v4.runtime.ParserRuleContext

interface ParseTreeToAstMapper<in PTN : ParserRuleContext, out ASTN : Node> {
    fun map(parseTreeNode: PTN) : ASTN
}

fun SandyFileContext.toAst() : SandyFile = SandyFile(this.line().map(LineContext::toAst))

fun LineContext.toAst() : Statement = this.statement().toAst()

fun StatementContext.toAst() : Statement = when (this) {
    is VarDeclarationStatementContext -> toAst()
    is AssignmentStatementContext -> toAst()
    is PrintStatementContext -> toAst()
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun PrintStatementContext.toAst() = print().toAst()

fun PrintContext.toAst() = Print(expression().toAst())

fun VarDeclarationStatementContext.toAst() = varDeclaration().toAst()

fun AssignmentStatementContext.toAst() = assignment().toAst()

fun AssignmentContext.toAst() = Assignment(this.ID().text, this.expression().toAst())

fun VarDeclarationContext.toAst() = VarDeclaration(this.assignment().ID().text, this.assignment().expression().toAst())

fun  ExpressionContext.toAst() : Expression = when (this) {
    is BinaryOperationContext -> toAst()
    is IntLiteralContext -> toAst()
    is DecimalLiteralContext -> toAst()
    is ParenExpressionContext -> expression().toAst()
    is VarReferenceContext -> toAst()
    is TypeConversionContext -> toAst()
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun TypeConversionContext.toAst() = TypeConversion(expression().toAst(), targetType.toAst())

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

fun  IntLiteralContext.toAst() = IntLit(text)

fun  DecimalLiteralContext.toAst() = DecLit(text)

fun  VarReferenceContext.toAst() = VarReference(text)

class SandyParseTreeToAstMapper : ParseTreeToAstMapper<SandyFileContext, SandyFile> {
    override fun map(parseTreeNode: SandyFileContext): SandyFile = parseTreeNode.toAst()
}
