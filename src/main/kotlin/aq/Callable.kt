package aq

interface Callable {
    val arity: Int
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}