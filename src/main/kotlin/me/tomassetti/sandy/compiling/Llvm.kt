package me.tomassetti.sandy.compiling

import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacpp.LLVM.*
import org.bytedeco.javacpp.PointerPointer
import java.io.File

@Throws(Exception::class)
fun setLibraryPath(path: String) {
    System.setProperty("java.library.path", path)

    //set sys_paths to null
    val sysPathsField = ClassLoader::class.java.getDeclaredField("sys_paths")
    sysPathsField.isAccessible = true
    sysPathsField.set(null, null)
}

fun main(args: Array<String>) {


    System.setProperty("java.library.path", File("./solibs").canonicalPath+ ":" + System.getProperty("java.library.path"))
    setLibraryPath(File("./solibs").canonicalPath+ ":" + System.getProperty("java.library.path"))
    System.loadLibrary("LLVM-3.9")
    System.loadLibrary("LTO")


    //print(System.getProperty("java.library.path"))
    val module = LLVMModuleCreateWithName("me.tomassetti.LLVMExample")
    // an array is a PointerPointer in the JavaCPP-Presets. Don't ask...
    val param_types = PointerPointer(LLVMInt32Type(), LLVMInt32Type())
    val ret_type = LLVMFunctionType(LLVMInt32Type(), param_types, 2, 0)
    val sum = LLVMAddFunction(module, "sum", ret_type)
    val entry = LLVMAppendBasicBlock(sum, "entry")
    val builder = LLVMCreateBuilder()
    LLVMPositionBuilderAtEnd(builder, entry)
    val tmp = LLVMBuildAdd(builder, LLVMGetParam(sum, 0), LLVMGetParam(sum, 1), "tmp")
    LLVMBuildRet(builder, tmp)
    var error = BytePointer()
    LLVMVerifyModule(module, LLVMAbortProcessAction, error)
    LLVMDisposeMessage(error)

    var engine = LLVMExecutionEngineRef()
    error = BytePointer()
    LLVMLinkInMCJIT()
    LLVMInitializeNativeTarget()
    if (LLVMCreateExecutionEngineForModule(engine, module, error) != 0) {
        println(error)
    }
    if (!error.isNull) {
        println(error)
        LLVMDisposeMessage(error)
    }
    println("COMPILED")
    if (LLVMWriteBitcodeToFile(module, "sum.bc") != 0) {
        println("error writing bitcode to file, skipping")
    }
    println("SAVED")

    val targetTriple = LLVMGetDefaultTargetTriple()
    println(targetTriple)

    LLVMInitializeAllTargetInfos()
    LLVMInitializeAllTargets()
    LLVMInitializeAllTargetMCs()
    LLVMInitializeAllAsmParsers()
    LLVMInitializeAllAsmPrinters()

    val target : LLVMTargetRef = LLVMTargetRef()
    LLVMGetTargetFromTriple(targetTriple, target, error)


    val CPU = "generic"
    val Features = ""

    //val opt = TargetOptions()
    //val RM = LLVMOptional<Reloc::Model>();
    //val TargetMachine = LLVMCreateTargetMachine()*/
    // @Cast("LLVMCodeGenOptLevel") int Level, @Cast("LLVMRelocMode") int Reloc, @Cast("LLVMCodeModel") int CodeModel
    val targetMachine = LLVMCreateTargetMachine(target, targetTriple.string, CPU, Features, LLVMCodeGenLevelDefault, LLVMRelocDefault, LLVMCodeModelDefault)

    val objectFilename = "output.o"
    val pass = LLVMCreatePassManager()
    //val fileType = LLVMCFG

    val objectFilenamePtr = BytePointer(objectFilename)

    LLVMTargetMachineEmitToFile(targetMachine, module, objectFilenamePtr, LLVMCodeGenLevelDefault, error)

    LLVMLinkModules2()


    /*auto Filename = "output.o";
    std::error_code EC;
    raw_fd_ostream dest(Filename, EC, sys::fs::F_None);

    if (EC) {
      errs() << "Could not open file: " << EC.message();
      return 1;
    }
    Finally, we define a pass that emits object code, then we run that pass:

    legacy::PassManager pass;
    auto FileType = TargetMachine::CGFT_ObjectFile;

    if (TargetMachine->addPassesToEmitFile(pass, dest, FileType)) {
      errs() << "TargetMachine can't emit a file of this type";
      return 1;
    }

    pass.run(*TheModule);
    dest.flush();
    */
}

