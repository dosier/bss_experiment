import ExperimentParticipant.Companion.Education
import com.eclipsesource.json.Json
import com.eclipsesource.json.WriterConfig
import util.TextUtil
import java.io.FileReader
import java.io.FileWriter
import java.lang.Exception
import java.nio.file.Paths
import java.util.*
import javax.swing.plaf.TextUI

/**
 * The [Experiment] class manages the [ExperimentGroup] selection and creates the [ExperimentSession] for the [ExperimentParticipant].
 *
 * @see groupA the [ExperimentGroup] starting with the [WordListCategory.EASY_TO_READ_WORDS]
 * @see groupB the [ExperimentGroup] starting with the [WordListCategory.HARD_TO_READ_WORDS]
 *
 * @author  Stan van der Bend
 * @since   2018-12-03
 * @version 1.0
 */
class Experiment {

    private val groupA : ExperimentGroup
    private val groupB : ExperimentGroup

    init {
        savePath.toFile().mkdirs()

        val groupADataFile = savePath.resolve(GROUP_A).toFile()
        val groupBDataFile = savePath.resolve(GROUP_B).toFile()

        groupA = if(!groupADataFile.exists())
            ExperimentGroup(GROUP_A, GROUP_SIZE, WordListCategory.EASY_TO_READ_WORDS)
        else
            ExperimentGroup.deserialize(Json.parse(FileReader(groupADataFile)).asObject())

        groupB = if(!groupBDataFile.exists())
            ExperimentGroup(GROUP_B, GROUP_SIZE, WordListCategory.HARD_TO_READ_WORDS)
        else
            ExperimentGroup.deserialize(Json.parse(FileReader(groupBDataFile)).asObject())
    }

    fun save() {
        val groupADataFile = savePath.resolve(GROUP_A).toFile()
        val groupBDataFile = savePath.resolve(GROUP_B).toFile()

        var fileWriter = FileWriter(groupADataFile)
        groupA.serialize().writeTo(fileWriter, WriterConfig.PRETTY_PRINT)
        fileWriter.flush()

        fileWriter = FileWriter(groupBDataFile)
        groupB.serialize().writeTo(fileWriter, WriterConfig.PRETTY_PRINT)
        fileWriter.flush()

        fileWriter.close()
    }

    fun buildSession() : Optional<ExperimentSession> {

        val name = promptForName()
        val age = promptForAge()
        val education = promptForEducation()

        val participant = ExperimentParticipant(name, age, education)

        val availableGroups = arrayOf(groupA, groupB).filter { it.isFull().not() }

        if(availableGroups.isEmpty()){
            println("There is no group available!")
            return Optional.empty()
        }

        val selectedGroup = availableGroups.random()

        selectedGroup.add(participant)

        println("Added participant to $selectedGroup")
        save()

        val session = ExperimentSession(selectedGroup.firstListCategory, participant, allWordLists)
        return Optional.of(session)
    }

    private fun promptForName() : String {
        println("Please enter the participant's name:")
        val enteredName = readLine()
        if(enteredName == null){
            println("Invalid name entered, try again.")
            return promptForName()
        }
        return enteredName.capitalize()
    }

    private fun promptForAge() : Int {
        println("Please enter the participant's age:")
        val enteredAge = readLine()
        if(enteredAge == null){
            println("Invalid age entered, try again.")
            return promptForAge()
        }
        val age = enteredAge.trim().toIntOrNull()
        if(age == null){
            println("Invalid age entered, please enter a number, try again.")
            return promptForAge()
        }
        if(age !in MINIMUM_AGE..MAXIMUM_AGE){
            println("Age does not meet requirements!)")
            println("The participant should be of age $MINIMUM_AGE to $MAXIMUM_AGE!")
            println()
            println("Enter -i to ignore or enter to try again!")

            val next = readLine()

            if(next != null && next.toLowerCase() == "-i")
                return age

            return promptForAge()
        }
        return age
    }

    private fun promptForEducation() : Education {
        println("Please enter the participant's education (UNIVERSITY or HBO):")
        var enteredEducation = readLine()
        if(enteredEducation == null || enteredEducation.isEmpty()){
            println("Invalid education entered, try again.")
            return promptForEducation()
        }
        enteredEducation = enteredEducation.toUpperCase().trim()

        var education : Education? = null
        try{
            education = Education.valueOf(enteredEducation)
        } catch (e : Exception){
            println("Invalid education entered!")
        }

        if(education == null) {
            val mostLikely =
                Education.values().minBy { TextUtil.calculateLevensteinDistance(it.name, enteredEducation) }!!
            println("Did you mean $mostLikely?")
            println("Enter 'Y' to continue or 'N' to re-enter.")
            val next = readLine()
            if(next != null && next.toUpperCase().trim() == "Y")
                education = mostLikely
        }

        if(education == null){
            println("Invalid education entered, try again.")
            return promptForEducation()
        }

        return education
    }

    companion object {

        const val GROUP_SIZE = 8
        const val GROUP_A = "GroupA.json"
        const val GROUP_B = "GroupB.json"

        const val MINIMUM_AGE = 18
        const val MAXIMUM_AGE = 21

        val savePath = Paths.get("data", "participants")!!

        val allWordLists = hashMapOf(
            Pair(WordListCategory.EASY_TO_READ_WORDS, WordList.EASY_LISTS),
            Pair(WordListCategory.HARD_TO_READ_WORDS, WordList.HARD_LISTS)
        )
    }
}