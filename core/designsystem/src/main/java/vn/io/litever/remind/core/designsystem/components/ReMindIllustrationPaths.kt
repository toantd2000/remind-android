package vn.io.litever.remind.core.designsystem.components

import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.addPathNodes
import vn.io.litever.designsystem.components.illustration.IllustrationColors
import vn.io.litever.designsystem.components.illustration.IllustrationPath

/**
 * Pre-parsed vector paths and dynamic theme token mappings for ReMind domain illustrations (Google M3 Sky theme).
 */
internal object ReMindIllustrationPaths {
    // 07 • NO ALARMS - Vầng trăng đội mũ ngủ
    val NoAlarm: List<IllustrationPath> by lazy {
        listOf(
            IllustrationPath(
                pathData = addPathNodes("M 45,172 a 55,8 0 1,0 110,0 a 55,8 0 1,0 -110,0 Z"),
                fillGetter = { it.shadow }
            ),
            IllustrationPath(
                pathData = addPathNodes("M46 164 C38 164 32 157 32 149 C32 142 37 136 44 135 C45 125 53 118 63 118 C67 118 71 119 74 122 C79 113 88 107 99 107 C112 107 123 115 126 126 C131 123 137 123 142 126 C150 130 155 138 153 147 C159 149 164 155 164 161 C164 168 158 174 150 174 H54 C50 174 46 170 46 164 Z"),
                fillGetter = { it.surface },
                strokeGetter = { it.stroke },
                strokeLineWidth = 2.2f,
                strokeLineJoin = StrokeJoin.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M14 -38 C-8 -32 -24 -12 -24 14 C-24 40 -6 58 16 62 C-2 56 -13 38 -13 14 C-13 -10 0 -29 14 -38 Z"),
                fillGetter = { it.warningContainer },
                strokeGetter = { it.stroke },
                strokeLineWidth = 2.4f,
                strokeLineJoin = StrokeJoin.Round,
                translationX = 100f,
                translationY = 102f,
                rotation = -12f
            ),
            IllustrationPath(
                pathData = addPathNodes("M-6 -34 C-4 -44 12 -48 24 -40 C28 -37 32 -30 32 -24 C32 -18 24 -14 20 -18"),
                fillGetter = { it.primaryContainer },
                strokeGetter = { it.stroke },
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                translationX = 100f,
                translationY = 102f,
                rotation = -12f
            ),
            IllustrationPath(
                pathData = addPathNodes("M 17,-18 a 3,3 0 1,0 6,0 a 3,3 0 1,0 -6,0 Z"),
                fillGetter = { it.primary },
                strokeGetter = { it.stroke },
                strokeLineWidth = 1.2f,
                translationX = 100f,
                translationY = 102f,
                rotation = -12f
            ),
            IllustrationPath(
                pathData = addPathNodes("M-19 12 Q-14 18 -9 12"),
                strokeGetter = { it.stroke },
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                translationX = 100f,
                translationY = 102f,
                rotation = -12f
            ),
            IllustrationPath(
                pathData = addPathNodes("M-16 22 Q-12 26 -8 23"),
                strokeGetter = { it.stroke },
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                translationX = 100f,
                translationY = 102f,
                rotation = -12f
            ),
            IllustrationPath(
                pathData = addPathNodes("M -8.5,18 a 2.5,1.6 0 1,0 5,0 a 2.5,1.6 0 1,0 -5,0 Z"),
                fillGetter = { it.errorContainer },
                translationX = 100f,
                translationY = 102f,
                rotation = -12f
            ),
            IllustrationPath(
                pathData = addPathNodes("M 131,74 a 3,3 0 1,0 6,0 a 3,3 0 1,0 -6,0 Z"),
                fillGetter = { it.primaryContainer },
                strokeGetter = { it.primary },
                strokeLineWidth = 1.3f
            ),
            IllustrationPath(
                pathData = addPathNodes("M 141.8,60 a 2.2,2.2 0 1,0 4.4,0 a 2.2,2.2 0 1,0 -4.4,0 Z"),
                fillGetter = { it.primaryContainer },
                strokeGetter = { it.primary },
                strokeLineWidth = 1.2f
            ),
            IllustrationPath(
                pathData = addPathNodes("M 150.5,48 a 1.5,1.5 0 1,0 3,0 a 1.5,1.5 0 1,0 -3,0 Z"),
                fillGetter = { it.primary },
                fillAlpha = 0.8f
            ),
            IllustrationPath(
                pathData = addPathNodes("M0 -4 C0 -1 1 0 4 0 C1 0 0 1 0 4 C0 1 -1 0 -4 0 C-1 0 0 -1 0 -4 Z"),
                fillGetter = { it.warningContainer },
                strokeGetter = { it.warning },
                strokeLineWidth = 1.2f,
                strokeLineJoin = StrokeJoin.Round,
                translationX = 62f,
                translationY = 78f
            )
        )
    }

    // 08 • ACTIVE ALARM - Mặt trời mắt lấp lánh & Chuông rung
    val ActiveAlarm: List<IllustrationPath> by lazy {
        listOf(
            IllustrationPath(
                pathData = addPathNodes("M 45,172 a 55,8 0 1,0 110,0 a 55,8 0 1,0 -110,0 Z"),
                fillGetter = { it.shadow }
            ),
            IllustrationPath(
                pathData = addPathNodes("M48 166 C40 166 34 160 34 152 C34 146 38 141 44 140 C45 131 53 124 63 124 C67 124 71 126 74 128 C79 119 89 113 100 113 C113 113 123 121 126 131 C131 129 136 129 141 132 C148 136 153 143 151 151 C157 153 161 158 161 164 C161 170 156 175 149 175 H54 C51 175 48 171 48 166 Z"),
                fillGetter = { it.surface },
                strokeGetter = { it.stroke },
                strokeLineWidth = 2.2f,
                strokeLineJoin = StrokeJoin.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M72 58 C66 50 78 44 86 50 Z"),
                fillGetter = { it.warningContainer },
                strokeGetter = { it.stroke },
                strokeLineWidth = 2.2f,
                strokeLineJoin = StrokeJoin.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M128 58 C134 50 122 44 114 50 Z"),
                fillGetter = { it.warningContainer },
                strokeGetter = { it.stroke },
                strokeLineWidth = 2.2f,
                strokeLineJoin = StrokeJoin.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M 96,44 H 104 a 2,2 0 0,1 2,2 V 48 a 2,2 0 0,1 -2,2 H 96 a 2,2 0 0,1 -2,-2 V 46 a 2,2 0 0,1 2,-2 Z"),
                fillGetter = { it.successContainer },
                strokeGetter = { it.stroke },
                strokeLineWidth = 2f
            ),
            IllustrationPath(
                pathData = addPathNodes("M 64,92 a 36,36 0 1,0 72,0 a 36,36 0 1,0 -72,0 Z"),
                fillGetter = { it.warningContainer },
                strokeGetter = { it.stroke },
                strokeLineWidth = 2.4f
            ),
            IllustrationPath(
                pathData = addPathNodes("M 82,88 a 6,6 0 1,0 12,0 a 6,6 0 1,0 -12,0 Z"),
                fillGetter = { it.stroke }
            ),
            IllustrationPath(
                pathData = addPathNodes("M 83.8,86 a 2.2,2.2 0 1,0 4.4,0 a 2.2,2.2 0 1,0 -4.4,0 Z"),
                fillGetter = { it.surface }
            ),
            IllustrationPath(
                pathData = addPathNodes("M 88.9,90 a 1.1,1.1 0 1,0 2.2,0 a 1.1,1.1 0 1,0 -2.2,0 Z"),
                fillGetter = { it.surface }
            ),
            IllustrationPath(
                pathData = addPathNodes("M 106,88 a 6,6 0 1,0 12,0 a 6,6 0 1,0 -12,0 Z"),
                fillGetter = { it.stroke }
            ),
            IllustrationPath(
                pathData = addPathNodes("M 107.8,86 a 2.2,2.2 0 1,0 4.4,0 a 2.2,2.2 0 1,0 -4.4,0 Z"),
                fillGetter = { it.surface }
            ),
            IllustrationPath(
                pathData = addPathNodes("M 112.9,90 a 1.1,1.1 0 1,0 2.2,0 a 1.1,1.1 0 1,0 -2.2,0 Z"),
                fillGetter = { it.surface }
            ),
            IllustrationPath(
                pathData = addPathNodes("M94 100 Q100 108 106 100"),
                strokeGetter = { it.stroke },
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M 76.8,97 a 3.2,2 0 1,0 6.4,0 a 3.2,2 0 1,0 -6.4,0 Z"),
                fillGetter = { it.errorContainer }
            ),
            IllustrationPath(
                pathData = addPathNodes("M 116.8,97 a 3.2,2 0 1,0 6.4,0 a 3.2,2 0 1,0 -6.4,0 Z"),
                fillGetter = { it.errorContainer }
            ),
            IllustrationPath(
                pathData = addPathNodes("M 58.00,54.00 L 57.76,54.12 L 57.53,54.25 L 57.30,54.38 L 57.08,54.51 L 56.86,54.64 L 56.65,54.78 L 56.44,54.92 L 56.23,55.07 L 56.13,55.15 M 53.49,57.86 L 53.44,57.94 L 53.31,58.13 L 53.19,58.32 L 53.07,58.51 L 52.96,58.70 L 52.85,58.90 L 52.75,59.09 L 52.65,59.29 L 52.55,59.49 L 52.46,59.69 L 52.42,59.78 M 51.45,63.44 L 51.43,63.63 L 51.41,63.84 L 51.39,64.05 L 51.38,64.26 L 51.38,64.47 L 51.37,64.67 L 51.37,64.88 L 51.38,65.09 L 51.38,65.29 L 51.39,65.50 L 51.40,65.63 M 52.25,69.32 L 52.25,69.33 L 52.33,69.50 L 52.40,69.67 L 52.48,69.84 L 52.57,70.00 L 52.65,70.16 L 52.74,70.32 L 52.83,70.48 L 52.92,70.63 L 53.02,70.78 L 53.12,70.93 L 53.22,71.08 L 53.32,71.22 L 53.33,71.23"),
                strokeGetter = { it.success },
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M 142.00,54.00 L 142.24,54.12 L 142.47,54.25 L 142.70,54.38 L 142.92,54.51 L 143.14,54.64 L 143.35,54.78 L 143.56,54.92 L 143.77,55.07 L 143.87,55.15 M 146.51,57.86 L 146.56,57.94 L 146.69,58.13 L 146.81,58.32 L 146.93,58.51 L 147.04,58.70 L 147.15,58.90 L 147.25,59.09 L 147.35,59.29 L 147.45,59.49 L 147.54,59.69 L 147.58,59.78 M 148.55,63.44 L 148.57,63.63 L 148.59,63.84 L 148.61,64.05 L 148.62,64.26 L 148.62,64.47 L 148.63,64.67 L 148.63,64.88 L 148.62,65.09 L 148.62,65.29 L 148.61,65.50 L 148.60,65.63 M 147.75,69.32 L 147.75,69.33 L 147.67,69.50 L 147.60,69.67 L 147.52,69.84 L 147.43,70.00 L 147.35,70.16 L 147.26,70.32 L 147.17,70.48 L 147.08,70.63 L 146.98,70.78 L 146.88,70.93 L 146.78,71.08 L 146.68,71.22 L 146.67,71.23"),
                strokeGetter = { it.success },
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M 100,36 L 100,28"),
                strokeGetter = { it.warning },
                strokeLineWidth = 2.4f,
                strokeLineCap = StrokeCap.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M 60,42 L 52,36"),
                strokeGetter = { it.warning },
                strokeLineWidth = 2.4f,
                strokeLineCap = StrokeCap.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M 140,42 L 148,36"),
                strokeGetter = { it.warning },
                strokeLineWidth = 2.4f,
                strokeLineCap = StrokeCap.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M0 -5 C0 -1 1 0 5 0 C1 0 0 1 0 5 C0 1 -1 0 -5 0 C-1 0 0 -1 0 -5 Z"),
                fillGetter = { it.success },
                strokeGetter = { it.stroke },
                strokeLineWidth = 1.2f,
                strokeLineJoin = StrokeJoin.Round,
                translationX = 142f,
                translationY = 88f
            )
        )
    }

    // 09 • NO REMINDERS - Đám mây thảnh thơi
    val NoReminder: List<IllustrationPath> by lazy {
        listOf(
            IllustrationPath(
                pathData = addPathNodes("M 45,172 a 55,8 0 1,0 110,0 a 55,8 0 1,0 -110,0 Z"),
                fillGetter = { it.shadow }
            ),
            IllustrationPath(
                pathData = addPathNodes("M52 162 C42 162 34 154 34 144 C34 135 40 128 48 126 C47 122 46 118 46 114 C46 96 60 82 78 82 C85 82 92 84 98 89 C105 78 117 72 130 72 C149 72 164 86 164 105 C164 108 163 111 162 114 C170 118 176 126 176 136 C176 147 167 156 156 156 C153 156 151 155 149 154 C144 159 137 162 129 162 Z"),
                fillGetter = { it.surface },
                strokeGetter = { it.stroke },
                strokeLineWidth = 2.2f,
                strokeLineJoin = StrokeJoin.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M84 122 Q89 127 94 122"),
                strokeGetter = { it.stroke },
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M112 122 Q117 127 122 122"),
                strokeGetter = { it.stroke },
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M100 134 Q103 138 106 134"),
                strokeGetter = { it.stroke },
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M 75,126 a 3,1.8 0 1,0 6,0 a 3,1.8 0 1,0 -6,0 Z"),
                fillGetter = { it.errorContainer }
            ),
            IllustrationPath(
                pathData = addPathNodes("M 125,126 a 3,1.8 0 1,0 6,0 a 3,1.8 0 1,0 -6,0 Z"),
                fillGetter = { it.errorContainer }
            ),
            IllustrationPath(
                pathData = addPathNodes("M -7,-12 H 7 a 3,3 0 0,1 3,3 V 9 a 3,3 0 0,1 -3,3 H -7 a 3,3 0 0,1 -3,-3 V -9 a 3,3 0 0,1 3,-3 Z"),
                fillGetter = { it.warningContainer },
                strokeGetter = { it.stroke },
                strokeLineWidth = 1.8f,
                translationX = 138f,
                translationY = 70f,
                rotation = 15f
            ),
            IllustrationPath(
                pathData = addPathNodes("M-4 -2 L-1 2 L5 -4"),
                strokeGetter = { it.success },
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                translationX = 138f,
                translationY = 70f,
                rotation = 15f
            ),
            IllustrationPath(
                pathData = addPathNodes("M 50.00,96.00 L 50.36,95.88 L 50.72,95.77 L 51.09,95.66 L 51.45,95.56 L 51.81,95.46 L 52.18,95.36 L 52.19,95.36 M 55.84,94.64 L 55.89,94.64 L 56.26,94.58 L 56.64,94.54 L 57.02,94.49 L 57.39,94.45 L 57.77,94.41 L 58.10,94.38 M 61.81,94.16 L 61.94,94.16 L 62.32,94.15 L 62.70,94.14 L 63.08,94.14 L 63.46,94.14 L 63.84,94.14 L 64.09,94.14 M 67.81,94.21 L 68.00,94.22 L 68.37,94.24 L 68.75,94.25 L 69.12,94.27 L 69.50,94.28 L 69.87,94.30 L 70.09,94.31 M 73.81,94.48 L 73.92,94.48 L 74.29,94.50 L 74.65,94.51 L 75.01,94.52 L 75.37,94.54 L 75.73,94.55 L 76.08,94.56 L 76.09,94.56 M 79.81,94.59 L 79.91,94.59 L 80.25,94.58 L 80.58,94.57 L 80.92,94.56 L 81.25,94.54 L 81.58,94.53 L 81.91,94.51 L 82.08,94.50 M 85.77,94.04 L 86.00,94.00"),
                strokeGetter = { it.primaryContainerSecondary },
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M 164.2,88 a 1.8,1.8 0 1,0 3.6,0 a 1.8,1.8 0 1,0 -3.6,0 Z"),
                fillGetter = { it.primaryContainerSecondary }
            ),
            IllustrationPath(
                pathData = addPathNodes("M 36.5,112 a 1.5,1.5 0 1,0 3,0 a 1.5,1.5 0 1,0 -3,0 Z"),
                fillGetter = { it.primaryContainerSecondary }
            )
        )
    }

    // 10 • ACTIVE REMINDERS - Chuông thông báo mây
    val ActiveReminder: List<IllustrationPath> by lazy {
        listOf(
            IllustrationPath(
                pathData = addPathNodes("M 45,172 a 55,8 0 1,0 110,0 a 55,8 0 1,0 -110,0 Z"),
                fillGetter = { it.shadow }
            ),
            IllustrationPath(
                pathData = addPathNodes("M52 164 C44 164 38 158 38 150 C38 143 43 138 49 137 C50 128 58 122 67 122 C71 122 75 123 78 125 C83 117 92 112 103 112 C115 112 125 119 128 129 C133 127 138 127 143 129 C149 133 154 140 152 148 C158 150 162 155 162 161 C162 167 157 172 150 172 H58 C55 172 52 168 52 164 Z"),
                fillGetter = { it.surface },
                strokeGetter = { it.stroke },
                strokeLineWidth = 2.2f,
                strokeLineJoin = StrokeJoin.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M-18 20 C-18 8 -14 -12 0 -16 C14 -12 18 8 18 20 Z"),
                fillGetter = { it.warningContainer },
                strokeGetter = { it.stroke },
                strokeLineWidth = 2.4f,
                strokeLineJoin = StrokeJoin.Round,
                translationX = 100f,
                translationY = 88f
            ),
            IllustrationPath(
                pathData = addPathNodes("M-22 20 H22 C22 24 18 26 14 26 H-14 C-18 26 -22 24 -22 20 Z"),
                fillGetter = { it.warning },
                strokeGetter = { it.stroke },
                strokeLineWidth = 2f,
                strokeLineJoin = StrokeJoin.Round,
                translationX = 100f,
                translationY = 88f
            ),
            IllustrationPath(
                pathData = addPathNodes("M -4,30 a 4,4 0 1,0 8,0 a 4,4 0 1,0 -8,0 Z"),
                fillGetter = { it.stroke },
                translationX = 100f,
                translationY = 88f
            ),
            IllustrationPath(
                pathData = addPathNodes("M-4 -16 C-4 -22 4 -22 4 -16"),
                strokeGetter = { it.stroke },
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                translationX = 100f,
                translationY = 88f
            ),
            IllustrationPath(
                pathData = addPathNodes("M -8.2,6 a 2.2,2.2 0 1,0 4.4,0 a 2.2,2.2 0 1,0 -4.4,0 Z"),
                fillGetter = { it.stroke },
                translationX = 100f,
                translationY = 88f
            ),
            IllustrationPath(
                pathData = addPathNodes("M 3.8,6 a 2.2,2.2 0 1,0 4.4,0 a 2.2,2.2 0 1,0 -4.4,0 Z"),
                fillGetter = { it.stroke },
                translationX = 100f,
                translationY = 88f
            ),
            IllustrationPath(
                pathData = addPathNodes("M-3 12 Q0 15 3 12"),
                strokeGetter = { it.stroke },
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                translationX = 100f,
                translationY = 88f
            ),
            IllustrationPath(
                pathData = addPathNodes("M0 -7 C0 -2 2 0 7 0 C2 0 0 2 0 7 C0 2 -2 0 -7 0 C-2 0 0 -2 0 -7 Z"),
                fillGetter = { it.primaryContainer },
                strokeGetter = { it.primary },
                strokeLineWidth = 1.6f,
                strokeLineJoin = StrokeJoin.Round,
                translationX = 136f,
                translationY = 56f
            ),
            IllustrationPath(
                pathData = addPathNodes("M -1.8,0 a 1.8,1.8 0 1,0 3.6,0 a 1.8,1.8 0 1,0 -3.6,0 Z"),
                fillGetter = { it.primary },
                translationX = 136f,
                translationY = 56f
            ),
            IllustrationPath(
                pathData = addPathNodes("M -10,0 a 10,10 0 1,0 20,0 a 10,10 0 1,0 -20,0 Z"),
                fillGetter = { it.error },
                strokeGetter = { it.surface },
                strokeLineWidth = 1.8f,
                translationX = 68f,
                translationY = 62f
            ),
            IllustrationPath(
                pathData = addPathNodes("M -1.8,-1.2 L 0,-3.5 V 4"),
                strokeGetter = { it.surface },
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                translationX = 68f,
                translationY = 62f
            ),
            IllustrationPath(
                pathData = addPathNodes("M66 94 C62 90 62 82 66 78"),
                strokeGetter = { it.warning },
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ),
            IllustrationPath(
                pathData = addPathNodes("M134 94 C138 90 138 82 134 78"),
                strokeGetter = { it.warning },
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            )
        )
    }

    fun get(type: ReMindIllustrationType): List<IllustrationPath> = when (type) {
        ReMindIllustrationType.NO_ALARM -> NoAlarm
        ReMindIllustrationType.ACTIVE_ALARM -> ActiveAlarm
        ReMindIllustrationType.NO_REMINDER -> NoReminder
        ReMindIllustrationType.ACTIVE_REMINDER -> ActiveReminder
    }
}
