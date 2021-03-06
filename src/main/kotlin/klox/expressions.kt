package klox

abstract class Expr {
    interface Visitor<R> {
        fun visitAssignExpr(expr: Assign): R
        fun visitBinaryExpr(expr: Binary): R

        fun visitCallExpr(expr: Call): R
//    fun visitGetExpr(expr: Get?): R

        fun visitGroupingExpr(expr: Grouping): R
        fun visitLiteralExpr(expr: Literal): R
        fun visitLogicalExpr(expr: Logical): R

//    fun visitSetExpr(expr: Set<*>): R
//    fun visitSuperExpr(expr: Super): R
//    fun visitThisExpr(expr: This): R
        fun visitUnaryExpr(expr: Unary): R
        fun visitFunctionExpr(expr: Function): R
        fun visitVariableExpr(expr: Variable): R
    }

    abstract fun <R> accept(visitor: Visitor<R>): R
}

data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitBinaryExpr(this)
    }
}

data class Grouping(val expression: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitGroupingExpr(this)
    }
}

data class Literal(val value: Any?) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitLiteralExpr(this)
    }
}

data class Unary(val operator: Token, val right: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitUnaryExpr(this)
    }
}

data class Variable(val name: Token) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitVariableExpr(this)
    }
}

data class Assign(val name: Token, val value: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitAssignExpr(this)
    }
}

data class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitLogicalExpr(this)
    }
}

data class Function(val params: List<Token>, val body: List<Stmt>) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitFunctionExpr(this)
    }
}

data class Call(val callee: Expr, val paren: Token, val arguments: List<Expr>) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitCallExpr(this)
    }
}