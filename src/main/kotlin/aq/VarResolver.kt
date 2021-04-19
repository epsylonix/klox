package aq

import java.util.*


class VarResolver(private val interpreter: Interpreter) : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {
    private enum class FunctionType {
        NONE, FUNCTION
    }

    private val scopes = Stack<MutableMap<String, Boolean>>()
    private var currentFunction = FunctionType.NONE

    override fun visitAssignExpr(expr: Assign) {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
    }

    override fun visitBinaryExpr(expr: Binary) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitCallExpr(expr: Call) {
        resolve(expr.callee)
        expr.arguments.forEach(::resolve)
    }

    override fun visitGroupingExpr(expr: Grouping) {
        resolve(expr.expression)
    }

    override fun visitLiteralExpr(expr: Literal) {
    }

    override fun visitLogicalExpr(expr: Logical) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitUnaryExpr(expr: Unary) {
        resolve(expr.right)
    }

    override fun visitFunctionExpr(expr: Function) {
        beginScope()

        expr.params.forEach {
            declare(it)
            define(it)
        }
        resolve(expr.body)

        endScope()
    }

    override fun visitVariableExpr(expr: Variable) {
        // same check as in VariableStmt
        if (!scopes.isEmpty() && scopes.peek()[expr.name.lexeme] == false) {
            error(expr.name, "Variable can't be initialized with itself");
        }

        resolveLocal(expr, expr.name);
    }

    override fun visitBlockStmt(stmt: Block) {
        beginScope()
        resolve(stmt.statements)
        endScope()
    }

    override fun visitExpressionStmt(stmt: Expression) {
        resolve(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Fun) {
        // declaration is done before the fun body is processed
        // to allow recursion
        declare(stmt.name)
        define(stmt.name)

        resolveFunction(stmt, FunctionType.FUNCTION)
    }

    override fun visitIfStmt(stmt: If) {
        resolve(stmt.condition)
        // what branch will be executed is only known at runtime
        // so here we have to resolve them both
        resolve(stmt.thenBranch)
        stmt.elseBranch?.let(::resolve)
    }

    override fun visitPrintStmt(stmt: Print) {
        resolve(stmt.expression)
    }

    override fun visitReturnStmt(stmt: Return) {
        if (currentFunction == FunctionType.NONE) {
            error(stmt.keyword, "return can't be used outside of a function");
        }

        stmt.value ?: return
        resolve(stmt.value)
    }

    override fun visitVarStmt(stmt: Var) {
        /*
            split into declaration and initialization to deal with code like this:
            var a = ...;
            {
              var a = a; // this would be a compile-time error
            }
         */
        declare(stmt.name) // put variable into current scope but mark it as not ready to be used
        stmt.initializer?.let(::resolve) // resolve initializer
        define(stmt.name) // mark variable as ready to be used
    }

    override fun visitWhileStmt(stmt: While) {
        resolve(stmt.condition)
        resolve(stmt.body)
    }

    override fun visitBreakStmt(stmt: Break) {
    }

    private fun beginScope() {
        scopes.push(mutableMapOf())
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return
        val scope = scopes.peek()

        if (scope.containsKey(name.lexeme)) {
            error(name, "Redefining var ${name.lexeme}");
        }

        scope[name.lexeme] = false
    }

    private fun resolveLocal(expr: Expr, name: Token) {
        scopes
            .indices.findLast { scopes[it].containsKey(name.lexeme) }
            ?.let { interpreter.resolve(expr, scopes.size - 1 - it) }
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return
        scopes.peek()[name.lexeme] = true
    }

    fun resolve(statements: List<Stmt>) {
        for (statement in statements) {
            resolve(statement)
        }
    }

    private fun resolve(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun resolve(expr: Expr) {
        expr.accept(this)
    }

    private fun resolveFunction(stmt: Fun, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type

        beginScope()
        stmt.function.params.forEach {
            declare(it)
            define(it)
        }

        resolve(stmt.function.body)
        endScope()

        currentFunction = enclosingFunction
    }
}