package me.tomassetti.sandy.compiler.jvm

import me.tomassetti.langsandbox.SandyParser
import me.tomassetti.sandy.parsing.SandyParserFacade
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import java.io.File
import java.io.FileOutputStream

object Compiler {

    private fun fail(message: String) {
        println(message)
        System.exit(1)
    }

    private fun produceBytecode(className:String, root: SandyParser.SandyFileContext) : ByteArray {
        val cw = ClassWriter(0)
        val fv: FieldVisitor
        var mv: MethodVisitor
        val av0: AnnotationVisitor

        cw.visit(49,
                ACC_PUBLIC + ACC_SUPER,
                className,
                null,
                "java/lang/Object",
                null)

        run {
            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC,
                    "main",
                    "([Ljava/lang/String;)V",
                    null,
                    null)
            mv.visitFieldInsn(GETSTATIC,
                    "java/lang/System",
                    "out",
                    "Ljava/io/PrintStream;")
            mv.visitLdcInsn("hello")
            mv.visitMethodInsn(INVOKEVIRTUAL,
                    "java/io/PrintStream",
                    "println",
                    "(Ljava/lang/String;)V")
            mv.visitInsn(RETURN)
            mv.visitMaxs(2, 1)
            mv.visitEnd()
        }
        cw.visitEnd()

        return cw.toByteArray()
    }

    private fun compile(root: SandyParser.SandyFileContext, classFile: File) {
        val fos = FileOutputStream(classFile)
        fos.write(produceBytecode(classFile.nameWithoutExtension, root))
        fos.close()
    }

    @JvmStatic fun main(args: Array<String>) {
        if (args.size != 1) {
            fail("Please pass exactly one argument")
        }
        val srcFile = File(args[0])
        if (!srcFile.exists() && !srcFile.isFile) {
            fail("Please pass a valid file")
        }
        val parsingResult = SandyParserFacade.parse(srcFile)
        if (!parsingResult.isCorrect()) {
            parsingResult.errors.forEach { println(it) }
            fail("Cannot compile because of parsing errors")
        }
        compile(parsingResult.root!!, File("${srcFile.nameWithoutExtension}.class"))
    }
}