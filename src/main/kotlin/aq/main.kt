package aq

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    when {
        args.size > 1 -> {
            println("Too many arguments: ${args.toList()}\nUsage: aq [script]")
            exitProcess(64)
        }
        args.size == 1 -> {
            try {
                Aq.exec(args.first())
            } catch (e: CompilerError) {
                exitProcess(65)
            }
        }
        else -> {
            Aq.repl()
        }
    }
}