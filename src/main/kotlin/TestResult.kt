import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.VBox

/**
 * A [TestResult] handles the user input, it counts the mistakes and offers functionality to display the result.
 *
 * @see createVBox called by [ExperimentScreen] after submission of an answer by the user
 *
 * @author  Stan van der Bend
 * @since   2018-11-30
 * @version 1.0
 */
class TestResult(answers : WordList, wordList: WordList) {

    private val numberOfWordsTested = wordList.size
    private val resultLabel : Label
    val mistakes : Int

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
        resultLabel = Label("Results: ${numberOfWordsTested-mistakes}/$numberOfWordsTested correct")
    }

    fun createVBox(score : Int) : VBox {

        val header = if(mistakes > 0)
            Label("Your end score for this category is $score")
        else
            Label("Your current score for this category is $score")

        val layout = VBox(10.0)
        layout.padding = Insets(15.0, 12.0, 0.0, 12.0)
        layout.alignment = Pos.TOP_CENTER
        layout.prefHeight = 300.0
        layout.children.addAll(header, resultLabel)

        return layout
    }

}