package aq

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ScannerTest {

    @Test
    fun scanTokens() {
        val source =
        """
            for (i = 1; i < 5; i++) {
              do some stuff 
              /* here is
              some multiline  
              comment */
              var x = "a"
            }
        """

//        val result = Scanner(source).scanTokens()
//        result
//        assertEquals(listOf(), result)
    }
}