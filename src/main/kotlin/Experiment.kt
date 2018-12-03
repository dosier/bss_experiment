
import ExperimentParticipant.Companion.Education
import java.util.*

/**
 * TODO: add documentation
 *
 * @author  Stan van der Bend (https://www.rune-server.ee/members/StanDev/)
 * @since   2018-12-03
 * @version 1.0
 */
class Experiment {

    private val groupA = ExperimentGroup(GROUP_SIZE, WordListCategory.EASY_TO_READ_WORDS)
    private val groupB = ExperimentGroup(GROUP_SIZE, WordListCategory.HARD_TO_READ_WORDS)

    fun createSession() : Optional<ExperimentSession> {

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

        println("Added participant to $selectedGroup]")

        val session = ExperimentSession(selectedGroup.firstListCategory, allWordLists)

        return Optional.of(session)
    }


    private fun promptForName() : String {
        println("Please enter the participant's name:")
        val enteredName = readLine()
        if(enteredName == null){
            println("Invalid name entered, try again.")
            return promptForName()
        }
        return enteredName
    }

    private fun promptForAge() : Int {
        println("Please enter the participant's age:")
        val enteredAge = readLine()
        if(enteredAge == null){
            println("Invalid age entered, try again.")
            return promptForAge()
        }
        return enteredAge.toInt()
    }

    private fun promptForEducation() : Education {
        println("Please enter the participant's education (UNIVERSITY or HBO):")
        val enteredEducation = readLine()
        if(enteredEducation == null){
            println("Invalid education entered, try again.")
            return promptForEducation()
        }
        return Education.valueOf(enteredEducation.toUpperCase().trim())
    }

    companion object {
        const val GROUP_SIZE = 8

        val allWordLists = hashMapOf(
            Pair(WordListCategory.EASY_TO_READ_WORDS, WordList.EASY_LISTS),
            Pair(WordListCategory.HARD_TO_READ_WORDS, WordList.HARD_LISTS)
        )

    }
}