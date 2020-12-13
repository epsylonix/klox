package aq


class ParseError : RuntimeException()

class Parser(private val tokens: List<Token>) {
    private var current: Int = 0
    private var loopLevel: Int = 0

    /*
        logic_or       → logic_and ( "or" logic_and )* ;
        logic_and      → equality ( "and" equality )* ;
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

    // statements ------------------------------------------------------------

    private fun functionDeclaration(kind: String): Fun {
        val name = consume(TokenType.IDENTIFIER, "Expected $kind name")
        return Fun(name, function(kind))
    }

    private fun declaration(): Stmt? {
        return try {
            when {
                match(TokenType.FUN) ->
                    functionDeclaration("function")
                match(TokenType.VAR) ->
                    varDeclaration()
                else ->
                    statement()
            }
        } catch (error: ParseError) {
            synchronize()
            null
        }
    }

    private fun statement(): Stmt {
        return when {
            match(TokenType.FOR) ->
                forStatement()
            match(TokenType.IF) ->
                ifStatement()
            match(TokenType.PRINT) ->
                printStatement()
            match(TokenType.RETURN) ->
                returnStatement()
            match(TokenType.WHILE) ->
                whileStatement()
            match(TokenType.LEFT_BRACE) ->
                Block(block())
            match(TokenType.BREAK) ->
                breakStatement()
            else ->
                expressionStatement()
        }
    }

    private fun forStatement(): Stmt {
        try {
            loopLevel += 1

            // desugaring for into while
            consume(TokenType.LEFT_PAREN, "Expected '(' after 'for'")

            val initializer = when {
                match(TokenType.SEMICOLON) -> null
                match(TokenType.VAR) -> varDeclaration()
                else -> expressionStatement()
            }

            var condition = if (check(TokenType.SEMICOLON)) null else expression()
            consume(TokenType.SEMICOLON, "Expected ';' after 'for' condition")

            val increment = if (check(TokenType.RIGHT_PAREN)) null else expression()
            consume(TokenType.RIGHT_PAREN, "Expected ')' after 'for' clauses")

            var body = statement()
            body = if (increment == null) body else Block(listOf(body, Expression(increment)))

            condition = condition ?: Literal(true)
            body = While(condition, body)

            body = if (initializer == null) body else Block(listOf(initializer, body))
            return body
        } finally {
            loopLevel -= 1
        }
    }

    private fun ifStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'if'")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expected ')' after 'if' condition")
        val thenBranch = statement()
        val elseBranch = if (match(TokenType.ELSE)) statement() else null

        return If(condition, thenBranch, elseBranch)
    }

    private fun printStatement(): Print {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expected ';' after statement")
        return Print(expr)
    }

    private fun returnStatement(): Return {
        val keyword = previous()
        val value = if (!check(TokenType.SEMICOLON)) expression() else null

        consume(TokenType.SEMICOLON, "Expected ';' after return value")
        return Return(keyword, value)
    }

    private fun whileStatement(): While {
        try {
            loopLevel += 1

            consume(TokenType.LEFT_PAREN, "Expected '(' after 'while'")
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expected ')' after 'while' condition")
            val body = statement()

            return While(expr, body)
        } finally {
            loopLevel -= 1
        }
    }

    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            declaration()?.let(statements::add)
        }
        consume(TokenType.RIGHT_BRACE, "Expected '}' after block")
        return statements
    }

    private fun breakStatement(): Break {
        if(loopLevel == 0) {
            error(previous(), "Invalid break statement outside of loop")
            throw ParseError()
        }

        consume(TokenType.SEMICOLON, "Expected ';' after break")
        return Break()
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


    // expressions ------------------------------------------------------------

    private fun expression(): Expr {
        return assignment()
    }

    private fun function(kind: String = "function"): Function {
        consume(TokenType.LEFT_PAREN, "Expected '(' in $kind declaration")

        val parameters = mutableListOf<Token>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size >= 255) {
                    error(peek(), "Can't have more than 255 parameters")
                }
                parameters.add(
                    consume(TokenType.IDENTIFIER, "Expected parameter name")
                )
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expected ')' after parameters")

        consume(TokenType.LEFT_BRACE, "Expected '{' before $kind body")
        val body = block()

        return Function(parameters, body)
    }

    private fun assignment(): Expr {
        val expr = or()

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

    private fun or(): Expr {
        var expr: Expr = and()

        while (match(TokenType.OR)) {
            val operator = previous()
            val right: Expr = and()
            expr = Logical(expr, operator, right)
        }
        return expr
    }

    private fun and(): Expr {
        var expr = equality()

        while (match(TokenType.AND)) {
            val operator = previous()
            val right = equality()
            expr = Logical(expr, operator, right)
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

        return call()
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

    private fun call(): Expr {
        var expr = primary()
        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr)
            } else {
                break
            }
        }
        return expr
    }

    private fun finishCall(callee: Expr): Expr {
        val arguments = mutableListOf<Expr>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                arguments.add(expression())
                if (arguments.size >= 255) error(peek(), "Can't have more than 255 arguments.")
            } while (match(TokenType.COMMA))
        }
        val paren = consume(
            TokenType.RIGHT_PAREN,
            "Expected ')' after a function arguments"
        )
        return Call(callee, paren, arguments)
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

        if(match(TokenType.FUN)) return function("function")

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