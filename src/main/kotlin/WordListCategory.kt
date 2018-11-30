import javafx.scene.paint.Color
import javafx.scene.text.Font

/**
 * TODO: add documentation
 *
 * @author  Stan van der Bend (https://www.rune-server.ee/members/StanDev/)
 * @since   2018-11-29
 * @version 1.0
 */
enum class WordListCategory(val font : Font, val color : Color) {

    EASY_TO_READ_WORDS(Font.font("Arial", 30.0), Color.BLACK),
    HARD_TO_READ_WORDS(Font.font("Monotype Corsiva", 30.0), Color.GREY.deriveColor(0.0, 1.0, 1.0, 0.6));

    fun other() : WordListCategory{
        return if(this == EASY_TO_READ_WORDS)
            HARD_TO_READ_WORDS
        else EASY_TO_READ_WORDS
    }
}