package vn.io.litever.remind.features.mission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.model.MathDifficulty
import vn.io.litever.remind.core.model.MathProblem
import vn.io.litever.remind.core.model.MathProblemGenerator
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
    fun mathProblemGenerator_easyDifficulty_generatesChallenging2DigitOrMultiplication() {
        val generator = MathProblemGenerator(Random(42))
        repeat(50) {
            val problem = generator.next(MathDifficulty.EASY)
            println("EASY: ${problem.expression} = ${problem.answer}")
            assertTrue("Answer must be >= 10: ${problem.answer} for ${problem.expression}", problem.answer >= 10)
            assertTrue(
                "Expression must contain +, −, or ×: ${problem.expression}",
                problem.expression.contains("+") || problem.expression.contains(MathProblemGenerator.MINUS) || problem.expression.contains(MathProblemGenerator.TIMES)
            )
        }
    }

    @Test
    fun mathProblemGenerator_normalDifficulty_generatesMentalMathProblems() {
        val generator = MathProblemGenerator(Random(123))
        repeat(50) {
            val problem = generator.next(MathDifficulty.NORMAL)
            assertTrue("Answer must be >= 40: ${problem.answer}", problem.answer >= 40)
            assertTrue(
                "Expression must contain +, or ×: ${problem.expression}",
                problem.expression.contains("+") || problem.expression.contains("×")
            )
        }
    }

    @Test
    fun mathProblemGenerator_hardDifficulty_generatesChallengingMath() {
        val generator = MathProblemGenerator(Random(999))
        repeat(50) {
            val problem = generator.next(MathDifficulty.HARD)
            assertTrue("Answer must be >= 40: ${problem.answer}", problem.answer >= 40)
        }
    }

    @Test
    fun mathProblemGenerator_veryHardDifficulty_generatesComplexMath() {
        val generator = MathProblemGenerator(Random(1001))
        repeat(50) {
            val problem = generator.next(MathDifficulty.VERY_HARD)
            assertTrue("Answer must be > 0: ${problem.answer}", problem.answer > 0)
            assertTrue(
                "Expression must contain operators: ${problem.expression}",
                problem.expression.contains("+") || problem.expression.contains("−") || problem.expression.contains("×")
            )
        }
    }

    @Test
    fun mathProblemGenerator_antiRepeat_avoidsDirectConsecutiveDuplicates() {
        val generator = MathProblemGenerator(Random(777))
        var prev = generator.next(MathDifficulty.EASY)
        var repeats = 0
        repeat(30) {
            val current = generator.next(MathDifficulty.EASY)
            if (current.expression == prev.expression) {
                repeats++
            }
            prev = current
        }
        assertEquals("Consecutive repeats should be avoided", 0, repeats)
    }
}
