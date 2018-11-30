import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.WriterConfig
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Duration
import java.io.FileWriter
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

/**
 * A [ExperimentSession] is the class handling the behaviour of the experiment.
 *
 * @param startCategory the initial [WordListCategory] from which lists are retrieved.
 * @param wordLists     a [HashMap] containing every [WordList] for both [WordListCategory].
 *
 * @see WordListCategory.EASY_TO_READ_WORDS -> [WordList.EASY_LISTS]
 * @see WordListCategory.HARD_TO_READ_WORDS -> [WordList.HARD_LISTS]
 *
 * @author  Stan van der Bend
 * @since   2018-11-29
 * @version 1.0
 */
class ExperimentSession(startCategory: WordListCategory, private val wordLists: HashMap<WordListCategory, Array<WordList>>)  {

    private var completedCategories = 0

    private var currentCategory = startCategory
    private var currentListIndex = 0

    private val answers = HashMap<WordListCategory, Array<LinkedList<String>>>()
    private val score = HashMap<WordListCategory, Int>()

    private val savePath = Paths.get("data", "${timeStampPattern.format(LocalDateTime.now())}_session.json")

    private val root = StackPane()
    private val timeLine = Timeline()
    private val wordLabel = Label()
    private val experimentInformation = TextArea()

    init {

        answers[WordListCategory.EASY_TO_READ_WORDS] = Array(wordLists[WordListCategory.EASY_TO_READ_WORDS]!!.size) {LinkedList<String>()}
        answers[WordListCategory.HARD_TO_READ_WORDS] = Array(wordLists[WordListCategory.HARD_TO_READ_WORDS]!!.size) {LinkedList<String>()}
        score[WordListCategory.EASY_TO_READ_WORDS] = 0
        score[WordListCategory.HARD_TO_READ_WORDS] = 0

        val leftHBox = createLeftHBox()
        updateExperimentInformation()
        experimentInformation.isWrapText = true
        experimentInformation.isEditable = false
        leftHBox.children.add(experimentInformation)

        val rightHBox = createRightHBox()
        val startButton = Button("Start")
        startButton.setOnAction {
            startButton.isVisible = false
            experimentInformation.isVisible = false
            wordLabel.isVisible = true
            timeLine.play()
        }
        rightHBox.children.add(startButton)

        updateWordLabel()
        wordLabel.textAlignment = TextAlignment.CENTER
        wordLabel.isVisible = false

        root.children.add(leftHBox)
        root.children.add(rightHBox)
        root.children.add(wordLabel)

        StackPane.setAlignment(leftHBox, Pos.CENTER_LEFT)
        StackPane.setAlignment(rightHBox, Pos.CENTER_RIGHT)
        StackPane.setAlignment(wordLabel, Pos.CENTER)

        var wordListCopy = WordList()
        wordListCopy.addAll(wordLists[currentCategory]!![currentListIndex])

        timeLine.cycleCount = wordListCopy.size + 1
        timeLine.keyFrames.add(
            KeyFrame(
                Duration.seconds(DISPLAY_INTERVAL_IN_SECONDS),
                EventHandler {

                    if(wordListCopy.isNotEmpty())
                        wordLabel.text = wordListCopy.pollFirst()
                    else {
                        wordLabel.text = ""

                        val textInputDialog = TextInputDialog()
                        textInputDialog.title = ANSWER_DIALOG_TITLE
                        textInputDialog.headerText = "Please enter all the words you are able to recall, separated by a comma!"
                        textInputDialog.contentText = "Answer:"
                        textInputDialog.dialogPane.lookupButton(ButtonType.CANCEL).isVisible = false

                        Platform.runLater {
                            val answer = textInputDialog.showAndWait()

                            answer.ifPresent { input ->

                                val split = input.split(",")

                                for (word in split)
                                    answers[currentCategory]!![currentListIndex].add(word.trim())

                                val wordList = wordLists[currentCategory]!![currentListIndex]

                                var wrongOrderCount = 0
                                var wrongAnswerCount = wordList.size

                                println("input      = $wordList")
                                println("answers    = ${answers[currentCategory]!![currentListIndex]}")
                                println()

                                for ((index, word) in answers[currentCategory]!![currentListIndex].withIndex()){
                                    val wordIsValid = wordList.contains(word)
                                    val wordIsInOrder = wordList[index] == word

                                    if(wordIsValid && wordIsInOrder)
                                        wrongAnswerCount--
                                    else {
                                        if(wordIsValid && !wordIsInOrder) wrongOrderCount++
                                    }
                                }
                                val mistakes = wrongAnswerCount + wrongOrderCount
                                val failed = mistakes > 0
                                val completedCategory = currentListIndex + 1 == wordLists[currentCategory]!!.size

                                val popupWindow = Stage()
                                popupWindow.initModality(Modality.APPLICATION_MODAL)
                                popupWindow.title = "Prompt"

                                score[currentCategory] = currentListIndex + if(failed) 0 else 1

                                val currentScore = score[currentCategory]

                                val header = if(failed)
                                    Label("Your end score for this category is $currentScore")
                                else
                                    Label("Your current score for this category is $currentScore")

                                val layout = VBox(10.0)
                                layout.padding = Insets(15.0, 12.0, 0.0, 12.0)
                                layout.alignment = Pos.TOP_CENTER
                                layout.children.addAll(header,
                                    Label("Wrong answers: $wrongAnswerCount"),
                                    Label("Wrong ordered answers: $wrongOrderCount"))

                                val popupButton = if (failed || completedCategory)
                                    Button("Submit category score")
                                else
                                    Button("Show next list")

                                popupButton.setOnAction {

                                    popupWindow.close()

                                    if(failed || completedCategory){
                                        completedCategories++

                                        if(completedCategories == WordListCategory.values().size){
                                            wordLabel.text = "You completed the experiment, thank you!"
                                            wordLabel.prefWidth = 700.0
                                            wordLabel.prefHeight = 60.0
                                            wordLabel.textFill = Color.BLACK

                                            val serialized = serialize()
                                            val file = savePath.toFile()
                                            val fileWriter = FileWriter(file)

                                            if(file.mkdirs())
                                                println("Created dirs at $savePath")
                                            if(file.createNewFile())
                                                println("Created file at $savePath")

                                            serialized.writeTo(fileWriter, WriterConfig.PRETTY_PRINT)

                                            fileWriter.flush()
                                            fileWriter.close()

                                            println("Completed session!")
                                            return@setOnAction
                                        }

                                        currentCategory = currentCategory.other()
                                        currentListIndex = 0

                                        updateWordLabel()
                                        updateExperimentInformation()

                                        startButton.isVisible = true
                                        experimentInformation.isVisible = true
                                        wordLabel.isVisible = false

                                    } else {

                                        currentListIndex++
                                        timeLine.playFromStart()
                                    }

                                    wordListCopy = WordList()
                                    wordListCopy.addAll(wordLists[currentCategory]!![currentListIndex])
                                }

                                layout.children.add(popupButton)

                                val popupScene = Scene(layout, POPUP_WIDTH, POPUP_HEIGHT)
                                popupWindow.scene = popupScene
                                popupWindow.showAndWait()
                            }
                        }

                    }
                })
        )
    }

    fun build() : Scene {
        return Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT)
    }

    fun serialize() : JsonObject {
        val sessionSerialized = JsonObject()

        val scores = JsonObject()
        for (entry in score.entries)
            scores.add(entry.key.name, entry.value)

        sessionSerialized.add("score", scores)

        val details = JsonObject()

        for (category in answers.keys){

            val answersInCategory = answers[category]!!
            val categoryDetails = JsonObject()

            for(listIndex in 0 until answersInCategory.size) {

                val listDetails = JsonObject()

                val answerDetails = JsonArray()
                for (answer in answersInCategory[listIndex])
                    answerDetails.add(answer)

                listDetails.add("answers", answerDetails)
                listDetails.add("list", wordLists[category]!![listIndex].serialize())

                categoryDetails.add("index-$listIndex", listDetails)
            }
            details.add(category.name, categoryDetails)
        }
        sessionSerialized.add("details", details)
        return sessionSerialized
    }
    private fun updateWordLabel(){
        wordLabel.font = currentCategory.font
        wordLabel.textFill = currentCategory.color
    }

    private fun updateExperimentInformation(){
        experimentInformation.text = "Experiment details:\n\n" +
                "Your current list-category is $currentCategory\n\n" +
                "When you press start, each word in the list is displayed with an interval of 1 second.\n" +
                "After all words have been displayed, a text area will popup in which you may enter the words u can recall.\n" +
                "To pass the test, the words must also be answered in the same order as they were displayed."
    }

    private fun createLeftHBox() : HBox {
        val hBox = HBox()
        hBox.alignment = Pos.CENTER_LEFT
        hBox.maxHeight = 500.0
        hBox.maxWidth = 500.0
        hBox.padding = Insets(15.0, 12.0, 0.0, 12.0)
        hBox.spacing = 10.0
        return hBox
    }
    private fun createRightHBox() : HBox {
        val hBox = HBox()
        hBox.alignment = Pos.CENTER
        hBox.maxHeight = 500.0
        hBox.maxWidth = 500.0
        hBox.padding = Insets(15.0, 12.0, 0.0, 12.0)
        hBox.spacing = 10.0
        return hBox
    }
    companion object {

        const val SCREEN_WIDTH  = 1000.0
        const val SCREEN_HEIGHT = 600.0

        const val POPUP_WIDTH = 350.0
        const val POPUP_HEIGHT = 250.0

        const val DISPLAY_INTERVAL_IN_SECONDS = 1.0

        const val ANSWER_DIALOG_TITLE = "Answer Dialog"
        val timeStampPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")!!
    }
}