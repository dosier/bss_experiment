package org.stan.wordlist

import com.eclipsesource.json.JsonObject

/**
 * TODO: add documentation
 *
 * @author  Stan van der Bend (https://www.rune-server.ee/members/StanDev/)
 * @since   2018-12-07
 * @version 1.0
 */
class Word(val word : String, val frequency : Int){

    fun serialize() : JsonObject {
        val jsonObject = JsonObject()
        jsonObject.add("word", word)
        jsonObject.add("frequency", frequency)
        return jsonObject
    }

    override fun toString(): String {
        return "Word(word='$word', frequency=$frequency)"
    }

    companion object {
        fun deserialize(jsonObject: JsonObject) : Word {
            return Word(jsonObject.getString("word", "UNDEFINED"), jsonObject.getInt("frequency", -1))
        }
    }
}