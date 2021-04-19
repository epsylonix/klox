package aq

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

class CompilerError(message: String) : Exception(message)

object Aq {
    private val interpreter = Interpreter()

    fun exec(path: String) {
        Files.readAllBytes(Paths.get(path))
            .toString(Charset.defaultCharset())
            .also(Aq::run)
    }

    fun repl() {
        var line: String?
        println("REPL started")
        while (true) {
            print("> ")
            line = readLine() ?: break
            run(line)
        }
        println("bye!")
    }

    fun run(script: String) {
        val scanner = Scanner(script)
        val tokens: List<Token> = scanner.scanTokens()

        val statements = Parser(tokens).parse()

        val resolver = VarResolver(interpreter);
        resolver.resolve(statements)

        interpreter.interpret(statements)
    }
}