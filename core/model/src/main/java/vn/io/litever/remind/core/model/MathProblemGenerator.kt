package vn.io.litever.remind.core.model

import java.util.Random

/**
 * Trình sinh bài toán cho nhiệm vụ giải toán khi báo thức reo.
 *
 * Các mức độ khó (được thiết kế để thực sự đánh thức não bộ vào buổi sáng):
 * - EASY (Dễ):
 *   + Cộng 2 số 2 chữ số (có nhớ): [25..79] + [18..59] (vd: 38 + 47)
 *   + Trừ 2 số 2 chữ số (có nhớ): [52..98] − [17..49] (vd: 73 − 38)
 *   + Bảng nhân số 1 chữ số kết hợp số nhỏ: [4..9] × [4..9] (vd: 7 × 8)
 *
 * - NORMAL (Vừa):
 *   + Nhân số 2 chữ số với 1 chữ số: [12..29] × [4..8] (vd: 17 × 6, 24 × 5)
 *   + Phép tính hỗn hợp: a × b + c với a,b ∈ [5..9], c ∈ [25..75] (vd: 8 × 7 + 49)
 *   + Cộng 3 số có 2 chữ số: [15..45] + [15..45] + [12..35] (vd: 27 + 34 + 19)
 *
 * - HARD (Khó):
 *   + Phép nhân 2 chữ số với 1 chữ số lớn hơn kèm cộng/trừ: [16..45] × [6..9] ± [25..85] (vd: 28 × 7 + 56)
 *   + Phép nhân kép: a × b + c × d với a,b,c,d ∈ [4..9] (vd: 7 × 8 + 6 × 9)
 *   + Nhân 2 chữ số: [12..25] × [11..19] (vd: 16 × 13, 14 × 15)
 *
 * - VERY_HARD (Rất khó):
 *   + Phép nhân kép lớn kèm cộng/trừ: (a × b + c × d) − e với a,b,c,d ∈ [6..12], e ∈ [15..50] (vd: 12 × 8 + 9 × 7 − 34)
 *   + Nhân 2 chữ số lớn kèm cộng số 2 chữ số: [25..65] × [12..18] + [35..95] (vd: 34 × 15 + 68)
 *   + Biểu thức 3 phép toán: a × b − c × d + e với a,b,c,d ∈ [7..14] (vd: 14 × 8 − 9 × 6 + 25)
 */
class MathProblemGenerator(
    private val random: Random = Random()
) {
    private var lastExpression: String? = null

    fun next(difficulty: MathDifficulty): MathProblem {
        var problem = build(difficulty)
        var attempts = 0
        while (problem.expression == lastExpression && attempts++ < 10) {
            problem = build(difficulty)
        }
        lastExpression = problem.expression
        return problem
    }

    private fun build(difficulty: MathDifficulty): MathProblem = when (difficulty) {
        MathDifficulty.EASY -> generateEasy()
        MathDifficulty.NORMAL -> generateNormal()
        MathDifficulty.HARD -> generateHard()
        MathDifficulty.VERY_HARD -> generateVeryHard()
    }

    private fun generateEasy(): MathProblem {
        return when (random.nextInt(3)) {
            0 -> {
                // Cộng 2 số 2 chữ số có nhớ: [25..79] + [18..59]
                val a = random.nextInt(55) + 25
                val b = random.nextInt(42) + 18
                MathProblem("$a $PLUS $b", a + b)
            }
            1 -> {
                // Trừ 2 số 2 chữ số có nhớ: [55..98] - [17..42], đảm bảo hiệu >= 12
                val a = random.nextInt(44) + 55 // 55..98
                val maxB = a - 12
                val b = random.nextInt(maxOf(1, minOf(42, maxB) - 17 + 1)) + 17 // 17..minOf(42, maxB)
                MathProblem("$a $MINUS $b", a - b)
            }
            else -> {
                // Phép nhân bảng cửu chương: [4..9] × [4..9]
                val a = random.nextInt(6) + 4
                val b = random.nextInt(6) + 4
                MathProblem("$a $TIMES $b", a * b)
            }
        }
    }

    private fun generateNormal(): MathProblem {
        return when (random.nextInt(3)) {
            0 -> {
                // Nhân 2 chữ số với 1 chữ số: [12..29] × [4..8]
                val a = random.nextInt(18) + 12
                val b = random.nextInt(5) + 4
                MathProblem("$a $TIMES $b", a * b)
            }
            1 -> {
                // Phép tính hỗn hợp: a × b + c với a,b ∈ [5..9], c ∈ [25..75]
                val a = random.nextInt(5) + 5
                val b = random.nextInt(5) + 5
                val c = random.nextInt(51) + 25
                MathProblem("$a $TIMES $b $PLUS $c", (a * b) + c)
            }
            else -> {
                // Cộng 3 số 2 chữ số: [15..45] + [15..45] + [12..35]
                val a = random.nextInt(31) + 15
                val b = random.nextInt(31) + 15
                val c = random.nextInt(24) + 12
                MathProblem("$a $PLUS $b $PLUS $c", a + b + c)
            }
        }
    }

    private fun generateHard(): MathProblem {
        return when (random.nextInt(3)) {
            0 -> {
                // Phép nhân 2 chữ số với 1 chữ số lớn hơn kèm cộng: [16..45] × [6..9] + [25..85]
                val a = random.nextInt(30) + 16
                val b = random.nextInt(4) + 6
                val c = random.nextInt(61) + 25
                MathProblem("$a $TIMES $b $PLUS $c", (a * b) + c)
            }
            1 -> {
                // Phép nhân kép: a × b + c × d với a,b,c,d ∈ [4..9]
                val a = random.nextInt(6) + 4
                val b = random.nextInt(6) + 4
                val c = random.nextInt(6) + 4
                val d = random.nextInt(6) + 4
                MathProblem("$a $TIMES $b $PLUS $c $TIMES $d", (a * b) + (c * d))
            }
            else -> {
                // Nhân 2 số 2 chữ số: [12..25] × [11..19]
                val a = random.nextInt(14) + 12
                val b = random.nextInt(9) + 11
                MathProblem("$a $TIMES $b", a * b)
            }
        }
    }

    private fun generateVeryHard(): MathProblem {
        return when (random.nextInt(3)) {
            0 -> {
                // Phép nhân kép kèm trừ: a × b + c × d − e (a,b,c,d ∈ [6..12], e ∈ [15..50])
                val a = random.nextInt(7) + 6
                val b = random.nextInt(7) + 6
                val c = random.nextInt(7) + 6
                val d = random.nextInt(7) + 6
                val sum = (a * b) + (c * d)
                val e = random.nextInt(36) + 15
                MathProblem("$a $TIMES $b $PLUS $c $TIMES $d $MINUS $e", sum - e)
            }
            1 -> {
                // Nhân 2 chữ số lớn kèm cộng: [25..65] × [12..18] + [35..95]
                val a = random.nextInt(41) + 25
                val b = random.nextInt(7) + 12
                val c = random.nextInt(61) + 35
                MathProblem("$a $TIMES $b $PLUS $c", (a * b) + c)
            }
            else -> {
                // 3 phép toán: a × b − c × d + e với a,b,c,d ∈ [7..14], đảm bảo dương
                val a = random.nextInt(8) + 7 // 7..14
                val b = random.nextInt(8) + 7 // 7..14
                val c = random.nextInt(5) + 5 // 5..9
                val d = random.nextInt(5) + 5 // 5..9
                val first = a * b
                val second = c * d
                val base = first - second
                val e = random.nextInt(41) + 20
                if (base >= 0) {
                    MathProblem("$a $TIMES $b $MINUS $c $TIMES $d $PLUS $e", base + e)
                } else {
                    MathProblem("$a $TIMES $b $PLUS $c $TIMES $d $MINUS $e", (first + second) - e)
                }
            }
        }
    }

    companion object {
        const val PLUS = "+"
        const val MINUS = "−"
        const val TIMES = "×"
    }
}
