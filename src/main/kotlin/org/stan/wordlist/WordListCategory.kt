package org.stan.wordlist

import javafx.scene.paint.Color
import javafx.scene.text.Font

/**
 * Contains two categories, the distinction lies in the [font] and [color].
 *
 * @param font the [Font] of the words in a [WordList] set
 * @param color the [Color] of the words in a [WordList] set
 *
 * @author  Stan van der Bend
 * @since   2018-11-29
 * @version 1.0
 */
enum class WordListCategory(val font : Font, val color : Color) {

    EASY_TO_READ_WORDS(Font.font("Arial", 30.0), Color.BLACK),
    HARD_TO_READ_WORDS(Font.font("Monotype Corsiva", 30.0), Color.GREY.deriveColor(0.0, 1.0, 1.0, 0.6));

    fun other() : WordListCategory {
        return if(this == EASY_TO_READ_WORDS)
            HARD_TO_READ_WORDS
        else EASY_TO_READ_WORDS
    }
}