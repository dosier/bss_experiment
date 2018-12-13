package org.stan.experiment

import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.Stage


/**
 * TODO: add documentation
 *
 * @author  Stan van der Bend (https://www.rune-server.ee/members/StanDev/)
 * @since   2018-12-13
 * @version 1.0
 */
abstract class ExperimentAnswerStage(private val inputFieldCount : Int, private val isDynamic : Boolean = false) : Stage() {

    private val layout = VBox()

    private val addRowButton = Button("+")
    private val submitButton = Button("Submit")

    private val grid = GridPane()

    private val buttonRow = HBox()

    var rowIndex = 0

    init {

        addRowButton.onMouseClicked = EventHandler{
            addRow()
            scene.window.sizeToScene()
        }

        submitButton.onMouseClicked = EventHandler {
            val alert = Alert(Alert.AlertType.CONFIRMATION)
            alert.title = "Confirmation"
            alert.headerText = "This will submit your answers"
            alert.contentText = "Are you sure you wish to continue?"
            alert.initOwner(scene.window)

            val option = alert.showAndWait()
            option.ifPresent { type ->
                if(type == ButtonType.OK) {
                    onSubmit(grid.children
                        .filter { field -> field is TextField }
                        .map { field -> field as TextField }
                        .map { textField -> textField.text.trim().toLowerCase() }
                        .filter { answer -> answer.isNotEmpty() }
                        .toTypedArray())
                }
            }
        }

        val clearButton = Button("Reset")
        clearButton.onMouseClicked = EventHandler {
            val alert = Alert(Alert.AlertType.CONFIRMATION)
            alert.title = "Confirmation"
            alert.headerText = "This will clear all fields"
            alert.contentText = "Are you sure you wish to continue?"
            alert.initOwner(scene.window)

            val option = alert.showAndWait()
            option.ifPresent { type ->
                if(type == ButtonType.OK) {
                    reset()
                    scene.window.sizeToScene()
                }
            }
        }

        grid.hgap = 10.0
        grid.vgap = 10.0
        grid.padding = Insets(20.0, 0.0, 10.0, 20.0)

        buttonRow.spacing = 15.0
        buttonRow.padding = Insets(0.0, 20.0, 10.0, 10.0)
        buttonRow.alignment = Pos.BOTTOM_RIGHT
        buttonRow.children.addAll(clearButton, submitButton)

        layout.children.add(createHeaderNode())
        layout.children.add(grid)
        layout.children.add(buttonRow)

        for (i in 0 until inputFieldCount)
            addRow()

        scene = Scene(layout)
        isFullScreen = false
    }

    private fun reset(){
        rowIndex = 0
        grid.children.clear()
        for (i in 0 until inputFieldCount)
            addRow()

    }

    private fun createHeaderNode(): TextFlow {
        val flow = TextFlow()

        flow.maxWidth = java.lang.Double.MAX_VALUE
        flow.style = "-fx-border-style: solid; -fx-background-color: white;"

        flow.padding = Insets(5.0, 5.0, 5.0,5.0)
        val textHeader = Text("Please enter the words you remember in the fields below: \n\n")
        textHeader.font = Font.font ("Verdana", 15.0)
        textHeader.fill = Color.BLACK

        val textDescription = Text(
            "You can press the '+' button to add another field. \n" +
                 "Leaving fields empty will not affect your final answer.")
        textDescription.font = Font.font ("Verdana", 13.0)
        textDescription.fill = Color.BLACK

        flow.children.addAll(textHeader, textDescription)
        return flow
    }


    private fun addRow() {

        val answerField = TextField()

        if(isDynamic)
            grid.children.remove(addRowButton)

        grid.add(Label("Answer ${rowIndex+1}"), 0, rowIndex)
        grid.add(answerField, 1, rowIndex)

        if(isDynamic) {
            grid.add(addRowButton, 2, rowIndex)
            GridPane.setHalignment(addRowButton, HPos.RIGHT)
        }

        updateBindings()

        rowIndex++
    }

    private fun updateBindings() {
        submitButton.disableProperty().bind(emptyTextFieldsBinding())
    }

    private fun emptyTextFieldsBinding(): BooleanBinding {
        var bind = SimpleBooleanProperty(false).not()

        for (text in grid.children.filter { it is TextField }.map { it as TextField })
            bind = bind.and(text.textProperty().isEmpty)

        return bind
    }

    abstract fun onSubmit(answers: Array<String>)
}