package aq

class AqFunction(
    private val name: String,
    private val declaration: Function,
    private val closure: Environment
) : Callable {
    override val arity: Int
        get() = declaration.params.size

    override fun call(
        interpreter: Interpreter,
        arguments: List<Any?>
    ): Any? {
        val environment = Environment(closure)

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

    override fun toString() = "<fun $name/$arity>"
}