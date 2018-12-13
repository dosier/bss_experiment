package org.stan.wordlist

import com.eclipsesource.json.JsonArray
import java.util.*

/**
 * A [LinkedList] containing [String] values.
 *
 * @author  Stan van der Bend
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

        val TEST_LISTS = arrayOf(
            WordList("test1", "test2", "test3"),
            WordList("boot1", "boot2", "boot3", "boot4")
        )

    }

}