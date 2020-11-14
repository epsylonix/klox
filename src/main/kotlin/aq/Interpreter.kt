package aq

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


class Interpreter : Visitor<Any?> {
    fun interpret(expression: Expr) {
        try {
            val value = eval(expression)
            println(stringify(value))
        } catch (error: RuntimeError) {
            runtimeError(error)
        }
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

    private fun isTruthy(obj: Any?): Boolean {
        obj ?: return false
        return if (obj is Boolean) obj else false
    }

    private fun eval(expr: Expr): Any? {
        return expr.accept(this)
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