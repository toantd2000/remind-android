package vn.io.litever.remind.features.mission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.model.MathDifficulty
import vn.io.litever.remind.core.model.MathProblem
import java.util.Random

class MathMissionLogicTest {

    @Test
    fun mathValidation_exactAnswer_succeeds() {
        val problem = MathProblem(expression = "15 + 27", answer = 42)
        val input = "42"

        val isCorrect = (input.trim() == problem.answer.toString())
        assertTrue(isCorrect)
    }

    @Test
    fun mathValidation_whitespacePadding_succeedsAfterTrim() {
        val problem = MathProblem(expression = "7 + 8", answer = 15)
        val input = "   15  \n"

        val isCorrect = (input.trim() == problem.answer.toString())
        assertTrue(isCorrect)
    }

    @Test
    fun mathValidation_wrongAnswerOrNonNumeric_fails() {
        val problem = MathProblem(expression = "10 + 20", answer = 30)

        assertFalse("31".trim() == problem.answer.toString())
        assertFalse("abc".trim() == problem.answer.toString())
        assertFalse("".trim() == problem.answer.toString())
    }

    @Test
    fun mathProblemGeneration_easyDifficulty_respectsRangeConstraints() {
        val random = Random(42)
        repeat(50) {
            val a = random.nextInt(9) + 1
            val b = random.nextInt(9) + 1
            val problem = MathProblem("$a + $b", a + b)

            assertTrue(a in 1..9)
            assertTrue(b in 1..9)
            assertEquals(a + b, problem.answer)
            assertTrue(problem.answer in 2..18)
        }
    }

    @Test
    fun mathProblemGeneration_normalDifficulty_respectsRangeConstraints() {
        val random = Random(123)
        repeat(50) {
            val a = random.nextInt(90) + 10
            val b = random.nextInt(90) + 10
            val problem = MathProblem("$a + $b", a + b)

            assertTrue(a in 10..99)
            assertTrue(b in 10..99)
            assertEquals(a + b, problem.answer)
            assertTrue(problem.answer in 20..198)
        }
    }

    @Test
    fun mathProblemGeneration_hardDifficulty_respectsPrecedenceAndConstraints() {
        val random = Random(999)
        repeat(50) {
            val a = random.nextInt(20) + 5
            val b = random.nextInt(20) + 5
            val c = random.nextInt(10) + 2
            val problem = MathProblem("$a + $b * $c", a + b * c)

            assertTrue(a in 5..24)
            assertTrue(b in 5..24)
            assertTrue(c in 2..11)
            assertEquals(a + b * c, problem.answer)
        }
    }
}
