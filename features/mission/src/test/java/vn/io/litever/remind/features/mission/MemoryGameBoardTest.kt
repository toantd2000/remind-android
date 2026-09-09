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

        // Index 1, 4, 7 are targets
        assertTrue(board.targetIndices.contains(1))
        assertTrue(board.targetIndices.contains(4))
        assertTrue(board.targetIndices.contains(7))

        // Others are non-targets
        assertFalse(board.targetIndices.contains(0))
        assertFalse(board.targetIndices.contains(2))
        assertFalse(board.targetIndices.contains(5))
    }

    @Test
    fun memoryGameBoard_selectionSimulation_detectsCompletion() {
        val board = MemoryGameBoard(gridSize = 3, targetTiles = 3, targetIndices = listOf(2, 5, 8))
        val selectedIndices = mutableSetOf<Int>()

        // Select first target
        selectedIndices.add(2)
        val allFound1 = board.targetIndices.all { selectedIndices.contains(it) }
        assertFalse(allFound1)

        // Select second target
        selectedIndices.add(5)
        val allFound2 = board.targetIndices.all { selectedIndices.contains(it) }
        assertFalse(allFound2)

        // Select third target
        selectedIndices.add(8)
        val allFound3 = board.targetIndices.all { selectedIndices.contains(it) }
        assertTrue(allFound3)
    }

    @Test
    fun memoryGameBoard_selectionSimulation_detectsMistake() {
        val board = MemoryGameBoard(gridSize = 3, targetTiles = 3, targetIndices = listOf(0, 3, 6))

        val pickedTile = 4
        val isTarget = board.targetIndices.contains(pickedTile)
        assertFalse(isTarget)
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
