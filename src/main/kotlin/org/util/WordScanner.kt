package org.util

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
                    && lemmas.remove(it.lemma)
        }

        val end = System.currentTimeMillis()
        val duration = end - start

        println("Scanned ${results.size} words in $duration ms.")

        results.forEach { println(it) }
    }

    private fun read() : List<Word> {
        return Files.readAllLines(Paths.get("data", "input_words_raw.txt")).map {

            val split = it.split("\t")
            val rank = split[0].toInt()
            val lemma = split[1]
            val pos = split[2][0]
            val word = split[3]
            val frequency = split[4].toInt()

            lemmas.add(lemma)

            Word(rank, lemma, pos, word, frequency)
        }
    }

    class Word(val rank : Int, val lemma : String, val pos : Char, val word : String, val frequency : Int){

        override fun toString(): String {
            return "Word(rank=$rank, lemma='$lemma', pos=$pos, word='$word', frequency=$frequency)"
        }
    }

}