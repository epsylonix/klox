package aq

fun error(line: Int, message: String) {
    report(line, "", message)
    throw CompilerError(message)
}

fun error(token: Token, message: String): Nothing {
    if (token.type === TokenType.EOF) {
        report(token.line, " at end", message)
    } else {
        report(token.line, " at '${token.lexeme}'", message)
    }
    throw CompilerError(message)
}

fun report(line: Int, where: String, message: String) {
    println("[line $line] Error: $where: $message")
}

