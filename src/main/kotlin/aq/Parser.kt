package aq

import java.util.*


class ParseError : RuntimeException()

class Parser(private val tokens: List<Token>) {
    private var current: Int = 0

    /*
        expression     → equality ;
        equality       → comparison ( ( "!=" | "==" ) comparison )* ;
        comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
        term           → factor ( ( "-" | "+" ) factor )* ;
        factor         → unary ( ( "/" | "*" ) unary )* ;
        unary          → ( "!" | "-" ) unary
                       | primary ;
        primary        → NUMBER | STRING | "true" | "false" | "nil"
                   | "(" expression ")" ;
     */

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            declaration()?.let(statements::add)
        }
        return statements

//        return try {
//            expression()
//        } catch (error: Exception) {
//            throw error
//        }
//        } catch (error: ParseError) {
//            null
//        }
    }

    private fun declaration(): Stmt? {
        return try {
            if (match(TokenType.VAR)) varDeclaration() else statement()
        } catch (error: ParseError) {
            synchronize()
            null
        }
    }

    private fun statement(): Stmt {
        return when {
            match(TokenType.PRINT) ->
                printStatement()
            match(TokenType.LEFT_BRACE) ->
                Block(block())
            else ->
                expressionStatement()
        }
    }

    private fun printStatement(): Print {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expected ';' after statement")
        return Print(expr)
    }

    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            declaration()?.let(statements::add)
        }
        consume(TokenType.RIGHT_BRACE, "Expected '}' after block")
        return statements
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expected ';' after statement")
        return Expression(expr)
    }

    private fun varDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expected variable name")
        val initialValue = if(match(TokenType.EQUAL)) expression() else null
        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration")
        return Var(name, initialValue)
    }

    private fun expression(): Expr {
        return assignment()
    }

    private fun assignment(): Expr {
        val expr = equality()

        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()
            if (expr is Variable) {
                val name: Token = expr.name
                return Assign(name, value)
            }
            error(equals, "Invalid assignment target")
        }

        return expr
    }

    private fun equality(): Expr {
        var expr = comparison()

        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr = term()

        while(match(TokenType.LESS_EQUAL, TokenType.LESS, TokenType.GREATER, TokenType.GREATER_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun term(): Expr {
        var expr = factor()

        while(match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun factor(): Expr {
        var expr = unary()

        while(match(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous()
            val right = unary()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        if(match(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Unary(operator, right)
        }

        return primary()
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return
            when (peek().type) {
                TokenType.CLASS,
                TokenType.FUN,
                TokenType.VAR,
                TokenType.FOR,
                TokenType.IF,
                TokenType.WHILE,
                TokenType.PRINT,
                TokenType.RETURN ->
                    return
            }
            advance()
        }
    }

    private fun primary(): Expr {
        if (match(TokenType.NUMBER, TokenType.STRING)) return Literal(previous().literal)
        if (match(TokenType.IDENTIFIER)) return Variable(previous())

        if (match(TokenType.FALSE)) return Literal(false)
        if (match(TokenType.TRUE)) return Literal(true)
        if (match(TokenType.NIL)) return Literal(null)

        if(match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Missing ')' after the expression")
            return Grouping(expr)
        }

        error(peek(), "Expected expression")
        throw ParseError()
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun advance() {
        if(!isAtEnd()) current += 1
    }

    private fun isAtEnd(): Boolean {
        return tokens[current].type == TokenType.EOF
    }

    private fun match(vararg tokens: TokenType): Boolean {
        if(tokens.none(::check)) return false
        advance()

        return true
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) {
            val token = peek()
            advance()
            return token
        }

        error(peek(), message)
        throw ParseError()
    }

    private fun check(token: TokenType): Boolean {
        return tokens[current].type == token
    }
}