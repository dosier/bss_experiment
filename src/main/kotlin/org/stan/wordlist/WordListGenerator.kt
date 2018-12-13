package org.stan.wordlist

import com.eclipsesource.json.Json
import com.sun.xml.internal.fastinfoset.util.StringArray
import java.io.FileReader
import java.nio.file.Paths
import java.util.*
import kotlin.collections.HashMap

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

        val availableWords = availableWordsPerCategory[category]!!

        if(availableWords.size < amount){
            println("Not enough words left!!!")
            return wordList
        }

        for (i in 0 until amount)
            wordList.add(availableWords.remove())

        return wordList
    }

    companion object {

        var availableWordsPerCategory = load()

        fun load() : HashMap<WordListCategory, WordList>{
            val jsonObject = Json.parse(FileReader(Paths.get("data", "words.json").toFile())).asObject()
            val jsonArray = jsonObject.get("words").asArray()
            val words = jsonArray.map { Word.deserialize(it.asObject()) }.map { it.word }.toTypedArray()

            val wordListOne = WordList()
            val wordListTwo = WordList()

            val iterator = words.iterator()
            while (iterator.hasNext()){
                wordListOne.add(iterator.next())
                if(iterator.hasNext())
                    wordListTwo.add(iterator.next())
            }

            val map = HashMap<WordListCategory, WordList>()
            map[WordListCategory.EASY_TO_READ_WORDS] = wordListOne
            map[WordListCategory.HARD_TO_READ_WORDS] = wordListTwo

            return map
        }
    }
}