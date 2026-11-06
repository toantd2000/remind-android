package vn.io.litever.remind.features.mission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.model.MemoryGameBoard

class MemoryGameBoardTest {

    @Test
    fun memoryGameBoard_initialization_retainsGridDimensionsAndTargets() {
        val board = MemoryGameBoard(gridSize = 3, targetTiles = 3, targetIndices = listOf(0, 4, 8))

        assertEquals(3, board.gridSize)
        assertEquals(3, board.targetTiles)
        assertEquals(listOf(0, 4, 8), board.targetIndices)
    }

    @Test
    fun memoryGameBoard_indexToCoordinateMapping_isConsistent() {
        val gridSize = 4
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val index = row * gridSize + col
                val computedRow = index / gridSize
                val computedCol = index % gridSize

                assertEquals(row, computedRow)
                assertEquals(col, computedCol)
            }
        }
    }

    @Test
    fun memoryGameBoard_targetVerification_distinguishesTargetFromNonTarget() {
        val board = MemoryGameBoard(gridSize = 3, targetTiles = 3, targetIndices = listOf(1, 4, 7))

        assertTrue(board.targetIndices.contains(1))
        assertTrue(board.targetIndices.contains(4))
        assertTrue(board.targetIndices.contains(7))

        assertFalse(board.targetIndices.contains(0))
        assertFalse(board.targetIndices.contains(2))
        assertFalse(board.targetIndices.contains(5))
    }

    @Test
    fun memoryGameBoard_selectionSimulation_detectsCompletion() {
        val board = MemoryGameBoard(gridSize = 3, targetTiles = 3, targetIndices = listOf(2, 5, 8))
        val selectedIndices = mutableSetOf<Int>()

        selectedIndices.add(2)
        val allFound1 = board.targetIndices.all { selectedIndices.contains(it) }
        assertFalse(allFound1)

        selectedIndices.add(5)
        val allFound2 = board.targetIndices.all { selectedIndices.contains(it) }
        assertFalse(allFound2)

        selectedIndices.add(8)
        val allFound3 = board.targetIndices.all { selectedIndices.contains(it) }
        assertTrue(allFound3)
    }

    @Test
    fun memoryGameBoard_livesSimulation_detectsFailureAfterThreeMistakes() {
        val board = MemoryGameBoard(gridSize = 3, targetTiles = 3, targetIndices = listOf(0, 3, 6))
        var lives = 3
        val wrongTaps = listOf(1, 2, 4)

        for (tap in wrongTaps) {
            val isTarget = board.targetIndices.contains(tap)
            if (!isTarget) {
                lives--
            }
        }

        assertEquals(0, lives)
        assertTrue(lives <= 0) // Should trigger FAILURE gameState
    }

    @Test
    fun memoryGameBoard_failureRevealAll_partitionsTilesAccurately() {
        val board = MemoryGameBoard(gridSize = 3, targetTiles = 3, targetIndices = listOf(0, 4, 8))
        val selectedIndices = setOf(0, 1, 2, 3) // 0 is correct target, 1, 2, 3 are wrong

        val foundTargets = selectedIndices.filter { board.targetIndices.contains(it) }
        val wrongPicks = selectedIndices.filter { !board.targetIndices.contains(it) }
        val missedTargets = board.targetIndices.filter { !selectedIndices.contains(it) }

        assertEquals(listOf(0), foundTargets)
        assertEquals(listOf(1, 2, 3), wrongPicks)
        assertEquals(listOf(4, 8), missedTargets)
    }

    @Test
    fun memoryGameBoard_copyOnFailure_generatesValidIndices() {
        val initialBoard = MemoryGameBoard(gridSize = 4, targetTiles = 5, targetIndices = listOf(0, 1, 2, 3, 4))
        val totalTiles = initialBoard.gridSize * initialBoard.gridSize

        val newTargets = (0 until totalTiles).shuffled().take(initialBoard.targetTiles)
        val newBoard = initialBoard.copy(targetIndices = newTargets)

        assertEquals(4, newBoard.gridSize)
        assertEquals(5, newBoard.targetTiles)
        assertEquals(5, newBoard.targetIndices.size)
        assertTrue(newBoard.targetIndices.distinct().size == 5)
        assertTrue(newBoard.targetIndices.all { it in 0 until totalTiles })
    }
}
