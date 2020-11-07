package aq

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

class CompilerError(message: String) : Exception(message)

object Aq {
    fun exec(path: String) {
        Files.readAllBytes(Paths.get(path))
            .toString(Charset.defaultCharset())
            .also(Aq::run)
    }

    fun repl() {
        var line: String?
        while (true) {
            line = readLine() ?: break
            run(line)
        }
    }

    fun run(script: String) {
        val scanner = Scanner(script)
        val tokens: List<Token> = scanner.scanTokens()
        val expression = Parser(tokens).parse()

        println(AstPrinter().print(expression))

//
//        for (token in tokens) {
//            System.out.println(token)
//        }
    }
}