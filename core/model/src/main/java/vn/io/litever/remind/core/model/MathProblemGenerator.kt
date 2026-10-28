package vn.io.litever.remind.core.model

import java.util.Random

/**
 * Trình sinh bài toán cho nhiệm vụ giải toán khi báo thức reo.
 *
 * Tham khảo các thực hành tốt nhất từ dự án nguồn mở WakeNudge:
 * - Toán tử hiển thị chuẩn: '×', '−', '+'
 * - Chống lặp bài toán liên tiếp (tránh sleepy repeat)
 * - Ràng buộc chặt chẽ theo độ khó:
 *   + EASY: Phép cộng/trừ 1 chữ số, không có toán hạng vô nghĩa (như + 0 hay × 1), đáp án luôn dương >= 2.
 *   + NORMAL: Số 2 chữ số với số 1 chữ số, hoặc bảng cửu chương 2..9, kết quả vừa sức để đánh thức não bộ.
 *   + HARD: Phép toán kết hợp nhân và cộng/trừ (a × b + c), hoặc 2 số 2 chữ số.
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
    }

    private fun generateEasy(): MathProblem {
        // 60% phép cộng, 40% phép trừ
        return if (random.nextInt(5) < 3) {
            val a = random.nextInt(8) + 2 // 2..9
            val b = random.nextInt(8) + 2 // 2..9
            MathProblem("$a $PLUS $b", a + b)
        } else {
            val answer = random.nextInt(7) + 2 // 2..8
            val b = random.nextInt(7) + 2      // 2..8
            val a = answer + b                  // a > b, kết quả dương
            MathProblem("$a $MINUS $b", answer)
        }
    }

    private fun generateNormal(): MathProblem {
        return when (random.nextInt(3)) {
            0 -> {
                // Phép cộng 2 chữ số với 1-2 chữ số nhỏ: [11..50] + [2..15]
                val a = random.nextInt(40) + 11
                val b = random.nextInt(14) + 2
                MathProblem("$a $PLUS $b", a + b)
            }
            1 -> {
                // Phép trừ có nhớ vừa phải: [15..60] - [3..15]
                val a = random.nextInt(46) + 15
                val b = random.nextInt(13) + 3
                MathProblem("$a $MINUS $b", a - b)
            }
            else -> {
                // Bảng cửu chương từ 2..9: ví dụ 7 × 6
                val a = random.nextInt(8) + 2 // 2..9
                val b = random.nextInt(8) + 2 // 2..9
                MathProblem("$a $TIMES $b", a * b)
            }
        }
    }

    private fun generateHard(): MathProblem {
        return when (random.nextInt(3)) {
            0 -> {
                // Hai số 2 chữ số: [12..60] + [12..50]
                val a = random.nextInt(49) + 12
                val b = random.nextInt(39) + 12
                MathProblem("$a $PLUS $b", a + b)
            }
            1 -> {
                // Phép trừ 2 chữ số: [40..99] - [15..39]
                val a = random.nextInt(60) + 40
                val b = random.nextInt(25) + 15
                MathProblem("$a $MINUS $b", a - b)
            }
            else -> {
                // Phép toán hỗn hợp nhân và cộng: a × b + c
                val a = random.nextInt(8) + 3  // 3..10
                val b = random.nextInt(7) + 3  // 3..9
                val c = random.nextInt(30) + 10 // 10..39
                MathProblem("$a $TIMES $b $PLUS $c", (a * b) + c)
            }
        }
    }

    companion object {
        const val PLUS = "+"
        const val MINUS = "−"
        const val TIMES = "×"
    }
}
