package me.tomassetti.sandy.compiling

import me.tomassetti.sandy.ast.*
import me.tomassetti.sandy.parsing.SandyParserFacade
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
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

object IntType : SandyType {
    override val jvmDescription: String
        get() = "I"
}

object DecimalType : SandyType {
    override val jvmDescription: String
        get() = "D"
}

fun Expression.type() : SandyType {
    return when (this) {
        is IntLit -> IntType
        is BinaryExpression -> {
            val leftType = left.type()
            val rightType = right.type()
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
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }
}

// Convert, if needed
fun Expression.pushAs(methodWriter: MethodVisitor, varNamesToIndexes: Map<String, Int>, desiredType: SandyType) {
    push(methodWriter, varNamesToIndexes)
    val myType = type()
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

fun Expression.push(methodWriter: MethodVisitor, varNamesToIndexes: Map<String, Int>) {
    when (this) {
        is IntLit -> methodWriter.visitLdcInsn(Integer.parseInt(this.value))
        is SumExpression -> {
            left.pushAs(methodWriter, varNamesToIndexes, this.type())
            right.pushAs(methodWriter, varNamesToIndexes, this.type())
            when (this.type()) {
                IntType -> methodWriter.visitInsn(IADD)
                DecimalType -> methodWriter.visitInsn(DADD)
                else -> throw UnsupportedOperationException("Summing ${this.type()}")
            }
        }
        is SubtractionExpression -> {
            left.pushAs(methodWriter, varNamesToIndexes, this.type())
            right.pushAs(methodWriter, varNamesToIndexes, this.type())
            when (this.type()) {
                IntType -> methodWriter.visitInsn(ISUB)
                DecimalType -> methodWriter.visitInsn(DSUB)
                else -> throw UnsupportedOperationException("Summing ${this.type()}")
            }
        }
        is DivisionExpression -> {
            left.pushAs(methodWriter, varNamesToIndexes, this.type())
            right.pushAs(methodWriter, varNamesToIndexes, this.type())
            when (this.type()) {
                IntType -> methodWriter.visitInsn(IDIV)
                DecimalType -> methodWriter.visitInsn(DDIV)
                else -> throw UnsupportedOperationException("Summing ${this.type()}")
            }
        }
        is MultiplicationExpression -> {
            left.pushAs(methodWriter, varNamesToIndexes, this.type())
            right.pushAs(methodWriter, varNamesToIndexes, this.type())
            when (this.type()) {
                IntType -> methodWriter.visitInsn(IMUL)
                DecimalType -> methodWriter.visitInsn(DMUL)
                else -> throw UnsupportedOperationException("Summing ${this.type()}")
            }
        }
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }
}

fun VarDeclaration.type() = this.value.type()

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
        val varNamesToIndexes = HashMap<String, Int>()
        root.specificProcess(VarDeclaration::class.java) {
            val index = nextVarIndex++
            varNamesToIndexes[it.varName] = index
            mainMethodWriter.visitLocalVariable(it.varName, it.type().jvmDescription, null, methodStart, methodEnd, index)
        }

        root.statements.forEach { s ->
            when(s) {
                is Print -> {
                    mainMethodWriter.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
                    s.value.push(mainMethodWriter, varNamesToIndexes)
                    mainMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(${s.value.type().jvmDescription})V", false)
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
    val code = "print(1 + 4 * 3 - 5)"
    val bytes = JvmCompiler().compile(SandyParserFacade.parse(code).root!!, "FOO")
    val fos = FileOutputStream("FOO.class")
    fos.write(bytes)
    fos.close()
}