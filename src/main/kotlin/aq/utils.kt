package aq

class RuntimeError(val token: Token, message: String?) : RuntimeException(message)

fun error(line: Int, message: String) {
    report(line, "", message)
}

fun error(token: Token, message: String) {
    if (token.type === TokenType.EOF) {
        report(token.line, " at end", message)
    } else {
        report(token.line, " at '${token.lexeme}'", message)
    }
}

fun runtimeError(error: RuntimeError) {
    System.err.println(error.message)
    System.err.println("[line [${error.token.line}]")
}

fun report(line: Int, where: String, message: String) {
    println("[line $line] Error: $where: $message")
}

