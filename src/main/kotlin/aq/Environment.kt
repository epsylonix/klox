package aq


class Environment(
    private val enclosing: Environment? = null
) {
    private val values = mutableMapOf<String, Any?>()

    operator fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) return values[name.lexeme]
        if (enclosing != null) return enclosing.get(name)

        throw RuntimeError(name,"Undefined variable ${name.lexeme}");
    }

    fun getAt(distance: Int, name: String): Any? {
        return ancestor(distance).values[name]
    }

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun assign(name: Token, value: Any?) {
        when {
            values.containsKey(name.lexeme) ->
                values[name.lexeme] = value
            enclosing != null ->
                enclosing.assign(name, value)
            else ->
                throw RuntimeError(name, "Undefined variable ${name.lexeme}")
        }
    }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = value
    }

    private fun ancestor(distance: Int): Environment {
        var env = this
        repeat(distance) { env = env.enclosing ?: throw IllegalStateException("environment doesn't have $distance ancestors") }
        return env
    }
}
