import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject

/**
 * TODO: add documentation
 *
 * @author  Stan van der Bend (https://www.rune-server.ee/members/StanDev/)
 * @since   2018-12-03
 * @version 1.0
 */
class ExperimentGroup(private val size : Int, val firstListCategory : WordListCategory) {

    private val participants = HashSet<ExperimentParticipant>(size)

    fun add(participant: ExperimentParticipant) : Boolean{
        return participants.add(participant)
    }

    fun isFull() : Boolean {
        return participants.size == size
    }

    fun serialize() : JsonObject {

        val jsonObject = JsonObject()

        jsonObject.add("firstListCategory", firstListCategory.name)

        val serializedParticipants = JsonArray()

        for (participant in participants)
            serializedParticipants.add(participant.serialize())

        jsonObject.add("participants", serializedParticipants)

        return jsonObject
    }

    override fun toString(): String {
        return "ExperimentGroup(firstListCategory=$firstListCategory)"
    }

}