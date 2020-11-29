package aq

class AqFunction(private val declaration: Function) : Callable {
    override val arity: Int
        get() = declaration.params.size

    override fun call(
        interpreter: Interpreter,
        arguments: List<Any?>
    ): Any? {
        val environment = Environment(interpreter.globals)

        declaration.params.forEachIndexed { i, param ->
            environment.define(param.lexeme, arguments[i])
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (r: Interpreter.ReturnSignal) {
            return r.value
        }

        return null
    }

    override fun toString() = "<fun ${declaration.name.lexeme}/${arity}>"
}