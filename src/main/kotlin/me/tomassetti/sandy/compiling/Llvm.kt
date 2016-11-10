package me.tomassetti.sandy.compiling

import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacpp.LLVM.*
import org.bytedeco.javacpp.Pointer
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

fun saveModuleAsIRCode(module: LLVMModuleRef, irFilename: String) {
    var error = BytePointer()
    LLVMPrintModuleToFile(module, irFilename, error)
}

fun saveModuleAsObjectCode(module: LLVMModuleRef, objectFilename: String) {
    var error = BytePointer()
    val targetTriple = LLVMGetDefaultTargetTriple()
    println(targetTriple)



    val target : LLVMTargetRef = LLVMTargetRef()
    LLVMGetTargetFromTriple(targetTriple, target, error)

    val CPU = "generic"
    val Features = ""

    //val opt = TargetOptions()
    //val RM = LLVMOptional<Reloc::Model>();
    //val TargetMachine = LLVMCreateTargetMachine()*/
    // @Cast("LLVMCodeGenOptLevel") int Level, @Cast("LLVMRelocMode") int Reloc, @Cast("LLVMCodeModel") int CodeModel
    val targetMachine = LLVMCreateTargetMachine(target, targetTriple.string, CPU, Features, LLVMCodeGenLevelDefault, LLVMRelocDefault, LLVMCodeModelDefault)

    val pass = LLVMCreatePassManager()
    //val fileType = LLVMCFG

    val objectFilenamePtr = BytePointer(objectFilename)

    LLVMTargetMachineEmitToFile(targetMachine, module, objectFilenamePtr, LLVMCodeGenLevelDefault, error)
}

fun moduleWithMain() {
    val context = LLVMGetGlobalContext()
    val module = LLVMModuleCreateWithName("me.tomassetti.MyModule")
    var builder = LLVMCreateBuilderInContext(context)

    val param_types = PointerPointer<LLVMTypeRef>()
    val funcType = LLVMFunctionType(LLVMInt32Type(), param_types, 0, 0)

    val mainFunc = LLVMAddFunction(module, "main", funcType)

    val value = LLVMCreateGenericValueOfPointer(BytePointer("hello world!\n"))

    val entry = LLVMAppendBasicBlock(mainFunc, "entrypoint")
    builder = LLVMCreateBuilder()
    LLVMPositionBuilderAtEnd(builder, entry)
    val argsTypes = PointerPointer<Pointer>(LLVMPointerType(LLVMInt8Type(), 0))
    val printf = LLVMAddFunction(module, "printf", LLVMFunctionType(LLVMInt32Type(), argsTypes, 1, 0));

    //LLVMBuildCall()
    //println("printf $printf")
    //val functionAddress : LLVMValueRef = null
    //LLVMBuildInvoke(builder, functionAddress, PointerPointer(LLVMPointerType(LLVMInt8Type())), 1, null, null, value)
    LLVMBuildRet(builder,  LLVMConstInt(LLVMInt32Type(), 0, 0))

    var error = BytePointer()
    LLVMVerifyModule(module, LLVMAbortProcessAction, error)
    saveModuleAsIRCode(module, "main.ll")
    saveModuleAsObjectCode(module, "main.o")
}

fun main(args: Array<String>) {
    System.setProperty("java.library.path", File("./solibs").canonicalPath+ ":" + System.getProperty("java.library.path"))
    setLibraryPath(File("./solibs").canonicalPath+ ":" + System.getProperty("java.library.path"))
    System.loadLibrary("LLVM-3.9")
    System.loadLibrary("LTO")

    LLVMInitializeAllTargetInfos()
    LLVMInitializeAllTargets()
    LLVMInitializeAllTargetMCs()
    LLVMInitializeAllAsmParsers()
    LLVMInitializeAllAsmPrinters()

    moduleWithMain()

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

    saveModuleAsObjectCode(module, "sum.o")

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

