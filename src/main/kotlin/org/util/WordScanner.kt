package org.util

import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
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

    private const val MINIMUM_FREQUENCY = 1_000_000
    private const val MAXIMUM_FREQUENCY = 10_000_000

    private const val WORD_LENGTH = 5

    @JvmStatic
    fun main(args : Array<String>){

        val start = System.currentTimeMillis()

        // we filter words only within the bounded frequency, of the specified length and no duplicate lemma's
        val results = read().filter {
            it.frequency in MINIMUM_FREQUENCY..MAXIMUM_FREQUENCY
                    && it.word.length == WORD_LENGTH
        }

        val end = System.currentTimeMillis()
        val duration = end - start

        println("Scanned ${results.size} words in $duration ms.")

        results.forEach { println(it) }

        val serialized = JsonObject()
        val serializedWords = JsonArray()
        results.forEach { serializedWords.add(it.serialize()) }
        serialized.add("words", serializedWords)

        val out = Paths.get("data", "used_words.json").toFile()
        out.createNewFile()

        val fileWriter = FileWriter(out)

        serialized.writeTo(fileWriter, WriterConfig.PRETTY_PRINT)

        fileWriter.flush()
        fileWriter.close()
    }

    private fun read() : List<Word> {
        return Files.readAllLines(Paths.get("data", "input_words_raw.txt"))
            .map { it.split("\t") }
            .filter { lemmas.add(it[1]) }
            .map { Word(it[3], it[4].toInt())  }
    }

}