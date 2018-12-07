package org.stan.wordlist

import com.eclipsesource.json.Json
import java.io.FileReader
import java.nio.file.Paths

/**
 * TODO: add documentation
 *
 * @author  Stan van der Bend (https://www.rune-server.ee/members/StanDev/)
 * @since   2018-12-03
 * @version 1.0
 */
class WordListGenerator(private val category: WordListCategory, private val amount: Int) {

    fun generate() : WordList {

        val wordList = WordList()

        val availableWords = mappedWords[category]!!

        if(availableWords.size < amount){
            println("Not enough words left!!!")
            return wordList
        }

        for (i in 0..amount)
            wordList.add(availableWords.remove())

        return wordList
    }

    companion object {

        private val mappedWords = HashMap<WordListCategory, WordList>()

        fun load(){
            val jsonObject = Json.parse(FileReader(Paths.get("data", "used_words.json").toFile())).asObject()
            val jsonArray = jsonObject.get("words").asArray()
            val words = jsonArray.map { Word.deserialize(it.asObject()) }.map { it.word }.toTypedArray()

            mappedWords.clear()

            for (category in WordListCategory.values())
                mappedWords[category] = WordList(*words)
        }
    }
}