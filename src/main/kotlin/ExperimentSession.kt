import WordListCategory.*
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
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Duration
import java.io.FileWriter
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

    private val answers = HashMap<WordListCategory, Array<WordList>>()
    private val score = HashMap<WordListCategory, Int>()

    private val dataPath = Paths.get("data")
    private val savePath = dataPath.resolve("${timeStampPattern.format(LocalDateTime.now())}_session.json")

    private val root = StackPane()
    private val timeLine = Timeline()
    private val wordLabel = Label()
    private val experimentInformation = TextArea()

    /**
     * Create a [Scene] containing the visual and interactive components of this [ExperimentSession].
     */
    fun buildScene() : Scene {
        return Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT)
    }

    init {

        dataPath.toFile().mkdirs()

        /*
         * Fill the answers and score map with a default value.
         */
        val nextCategory = startCategory.other() // this is to maintain the order in the JSON file
        answers[startCategory] = Array(wordLists[startCategory]!!.size) {WordList()}
        answers[nextCategory] = Array(wordLists[nextCategory]!!.size) {WordList()}
        score[startCategory] = 0
        score[nextCategory] = 0

        /*
         * Set the properties of the visual and interactive components of the scene.
         */
        experimentInformation.isWrapText = true
        experimentInformation.isEditable = false
        updateExperimentInformation()
        wordLabel.textAlignment = TextAlignment.CENTER
        wordLabel.isVisible = false
        updateWordLabel()

        // Create a start button that triggers the timeline
        val startButton = Button("Start")
        startButton.setOnAction {
            startButton.isVisible = false
            experimentInformation.isVisible = false
            wordLabel.isVisible = true
            timeLine.play()
        }

        // Create a left aligned pane that contains the experiment information (TextArea)
        val leftHBox = createLeftHBox()
        leftHBox.children.add(experimentInformation)
        // Create a right aligned pane that contains the start button
        val rightHBox = createRightHBox()
        rightHBox.children.add(startButton)

        /*
         * Add the sub-panes and the text-label to the main pane
         */
        root.children.addAll(leftHBox, rightHBox, wordLabel)

        /*
         * Set the alignment of the sub-panes and the text-label with respect to the main pane
         */
        StackPane.setAlignment(leftHBox, Pos.CENTER_LEFT)
        StackPane.setAlignment(rightHBox, Pos.CENTER_RIGHT)
        StackPane.setAlignment(wordLabel, Pos.CENTER)

        /*
         * Create a copy of the first presented WordList (this object is overridden at next list selection)
         */
        var wordListCopy = WordList()
        wordListCopy.addAll(wordLists[currentCategory]!![currentListIndex])


        // Define the duration of the timeline (the amount of frames, one frame has a duration of one second.
        timeLine.cycleCount = wordListCopy.size + 1
        /*
         * Define the KeyFrame:
         * - displays all words in the current list
         * - prompts the user for an answer
         * - verifies the answer and measures the result
         * - executes the appropriate behaviour to the measured result
         */
        timeLine.keyFrames.add(
            KeyFrame(
                Duration.seconds(DISPLAY_INTERVAL_IN_SECONDS),
                EventHandler {

                    // if there are still words left in the list currently being tested.
                    if(wordListCopy.isNotEmpty())
                        wordLabel.text = wordListCopy.pollFirst()
                    else {

                        wordLabel.text = ""

                        val textInputDialog = createTextInputDialog()

                        Platform.runLater {

                            val currentCategoryAnswers = answers[currentCategory]!!
                            val currentListAnswers = currentCategoryAnswers[currentListIndex]
                            val currentCategoryLists =  wordLists[currentCategory]!!
                            val wordList = currentCategoryLists[currentListIndex]

                            val answer = textInputDialog.showAndWait()

                            answer.ifPresent { input ->

                                val split = input.split(",")

                                for (word in split)
                                    currentListAnswers.add(word.trim())

                                val testResult = WordListScore(currentListAnswers, wordList)

                                val completedAllListsInCategory = currentListIndex + 1 == currentCategoryLists.size
                                val failedCurrentList = testResult.mistakes > 0

                                val currentScore = currentListIndex + if(failedCurrentList) 0 else 1
                                score[currentCategory] = currentScore

                                val popupWindow = createPopUpWindow()
                                val popupLayout = testResult.createVBox(currentScore)
                                val popupButton = if (failedCurrentList || completedAllListsInCategory)
                                    Button("Submit category score")
                                else Button("Show next list")

                                popupButton.setOnAction {

                                    popupWindow.close()

                                    if(failedCurrentList || completedAllListsInCategory){

                                        completedCategories++

                                        if(completedCategories == values().size){
                                            completeExperiment()
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

                                popupLayout.children.add(popupButton)

                                val popupScene = Scene(popupLayout, POPUP_WIDTH, POPUP_HEIGHT)
                                popupWindow.scene = popupScene
                                popupWindow.showAndWait()
                            }
                        }

                    }
                })
        )
    }

    private fun serialize() : JsonObject {
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

                val answersInList = answersInCategory[listIndex]

                if(answersInList.isEmpty())
                    continue

                val listDetails = JsonObject()

                val answerDetails = JsonArray()
                for (answer in answersInList)
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
                "After all words have been displayed, a text area will popup in which you may enter the words you can recall.\n" +
                "To pass the test, the words must also be answered in the same order as they were displayed."
    }

    private fun completeExperiment() {
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
    }

    private fun createPopUpWindow() : Stage{
        val popupWindow = Stage()
        popupWindow.initModality(Modality.APPLICATION_MODAL)
        popupWindow.title = "Prompt"
        return popupWindow
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
    private fun createTextInputDialog() : TextInputDialog {
        val textInputDialog = TextInputDialog()
        textInputDialog.title = ANSWER_DIALOG_TITLE
        textInputDialog.headerText = "Please enter all the words you are able to recall, separated by a comma!"
        textInputDialog.contentText = "Answer:"
        textInputDialog.dialogPane.lookupButton(ButtonType.CANCEL).isVisible = false
        return textInputDialog
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