import com.eclipsesource.json.JsonArray
import java.util.*

/**
 * TODO: add documentation
 *
 * @author  Stan van der Bend (https://www.rune-server.ee/members/StanDev/)
 * @since   2018-11-29
 * @version 1.0
 */
class WordList(vararg words: String) : LinkedList<String>() {

    init {
        addAll(words)
    }

    fun serialize() : JsonArray {

        val jsonArray = JsonArray()
        for (word in this)
            jsonArray.add(word)

        return jsonArray
    }

    companion object {

        val EASY_LISTS = arrayOf(
            WordList("test1", "test2", "test3"),
            WordList("boo1", "boo2", "boo3")
        )

        val HARD_LISTS = arrayOf(
            WordList("foo1", "foo2", "foo3"),
            WordList("apple1", "apple2", "apple3")
        )

    }


}