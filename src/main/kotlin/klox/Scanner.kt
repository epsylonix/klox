package klox

import klox.TokenType.Companion.KEYWORDS
import java.util.*


class Scanner(
    private val source: String
) {
    private val tokens = mutableListOf<Token>()

    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return Collections.unmodifiableList(tokens)
    }

    private fun scanToken() {
        val c: Char = advance()
        when (c) {
            // single-char
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)

            // multi-char
            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '/' ->
                when {
                    match('/') -> {
                        // read comment
                        while (peek() != '\n' && !isAtEnd()) advance()
                    }
                    match('*') -> readMultilineComment()
                    else -> addToken(TokenType.SLASH)
                }
            '"' -> readString()

            ' ', '\r', '\t' -> {
                // whitespace ignored
            }
            '\n' -> line++

            else -> {
                when {
                    isDigit(c) -> readNumber()
                    isAlpha(c) -> readIdentifier()
                    else -> error(line, "Unexpected char: $c (${c.toShort()})")
                }
            }
        }
    }

    private fun isDigit(c: Char): Boolean = c in '0'..'9'
    private fun isAlpha(c: Char): Boolean = c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    private fun isAlphaNumeric(c: Char): Boolean = isDigit(c) or isAlpha(c)

    private fun readString() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }
        if (isAtEnd()) error(line, "Unterminated string")

        // The closing ".
        advance()

        // Trim the surrounding quotes.
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun readNumber() {
        while (isDigit(peek())) advance()

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance()
            while (isDigit(peek())) advance()
        }
        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }

    private fun readMultilineComment() {
        val startedComments = mutableListOf(Pair(line, current))
        while(startedComments.size > 0 && !isAtEnd()) {
            when {
                peek() == '*' && peekNext() == '/' -> {
                    advance(2)
                    startedComments.removeLast()
                }
                peek() == '/' && peekNext() == '*' -> {
                    advance(2)
                    startedComments.add(Pair(line, current))
                }
                match('\n') -> line++
                else -> advance()
            }
        }

        if(startedComments.isEmpty()) return
        error(startedComments.last().first, "Comment started at pos ${startedComments.last().second} is not closed")
    }

    private fun readIdentifier() {
        while (isAlphaNumeric(peek())) advance()

        val text = source.substring(start, current)
        addToken(KEYWORDS[text] ?: TokenType.IDENTIFIER)
    }

    private fun match(c: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != c) return false

        current++
        return true
    }

    private fun peek(): Char = if (isAtEnd()) 0.toChar() else source[current]

    private fun peekNext(): Char {
        return if (current + 1 >= source.length) ZERO else source[current + 1]
    }

    private fun advance(): Char {
        current++
        return source[current - 1]
    }

    private fun advance(count: Int) {
        current += count
    }

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun isAtEnd() = current >= source.length

    companion object {
        const val ZERO = '\u0000'
    }
}