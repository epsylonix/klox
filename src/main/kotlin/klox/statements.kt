package klox

abstract class Stmt {
    interface Visitor<R> {
        fun visitBlockStmt(stmt: Block): R
//        fun visitClassStmt(stmt: Class<*>): R
        fun visitExpressionStmt(stmt: Expression): R
        fun visitFunctionStmt(stmt: Fun): R
        fun visitIfStmt(stmt: If): R
        fun visitPrintStmt(stmt: Print): R
        fun visitReturnStmt(stmt: Return): R
        fun visitVarStmt(stmt: Var): R
        fun visitWhileStmt(stmt: While): R
        fun visitBreakStmt(stmt: Break): R
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

class Block(val statements: List<Stmt>) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitBlockStmt(this)
    }
}

class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitIfStmt(this)
    }
}

class While(val condition: Expr, val body: Stmt) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitWhileStmt(this)
    }
}

class Break : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitBreakStmt(this)
    }
}

class Fun(val name: Token, val function: Function) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitFunctionStmt(this)
    }
}

class Return(val keyword: Token, val value: Expr?) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitReturnStmt(this)
    }
}