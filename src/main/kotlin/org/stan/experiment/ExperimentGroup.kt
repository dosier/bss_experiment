package org.stan.experiment

import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import org.stan.wordlist.WordListCategory

/**
 * The [ExperimentGroup] contains [ExperimentParticipant] instances created at [ExperimentConfiguration.buildSession].
 *
 * @param identifier        the [String] identifier of this group aka file name.
 * @param maxCapacity       the maximum capacity of [participants].
 * @param firstListCategory the first [WordListCategory] from which [WordList]s are selected in the [Experiment].
 *
 * @author  Stan van der Bend
 * @since   2018-12-03
 * @version 1.0
 */
class ExperimentGroup(private val identifier : String, private val maxCapacity : Int, val firstListCategory : WordListCategory) {

    private val participants = HashSet<ExperimentParticipant>(maxCapacity)

    fun add(participant: ExperimentParticipant) : Boolean{
        return participants.add(participant)
    }

    fun isFull() : Boolean {
        return participants.size == maxCapacity
    }

    fun serialize() : JsonObject {

        val jsonObject = JsonObject()

        jsonObject.add("identifier", identifier)
        jsonObject.add("maxCapacity", maxCapacity)
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
            val capacity = groupData.getInt("maxCapacity", -1)
            val firstListCategory = WordListCategory.valueOf(groupData.getString("firstListCategory", "NONE"))

            val group = ExperimentGroup(identifier, capacity, firstListCategory)

            val serializedParticipants = groupData.get("participants").asArray()

            for (serializedParticipant in serializedParticipants)
                group.add(ExperimentParticipant.deserialize(serializedParticipant.asObject()))

            return group
        }
    }

}