//package aq
//
//internal class AstPrinter : Expr.Visitor<String> {
//    fun print(expr: Expr): String {
//        return expr.accept(this)
//    }
//
//    override fun visitBinaryExpr(expr: Binary): String {
//        return parenthesize(
//            expr.operator.lexeme,
//            expr.left, expr.right
//        )
//    }
//
//    override fun visitGroupingExpr(expr: Grouping): String {
//        return parenthesize("group", expr.expression)
//    }
//
//    override fun visitLiteralExpr(expr: Literal): String {
//        return expr.value?.toString() ?: "nil"
//    }
//
//    override fun visitUnaryExpr(expr: Unary): String {
//        return parenthesize(expr.operator.lexeme, expr.right)
//    }
//
//    override fun visitVariableExpr(expr: Variable): String {
//        return parenthesize("var", expr)
//    }
//
//    override fun visitAssignExpr(expr: Assign): String {
//        return parenthesize("assign", expr)
//    }
//
//    override fun visitLogicalExpr(expr: Logical): String {
//        return parenthesize(expr.operator.literal.toString(), expr.left, expr.right)
//    }
//
//    private fun parenthesize(name: String, vararg exprs: Expr): String {
//        val builder = StringBuilder()
//
//        builder.append("(").append(name)
//        exprs.forEach { builder.append(" ").append(it.accept(this)) }
//        builder.append(")")
//
//        return builder.toString()
//    }
//}
