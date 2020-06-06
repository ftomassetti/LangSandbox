package me.tomassetti.sandy.ast

import java.util.*

data class Error(val message: String, val position: Point)

fun SandyFile.validate() : List<Error> {
    val errors = LinkedList<Error>()

    // check a variable is not duplicated
    val varsByName = HashMap<String, VarDeclaration>()
    this.specificProcess(VarDeclaration::class.java) {
        if (varsByName.containsKey(it.varName)) {
            errors.add(Error("A variable named '${it.varName}' has been already declared at ${varsByName[it.varName]!!.position!!.start}",
                    it.position!!.start))
        } else {
            varsByName[it.varName] = it
        }
    }

    // check a variable is not referred before being declared
    this.specificProcess(VarReference::class.java) {
        if (!varsByName.containsKey(it.varName)) {
            errors.add(Error("There is no variable named '${it.varName}'", it.position!!.start))
        } else if (it.isBefore(varsByName[it.varName]!!)) {
            errors.add(Error("You cannot refer to variable '${it.varName}' before its declaration", it.position!!.start))
        }
    }
    this.specificProcess(Assignment::class.java) {
        if (!varsByName.containsKey(it.varName)) {
            errors.add(Error("There is no variable named '${it.varName}'", it.position!!.start))
        } else if (it.isBefore(varsByName[it.varName]!!)) {
            errors.add(Error("You cannot refer to variable '${it.varName}' before its declaration", it.position!!.start))
        }
    }

    return errors
}
