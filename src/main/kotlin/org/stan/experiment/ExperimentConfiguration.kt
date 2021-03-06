package org.stan.experiment

import org.stan.experiment.ExperimentParticipant.Companion.Education
import com.eclipsesource.json.Json
import com.eclipsesource.json.WriterConfig
import org.stan.wordlist.WordList
import org.stan.wordlist.WordListCategory
import org.stan.wordlist.WordListCategory.*
import org.stan.wordlist.WordListGenerator
import org.util.TextUtil
import java.io.FileReader
import java.io.FileWriter
import java.lang.Exception
import java.nio.file.Paths
import java.util.*
import kotlin.collections.ArrayList

/**
 * The [ExperimentConfiguration] class manages the [ExperimentGroup] selection and creates the [Experiment] for the [ExperimentParticipant].
 *
 * @see groupA the [ExperimentGroup] starting with the [WordListCategory.EASY_TO_READ_WORDS]
 * @see groupB the [ExperimentGroup] starting with the [WordListCategory.HARD_TO_READ_WORDS]
 *
 * @author  Stan van der Bend
 * @since   2018-12-03
 * @version 1.0
 */
class ExperimentConfiguration {

    private val groupA : ExperimentGroup
    private val groupB : ExperimentGroup

    init {

        WordListGenerator.load()

        savePath.toFile().mkdirs()

        val groupADataFile = savePath.resolve(GROUP_A).toFile()
        val groupBDataFile = savePath.resolve(GROUP_B).toFile()

        groupA = if(!groupADataFile.exists())
            ExperimentGroup(
                GROUP_A,
                GROUP_SIZE,
                EASY_TO_READ_WORDS
            )
        else
            ExperimentGroup.deserialize(Json.parse(FileReader(groupADataFile)).asObject())

        groupB = if(!groupBDataFile.exists())
            ExperimentGroup(
                GROUP_B,
                GROUP_SIZE,
                HARD_TO_READ_WORDS
            )
        else
            ExperimentGroup.deserialize(Json.parse(FileReader(groupBDataFile)).asObject())
    }

    fun buildSession() : Optional<Experiment> {

//        val name = promptForName()
//        val age = promptForAge()
//        val education = promptForEducation()
        val name = ""
        val age = -1
        val education = Education.UNIVERSITY

        val participant = ExperimentParticipant(name, age, education)

        val availableGroups = arrayOf(groupA, groupB).filter { it.isFull().not() }

        if(availableGroups.isEmpty()){
            println("There is no group available!")
            return Optional.empty()
        }

        val selectedGroup = availableGroups.random()

        selectedGroup.add(participant)

        println("Added participant to $selectedGroup")

        val session = Experiment(
            selectedGroup.firstListCategory,
            this,
            participant,
            allWordLists
        )
        return Optional.of(session)
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

        private const val LISTS_PER_CATEGORY = 13
        const val INITIAL_WORDS_IN_LIST = 3

        const val GROUP_SIZE = 8
        const val GROUP_A = "GroupA.json"
        const val GROUP_B = "GroupB.json"

        const val MINIMUM_AGE = 18
        const val MAXIMUM_AGE = 21

        val savePath = Paths.get("data", "participants")!!

        val allWordLists = hashMapOf(
            Pair(TEST_CATEGORY, WordList.TEST_LISTS),
            Pair(EASY_TO_READ_WORDS, buildLists(EASY_TO_READ_WORDS)),
            Pair(HARD_TO_READ_WORDS, buildLists(HARD_TO_READ_WORDS))
        )

        private fun buildLists(category: WordListCategory) : Array<WordList>{
            val list = ArrayList<WordList>()

            for (i in 0 until LISTS_PER_CATEGORY)
                list.add(WordListGenerator(category, INITIAL_WORDS_IN_LIST + i).generate())

            return list.toTypedArray()
        }
    }
}