import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.JsonValue

/**
 * TODO: add documentation
 *
 * @author  Stan van der Bend
 * @since   2018-12-03
 * @version 1.0
 */
class ExperimentGroup(private val identifier : String, private val size : Int, val firstListCategory : WordListCategory) {

    private val participants = HashSet<ExperimentParticipant>(size)

    fun add(participant: ExperimentParticipant) : Boolean{
        return participants.add(participant)
    }

    fun isFull() : Boolean {
        return participants.size == size
    }

    fun serialize() : JsonObject {

        val jsonObject = JsonObject()

        jsonObject.add("identifier", identifier)
        jsonObject.add("capacity", size)
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

    companion object {

        fun deserialize(groupData : JsonObject) : ExperimentGroup {

            val identifier = groupData.getString("identifier", "UNDEFINED")
            val capacity = groupData.getInt("capacity", -1)
            val firstListCategory = WordListCategory.valueOf(groupData.getString("firstListCategory", "NONE"))

            val group = ExperimentGroup(identifier, capacity, firstListCategory)

            val serializedParticipants = groupData.get("participants").asArray()

            for (serializedParticipant in serializedParticipants)
                group.add(ExperimentParticipant.deserialize(serializedParticipant.asObject()))

            return group
        }
    }

}