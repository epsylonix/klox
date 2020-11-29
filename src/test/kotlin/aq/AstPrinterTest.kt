//package aq
//
//import org.junit.jupiter.api.Test
//import kotlin.test.assertEquals
//
//internal class AstPrinterTest {
//
//    @Test
//    fun print() {
//        val expression: Expr = Binary(
//            Unary(
//                Token(TokenType.MINUS, "-", null, 1),
//                Literal(123)
//            ),
//            Token(TokenType.STAR, "*", null, 1),
//            Grouping(
//                Literal(45.67)
//            )
//        )
//
//        assertEquals(
//            "(* (- 123) (group 45.67))",
//            AstPrinter().print(expression)
//        )
//    }
//}