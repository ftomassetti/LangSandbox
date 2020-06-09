package me.tomassetti.sandy.compiling

import me.tomassetti.sandy.ast.*
import me.tomassetti.sandy.parsing.SandyParserFacade
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

interface Source {
    val name: String
    val inputStream: InputStream
}

interface SandyType {
    val jvmDescription: String
}

fun Type.toSandyType() : SandyType {
    return when(this) {
        is me.tomassetti.sandy.ast.IntType -> IntType
        is me.tomassetti.sandy.ast.DecimalType -> DecimalType
        else -> throw UnsupportedOperationException()
    }
}

object IntType : SandyType {
    override val jvmDescription: String
        get() = "I"
}

object DecimalType : SandyType {
    override val jvmDescription: String
        get() = "D"
}

fun Expression.type(vars: Map<String, Var>) : SandyType {
    return when (this) {
        is IntLit -> IntType
        is DecLit -> DecimalType
        is BinaryExpression -> {
            val leftType = left.type(vars)
            val rightType = right.type(vars)
            if (leftType != IntType && leftType != DecimalType) {
                throw UnsupportedOperationException()
            }
            if (rightType != IntType && rightType != DecimalType) {
                throw UnsupportedOperationException()
            }
            if (leftType == IntType && rightType == IntType) {
                return IntType
            } else {
                return DecimalType
            }
        }
        is VarReference -> vars[this.varName]!!.type
        is TypeConversion -> this.targetType.toSandyType()
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }
}

// Convert, if needed
fun Expression.pushAs(methodWriter: MethodVisitor, vars: Map<String, Var>, desiredType: SandyType) {
    push(methodWriter, vars)
    val myType = type(vars)
    if (myType != desiredType) {
        if (myType == IntType && desiredType == DecimalType) {
            methodWriter.visitInsn(I2D)
        } else if (myType == DecimalType && desiredType == IntType) {
            methodWriter.visitInsn(D2I)
        } else {
            throw UnsupportedOperationException("Conversion from $myType to $desiredType")
        }
    }
}

fun Expression.push(methodWriter: MethodVisitor, vars: Map<String, Var>) {
    when (this) {
        is IntLit -> methodWriter.visitLdcInsn(Integer.parseInt(this.value))
        is DecLit -> methodWriter.visitLdcInsn(java.lang.Double.parseDouble(this.value))
        is SumExpression -> {
            left.pushAs(methodWriter, vars, this.type(vars))
            right.pushAs(methodWriter, vars, this.type(vars))
            when (this.type(vars)) {
                IntType -> methodWriter.visitInsn(IADD)
                DecimalType -> methodWriter.visitInsn(DADD)
                else -> throw UnsupportedOperationException("Summing ${this.type(vars)}")
            }
        }
        is SubtractionExpression -> {
            left.pushAs(methodWriter, vars, this.type(vars))
            right.pushAs(methodWriter, vars, this.type(vars))
            when (this.type(vars)) {
                IntType -> methodWriter.visitInsn(ISUB)
                DecimalType -> methodWriter.visitInsn(DSUB)
                else -> throw UnsupportedOperationException("Summing ${this.type(vars)}")
            }
        }
        is DivisionExpression -> {
            left.pushAs(methodWriter, vars, this.type(vars))
            right.pushAs(methodWriter, vars, this.type(vars))
            when (this.type(vars)) {
                IntType -> methodWriter.visitInsn(IDIV)
                DecimalType -> methodWriter.visitInsn(DDIV)
                else -> throw UnsupportedOperationException("Summing ${this.type(vars)}")
            }
        }
        is MultiplicationExpression -> {
            left.pushAs(methodWriter, vars, this.type(vars))
            right.pushAs(methodWriter, vars, this.type(vars))
            when (this.type(vars)) {
                IntType -> methodWriter.visitInsn(IMUL)
                DecimalType -> methodWriter.visitInsn(DMUL)
                else -> throw UnsupportedOperationException("Summing ${this.type(vars)}")
            }
        }
        is VarReference -> {
            val type = vars[this.varName]!!.type
            when (type) {
                IntType -> methodWriter.visitVarInsn(ILOAD, vars[this.varName]!!.index)
                DecimalType -> methodWriter.visitVarInsn(DLOAD, vars[this.varName]!!.index)
                else -> throw UnsupportedOperationException(type.javaClass.canonicalName)
            }
        }
        is TypeConversion -> {
            this.value.pushAs(methodWriter, vars, this.targetType.toSandyType())
        }
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }
}

fun VarDeclaration.type(vars: Map<String, Var>) = this.value.type(vars)

data class Var(val type:SandyType, val index:Int)

class JvmCompiler {

    fun compile(root: SandyFile, name: String) : ByteArray {
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        cw.visit(V1_8, ACC_PUBLIC, name, null, "java/lang/Object", null)
        val mainMethodWriter = cw.visitMethod(ACC_PUBLIC or ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null)
        mainMethodWriter.visitCode()
        val methodStart = Label()
        val methodEnd = Label()
        mainMethodWriter.visitLabel(methodStart)

        // Variable declarations
        var nextVarIndex = 0
        val vars = HashMap<String, Var>()
        root.specificProcess(VarDeclaration::class.java) {
            val index = nextVarIndex++
            vars[it.varName] = Var(it.type(vars), index)
            mainMethodWriter.visitLocalVariable(it.varName, it.type(vars).jvmDescription, null, methodStart, methodEnd, index)
        }

        root.statements.forEach { s ->
            when (s) {
                is VarDeclaration -> {
                    val type = vars[s.varName]!!.type
                    s.value.pushAs(mainMethodWriter, vars, type)
                    when (type) {
                        IntType -> mainMethodWriter.visitVarInsn(ISTORE, vars[s.varName]!!.index)
                        DecimalType -> mainMethodWriter.visitVarInsn(DSTORE, vars[s.varName]!!.index)
                        else -> throw UnsupportedOperationException(type.javaClass.canonicalName)
                    }
                }
                is Print -> {
                    mainMethodWriter.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
                    s.value.push(mainMethodWriter, vars)
                    mainMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(${s.value.type(vars).jvmDescription})V", false)
                }
                is Assignment -> {
                    val type = vars[s.varName]!!.type
                    s.value.pushAs(mainMethodWriter, vars, type)
                    when (type) {
                        IntType -> mainMethodWriter.visitVarInsn(ISTORE, vars[s.varName]!!.index)
                        DecimalType -> mainMethodWriter.visitVarInsn(DSTORE, vars[s.varName]!!.index)
                        else -> throw UnsupportedOperationException(type.javaClass.canonicalName)
                    }
                }
                else -> throw UnsupportedOperationException(s.javaClass.canonicalName)
            }
        }

        mainMethodWriter.visitLabel(methodEnd)
        mainMethodWriter.visitInsn(RETURN)
        mainMethodWriter.visitEnd()
        mainMethodWriter.visitMaxs(-1, -1)
        cw.visitEnd()
        return cw.toByteArray()
    }

}

fun main(args: Array<String>) {
    val code : InputStream? = when (args.size) {
        0 -> System.`in`
        1 -> FileInputStream(File(args[0]))
        else -> {
            System.err.println("Pass 0 arguments or 1")
            System.exit(1)
            null
        }
    }
    val parsingResult = SandyParserFacade.parse(code!!)
    if (!parsingResult.isCorrect()) {
        println("ERRORS:")
        parsingResult.errors.forEach { println(" * L${it.position.line}: ${it.message}") }
        return
    }
    val root = parsingResult.root!!
    val errors = root.validate()
    if (errors.isNotEmpty()) {
        println("ERRORS:")
        errors.forEach { println(" * L${it.position.line}: ${it.message}") }
        return
    }
    val bytes = JvmCompiler().compile(root, "MyClass")
    val fos = FileOutputStream("MyClass.class")
    fos.write(bytes)
    fos.close()
}
