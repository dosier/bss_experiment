package org.stan.experiment

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.Background
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Duration
import kotlin.collections.HashMap
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import org.stan.experiment.ExperimentParticipant.Companion.Education.*
import org.stan.wordlist.WordList
import org.stan.wordlist.WordListCategory
import org.stan.wordlist.WordListScore
import java.util.concurrent.Callable


/**
 * A [Experiment] is the class handling the behaviour of the experiment.
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
class Experiment(startCategory: WordListCategory, private val participant: ExperimentParticipant, private val wordLists: HashMap<WordListCategory, Array<WordList>>)  {

    private var completedCategories = 0

    private var currentCategory = startCategory
    private var currentListIndex = 0

    private val answers = HashMap<WordListCategory, Array<WordList>>()
    private val score = HashMap<WordListCategory, Int>()

    private val root = StackPane()
    private val timeLine = Timeline()
    private val wordLabel = Label()
    private val experimentDetails = TextFlow()
    private val participantDetails = TextFlow()
    /**
     * Create a [Scene] containing the visual and interactive components of this [Experiment].
     */
    fun buildScene() : Scene {
        return Scene(root,
            SCREEN_WIDTH,
            SCREEN_HEIGHT
        )
    }

    init {
        wordLabel.styleClass.add("normal")
        /*
         * Fill the answers and score map with a default value.
         */
        val nextCategory = startCategory.other() // this is to maintain the order in the JSON file
        answers[startCategory] = Array(wordLists[startCategory]!!.size) { WordList() }
        answers[nextCategory] = Array(wordLists[nextCategory]!!.size) { WordList() }
        score[startCategory] = 0
        score[nextCategory] = 0

        /*
         * Set the properties of the visual and interactive components of the scene.
         */
        updateParticipantDetails()
        updateExperimentInformation()
        wordLabel.textAlignment = TextAlignment.CENTER
        wordLabel.isVisible = false
        updateWordLabel()

        val upperPane = createStackPane(experimentDetails)
        val lowerPane = createStackPane(participantDetails)
        val leftSplitPlane = createLeftSplitPane(upperPane, lowerPane)
        val splitPane = createRightSplitPlane()

        // Create a start button that triggers the timeline
        val startButton = Button("Start")
        startButton.setOnAction {
            startButton.isVisible = false
            leftSplitPlane.isVisible = false
            splitPane.isVisible = false
            wordLabel.isVisible = true
            timeLine.play()
        }


        // Create a right aligned pane that contains the start button
        val rightHBox = createRightHBox()
        rightHBox.children.add(startButton)


        splitPane.items.addAll(leftSplitPlane, rightHBox)

        /*
         * Add the sub-panes and the text-label to the main pane
         */
        root.children.addAll(splitPane, wordLabel)

        /*
         * Set the alignment of the sub-panes and the text-label with respect to the main pane
         */
        StackPane.setAlignment(leftSplitPlane, Pos.CENTER_LEFT)
        StackPane.setAlignment(rightHBox, Pos.CENTER_RIGHT)
        StackPane.setAlignment(wordLabel, Pos.CENTER)

        /*
         * Create a copy of the first presented org.stan.experiment.wordlist.WordList (this object is overridden at next list selection)
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

                                val testResult =
                                    WordListScore(
                                        currentListIndex,
                                        currentListAnswers,
                                        wordList
                                    )
                                val failedCurrentList = testResult.mistakes > 0
                                val currentScore = testResult.score

                                participant.scores[currentCategory] = testResult
                                score[currentCategory] = currentScore

                                val completedAllListsInCategory = currentListIndex + 1 == currentCategoryLists.size

                                val popupWindow = createPopUpWindow()
                                val popupLayout = testResult.createVBox(currentScore)
                                val popupButton = if (failedCurrentList || completedAllListsInCategory)
                                    Button("Submit category score")
                                else
                                    Button("Show next list")

                                popupButton.setOnAction {

                                    popupWindow.close()

                                    if(failedCurrentList || completedAllListsInCategory){

                                        completedCategories++

                                        if(completedCategories == WordListCategory.values().size){
                                            completeExperiment()
                                            return@setOnAction
                                        }

                                        currentCategory = currentCategory.other()
                                        currentListIndex = 0

                                        updateWordLabel()
                                        updateExperimentInformation()
                                        updateParticipantDetails()

                                        startButton.isVisible = true
                                        leftSplitPlane.isVisible = true
                                        splitPane.isVisible = true
                                        wordLabel.isVisible = false

                                    } else {
                                        currentListIndex++
                                        timeLine.playFromStart()
                                    }

                                    wordListCopy = WordList()
                                    wordListCopy.addAll(wordLists[currentCategory]!![currentListIndex])
                                }

                                popupLayout.children.add(popupButton)

                                val popupScene = Scene(popupLayout,
                                    POPUP_WIDTH,
                                    POPUP_HEIGHT
                                )
                                popupWindow.scene = popupScene
                                popupWindow.showAndWait()
                            }
                        }

                    }
                })
        )
    }
    private fun completeExperiment() {
        wordLabel.text = "You completed the experiment, thank you!"
        wordLabel.prefWidth = 700.0
        wordLabel.prefHeight = 60.0
        wordLabel.textFill = Color.BLACK

        ExperimentResult(participant, wordLists, score, answers).save()
    }

    private fun updateWordLabel(){
        wordLabel.font = currentCategory.font
        wordLabel.textFill = currentCategory.color
    }

    private fun updateExperimentInformation() {

        val textHeader = Text("Experiment details: \n\n")
        textHeader.font = Font.font ("Verdana", 30.0)
        textHeader.fill = Color.WHITE

        val textCategory = Text("Your current list-category is: \n")
        textCategory.font = Font.font ("Verdana", 15.0)
        textCategory.fill = Color.WHITE

        val actualCategory = Text("$currentCategory \n\n")
        actualCategory.style = "-fx-fill: #4F8A10;-fx-font-weight:bold;"
        actualCategory.font = Font.font("Verdana", 17.0)

        val textBody = Text()
        textBody.font = Font.font ("Verdana", 15.0)
        textBody.fill = Color.WHITE
        if(completedCategories == 0) {
            textBody.text =
                    "When you press start, each word in the list is displayed with an interval of 1 second.\n" +
                    "After all words have been displayed, a text area will popup in which you may enter the words you can recall.\n" +
                    "To pass the test, the words must also be answered in the same order as they were displayed.\n\n" +
                    "First you will do a test round to get familiar with the mechanics."
        } else
            textBody.text = "You have completed the first category test, press start again to start with the second category!\n\n"

        experimentDetails.children.clear()
        experimentDetails.children.addAll(textHeader, textCategory, actualCategory, textBody)
    }

    private fun updateParticipantDetails()  {

        participantDetails.children.clear()

        val textHeader = Text("Participant details: \n\n")
        textHeader.font = Font.font ("Verdana", 30.0)
        textHeader.fill = Color.WHITE

        participantDetails.children.add(textHeader)

        if(completedCategories == 0) {

            val hBox = VBox(10.0)
            val nameField = TextField()
            nameField.promptText = "Enter your name"
            nameField.textProperty().addListener {_, _, newValue ->
                run {
                    if (newValue != null)
                        participant.name = newValue
                }
            }
            val ageField = TextField()
            ageField.promptText = "Enter your age"
            ageField.textProperty().addListener {_, _, newValue ->
                run {
                    val entered = newValue.toIntOrNull()
                    if (entered != null)
                        participant.age = entered

                }
            }
            val options = arrayOf(UNIVERSITY.formattedName(), UNIVERSITY_OF_APPLIED_SCIENCES.formattedName())
            val choiceBox = ChoiceBox<String>(FXCollections.observableArrayList(*options))
            choiceBox.prefHeight = 15.0
            choiceBox.value = options[0]
            choiceBox.tooltip = Tooltip()
            choiceBox.tooltip.text = "Select your education"
            choiceBox.selectionModel.selectedItemProperty()
                .addListener { _, _, newValue ->  participant.education = ExperimentParticipant.Companion.Education.valueOf(newValue.replace(" ", "_").toUpperCase()) }

            hBox.children.addAll(nameField, ageField, choiceBox)
            participantDetails.children.add(hBox)
        } else {
            val textDetails = Text(
                "Name: ${participant.name}\n"
                        + "Age: ${participant.age}\n"
                        + "Education: ${participant.education.formattedName()}")
            textDetails.font = Font.font ("Verdana", 15.0)
            textDetails.fill = Color.WHITE
            participantDetails.children.add(textDetails)
        }
    }

    private fun createPopUpWindow() : Stage{
        val popupWindow = Stage()
        popupWindow.initModality(Modality.APPLICATION_MODAL)
        popupWindow.title = "Answer prompt"
        popupWindow.maxHeight = 150.0
        return popupWindow
    }
    private fun createRightSplitPlane() : SplitPane {
        val splitPlane = SplitPane()

        splitPlane.background = Background.EMPTY
        splitPlane.orientation = Orientation.HORIZONTAL

        return splitPlane
    }
    private fun createLeftSplitPane(upperPane : StackPane, lowerPane: StackPane) : SplitPane {
        val splitPlane = SplitPane()

        splitPlane.background = Background.EMPTY
        splitPlane.orientation = Orientation.VERTICAL
        splitPlane.items.addAll(upperPane, lowerPane)
        splitPlane.setDividerPositions(0.6)

        //Constrain max size of left component:
        upperPane.maxHeightProperty().bind(splitPlane.heightProperty().multiply(0.6))
        lowerPane.maxHeightProperty().bind(splitPlane.heightProperty().multiply(0.4))
        splitPlane.maxWidthProperty().bind(root.widthProperty().multiply(0.5))
        return splitPlane
    }

    private fun createStackPane(node : Node) : StackPane{
        val stackPane = StackPane(node)
        stackPane.background = Background.EMPTY
        stackPane.alignment = Pos.CENTER_LEFT
        stackPane.maxHeight = 500.0
        stackPane.maxWidth = 500.0
        stackPane.padding = Insets(15.0, 12.0, 0.0, 12.0)
        return stackPane
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
        textInputDialog.dialogPane.lookupButton(ButtonType.OK).disableProperty().bind(
            Bindings.createBooleanBinding(Callable {
                textInputDialog.editor.text.trim().isEmpty()
            }, textInputDialog.editor.textProperty()))

        textInputDialog.editor.prefHeight(200.0)
        return textInputDialog
    }

    companion object {

        const val SCREEN_WIDTH  = 1000.0
        const val SCREEN_HEIGHT = 600.0

        const val POPUP_WIDTH = 350.0
        const val POPUP_HEIGHT = 250.0

        const val DISPLAY_INTERVAL_IN_SECONDS = 1.0

        const val ANSWER_DIALOG_TITLE = "Answer Dialog"

    }
}