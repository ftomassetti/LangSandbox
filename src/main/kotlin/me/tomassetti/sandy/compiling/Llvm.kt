package me.tomassetti.sandy.compiling

import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacpp.LLVM.*
import org.bytedeco.javacpp.PointerPointer

fun main(args: Array<String>) {
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
        print(error)
    }
    if (!error.isNull) {
        print(error)
        LLVMDisposeMessage(error)
    }
}

