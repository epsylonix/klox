package aq

abstract class Stmt {
    interface Visitor<R> {
//        fun visitBlockStmt(stmt: Block): R
//        fun visitClassStmt(stmt: Class<*>): R
        fun visitExpressionStmt(stmt: Expression): R
//        fun visitFunctionStmt(stmt: Function): R
//        fun visitIfStmt(stmt: If): R
        fun visitPrintStmt(stmt: Print): R
//        fun visitReturnStmt(stmt: Return): R
        fun visitVarStmt(stmt: Var): R
//        fun visitWhileStmt(stmt: While): R
    }

    // Nested Stmt classes here...
    abstract fun <R> accept(visitor: Visitor<R>): R
}

class Print(val expression: Expr) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitPrintStmt(this)
    }
}

class Expression(val expression: Expr) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitExpressionStmt(this)
    }
}

class Var(val name: Token, val initializer: Expr?) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitVarStmt(this)
    }
}