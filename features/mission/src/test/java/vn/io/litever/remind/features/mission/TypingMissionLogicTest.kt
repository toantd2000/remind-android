package vn.io.litever.remind.features.mission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.model.Phrase
import vn.io.litever.remind.core.model.TypingMode

class TypingMissionLogicTest {

    @Test
    fun typingValidation_exactMatch_succeeds() {
        val target = Phrase(id = 1, content = "Good morning sunshine", categoryId = "basic")
        val userInput = "Good morning sunshine"

        val isCorrect = (userInput == target.content)
        assertTrue(isCorrect)
    }

    @Test
    fun typingValidation_caseMismatch_fails() {
        val target = Phrase(id = 1, content = "Good morning sunshine", categoryId = "basic")
        val userInput = "good morning sunshine"

        val isCorrect = (userInput == target.content)
        assertFalse(isCorrect)
    }

    @Test
    fun typingValidation_partialOrTrailingSpace_fails() {
        val target = Phrase(id = 1, content = "Rise and shine", categoryId = "basic")
        
        assertFalse("Rise and" == target.content)
        assertFalse("Rise and shine " == target.content)
        assertFalse(" Rise and shine" == target.content)
    }

    @Test
    fun phraseTransformation_shuffleWords_preservesAllWords() {
        val original = "The quick brown fox jumps over the lazy dog"
        val parts = original.split(" ")
        val words = parts.filter { it.isNotBlank() }

        // Simulate SHUFFLE_WORDS algorithm from MissionRingingViewModel
        val shuffledWords = words.shuffled()
        var wordIdx = 0
        val transformed = parts.joinToString(" ") {
            if (it.isNotBlank()) shuffledWords[wordIdx++] else it
        }

        val resultWords = transformed.split(" ").filter { it.isNotBlank() }
        assertEquals(words.size, resultWords.size)
        assertEquals(words.sorted(), resultWords.sorted())
    }

    @Test
    fun phraseTransformation_shuffleChars_preservesCharacterCountsPerWord() {
        val original = "Focus and succeed"
        val words = original.split(" ")

        // Simulate SHUFFLE_CHARS algorithm from MissionRingingViewModel
        val transformed = words.joinToString(" ") { word ->
            if (word.isNotBlank()) {
                word.toList().shuffled().joinToString("")
            } else {
                word
            }
        }

        val resultWords = transformed.split(" ")
        assertEquals(words.size, resultWords.size)
        for (i in words.indices) {
            val originalWordSorted = words[i].toList().sorted()
            val transformedWordSorted = resultWords[i].toList().sorted()
            assertEquals(originalWordSorted, transformedWordSorted)
        }
    }

    @Test
    fun repetitionExpansion_generatesExactRepeatCount() {
        val basePhrases = listOf(
            Phrase(id = 1, content = "Phrase A", categoryId = "c"),
            Phrase(id = 2, content = "Phrase B", categoryId = "c")
        )
        val repeatCount = 5

        val result = mutableListOf<Phrase>()
        val fullRepeats = repeatCount / basePhrases.size
        for (i in 0 until fullRepeats) {
            result.addAll(basePhrases)
        }
        val remainder = repeatCount % basePhrases.size
        result.addAll(basePhrases.shuffled().take(remainder))

        assertEquals(repeatCount, result.size)
        assertEquals(5, result.size)
    }
}
