package org.util

import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.PrettyPrint
import com.eclipsesource.json.WriterConfig
import org.stan.wordlist.Word
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths

/**
 * TODO: add documentation
 *
 * @author  Stan van der Bend (https://www.rune-server.ee/members/StanDev/)
 * @since   2018-12-07
 * @version 1.0
 */
object WordScanner {

    private val lemmas = HashSet<String>()

    private const val MINIMUM_FREQUENCY = 6_000
    private const val MAXIMUM_FREQUENCY = 100_000_000

    private const val WORD_LENGTH = 5

    @JvmStatic
    fun main(args : Array<String>){

        val start = System.currentTimeMillis()

        // we filter words only within the bounded frequency, of the specified length and no duplicate lemma's
        val results = readWordFrequencyWebsite().filter {
            it.frequency in MINIMUM_FREQUENCY..MAXIMUM_FREQUENCY
                    && it.word.length == WORD_LENGTH
        }

        val end = System.currentTimeMillis()
        val duration = end - start

        val wordsCounts = results.size
        var wordsNeeded = 0
        var wordsInList = 3
        for (i in 1..13){
            wordsNeeded += wordsInList * 2
            wordsInList++
        }
        println("Required amount of words = $wordsNeeded")
        println("Scanned ${results.size} words in $duration ms.")

        results.forEach { println(it) }

        val serialized = JsonObject()
        val serializedWords = JsonArray()
        results.forEach { serializedWords.add(it.serialize()) }
        serialized.add("words", serializedWords)

        val out = Paths.get("data", "other_used_words.json").toFile()
        out.createNewFile()

        val fileWriter = FileWriter(out)

        serialized.writeTo(fileWriter, WriterConfig.PRETTY_PRINT)

        fileWriter.flush()
        fileWriter.close()
    }

    // this is a list of random words from websites and such
    private fun readWordFrequencyWebsite() : List<Word> {
        return Files.readAllLines(Paths.get("data", "input_words_raw.txt"))
            .map { it.split("\t") }
            .filter { lemmas.add(it[1]) }
            .map { Word(it[3], it[4].toInt())  }
    }

    // this is a list of subtitles
    private fun readWordFrequencyGithub() : List<Word> {
        return Files.readAllLines(Paths.get("data", "en_full.txt"))
            .map {
               val split = it.split(" ")
                Word(split[0], split[1].toInt())
            }

    }
}