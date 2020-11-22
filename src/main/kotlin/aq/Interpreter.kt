package aq

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    private class BreakException: Exception()

    private var environment = Environment()

    fun interpret(statements: List<Stmt>) {
        try {
            statements.forEach(::execute)
        } catch (error: RuntimeError) {
            runtimeError(error)
        }
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    @ExperimentalContracts
    override fun visitBinaryExpr(expr: Binary): Any? {
        val left = eval(expr.left)
        val right = eval(expr.right)

        return when (expr.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                left - right
            }
            TokenType.PLUS -> {
                when {
                    left is Double && right is Double ->
                        left + right
                    left is String && right is String ->
                        left + right
                    left is String ->
                        throw RuntimeError(expr.operator, "expected string, got [$right]")
                    left is Double ->
                        throw RuntimeError(expr.operator, "expected number, got [$right]")
                    else ->
                        throw RuntimeError(expr.operator, "expected string or number, got [$left]")
                }
            }
            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                left / right
            }
            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                left * right
            }
            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                left > right
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                left >= right
            }
            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                left < right
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                left <= right
            }
            TokenType.BANG_EQUAL ->
                left != right
            TokenType.EQUAL_EQUAL ->
                left == right
            else ->
                throw IllegalStateException("unexpected binary operator: ${expr.operator}")
        }
    }

    override fun visitGroupingExpr(expr: Grouping): Any? {
        return eval(expr.expression)
    }

    override fun visitLiteralExpr(expr: Literal): Any? {
        return expr.value
    }

    @ExperimentalContracts
    override fun visitUnaryExpr(expr: Unary): Any? {
        val right = eval(expr.right)

        return when (expr.operator.type) {
            TokenType.BANG ->
                !isTruthy(right)
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                -right
            }
            else ->
                throw IllegalStateException("unexpected unary operator: ${expr.operator}")
        }
    }

    // access to var
    override fun visitVariableExpr(expr: Variable): Any? {
        return environment.get(expr.name)
    }

    override fun visitAssignExpr(expr: Assign): Any? {
        val value = eval(expr.value)
        environment.assign(expr.name, value)
        return value
    }

    override fun visitLogicalExpr(expr: Logical): Any? {
        val left = eval(expr.left)
        when {
            (expr.operator.type == TokenType.OR) ->
                if (isTruthy(left)) return left
            else ->
                if (!isTruthy(left)) return left
        }

        return eval(expr.right)
    }


    // statements ======================

    override fun visitExpressionStmt(stmt: Expression) {
        eval(stmt.expression)
    }

    override fun visitPrintStmt(stmt: Print) {
        val value = eval(stmt.expression)
        println(stringify(value))
    }

    // var definition
    override fun visitVarStmt(stmt: Var) {
        val value = stmt.initializer?.let(::eval)
        environment.define(stmt.name.lexeme, value)
    }

    override fun visitBlockStmt(stmt: Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitIfStmt(stmt: If) {
        if(isTruthy(eval(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitWhileStmt(stmt: While) {
        while (isTruthy(eval(stmt.condition))) {
            try {
                execute(stmt.body)
            } catch (e: BreakException) {
                break
            }
        }
    }

    override fun visitBreakStmt(stmt: Break) {
        throw BreakException()
    }
    // ===================================

    private fun isTruthy(obj: Any?): Boolean {
        obj ?: return false
        return if (obj is Boolean) obj else false
    }

    private fun eval(expr: Expr): Any? {
        return expr.accept(this)
    }

    private fun executeBlock(
        statements: List<Stmt?>,
        blockEnv: Environment
    ) {
        val previous = environment
        try {
            environment = blockEnv
            for (statement in statements) {
                execute(statement!!)
            }
        } finally {
            this.environment = previous
        }
    }

    @ExperimentalContracts
    private fun checkNumberOperand(operator: Token, operand: Any?) {
        contract { returns() implies (operand is Double) }

        if (operand is Double) return
        throw RuntimeError(operator, "Expected $operand to be a number")
    }

    @ExperimentalContracts
    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        contract { returns() implies (left is Double && right is Double) }

        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Expected [$left] and [$right] to be numbers")
    }
    
    private fun stringify(obj: Any?): String? {
        obj ?: return "nil"
        if (obj is Double) {
            val text = obj.toString()
            return if(text.endsWith(".0")) text.dropLast(2) else text
        }
        return obj.toString()
    }
}