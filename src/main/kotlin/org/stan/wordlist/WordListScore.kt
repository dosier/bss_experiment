package org.stan.wordlist

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.GridPane
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextFlow

/**
 * A [WordListScore] handles the user input, it counts the mistakes and offers functionality to display the result.
 *
 * @see createGrid called by [ExperimentScreen] after submission of an answer by the user
 *
 * @author  Stan van der Bend
 * @since   2018-11-30
 * @version 1.0
 */
class WordListScore(listIndex : Int, answers : WordList, wordList: WordList) {

    private val numberOfWordsTested = wordList.size
    private val resultText : Text
    val mistakes : Int
    val score : Int

    init {

        var wrongAnswerCount = numberOfWordsTested

        println("input      = $wordList")
        println("answers    = $answers")
        println()

        for ((index, word) in answers.withIndex()){
            val wordIsValid = wordList.contains(word)
            val wordIsInOrder = wordList[index] == word

            if(wordIsValid && wordIsInOrder)
                wrongAnswerCount--

        }
        mistakes = wrongAnswerCount
        score = listIndex + if(mistakes > 0) 0 else 1
        resultText = Text("Results: ${numberOfWordsTested-mistakes}/$numberOfWordsTested correct")
        resultText.font = Font.font(13.0)
    }

    fun createGrid(score : Int) : GridPane {

        val grid = GridPane()

        grid.alignment = Pos.CENTER
        grid.hgap = 15.0

        val textFlow = TextFlow()
        textFlow.style = "-fx-border-style: solid; -fx-background-color: white;"
        textFlow.padding = Insets(5.0, 5.0, 5.0,5.0)

        val headerText = if(mistakes > 0)
            Text("Your end score for this category is $score")
        else Text("Your current score for this category is $score")
        headerText.font = Font.font(15.0)

        textFlow.children.addAll(headerText, resultText)

        grid.addRow(0, textFlow)
        return grid
    }

}