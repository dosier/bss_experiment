package org.stan.experiment

import com.eclipsesource.json.JsonObject
import org.stan.wordlist.WordListCategory
import org.stan.wordlist.WordListScore

/**
 * The [ExperimentParticipant] is a person that will be randomly added to one of two [ExperimentGroup].
 *
 * @param name      the full name of the person
 * @param age       the age in years of this person
 * @param education the [Education] this person attends
 *
 * @author  Stan van der Bend
 * @since   2018-12-03
 * @version 1.0
 */
class ExperimentParticipant(var name : String, var age : Int, var education: Education) {

    val scores = HashMap<WordListCategory, WordListScore>()

    fun serialize() : JsonObject {
        val jsonObject = JsonObject()
        jsonObject.add("name", name)
        jsonObject.add("age", age)
        jsonObject.add("education", education.name)
        return jsonObject
    }

    companion object {

        enum class Education {
            UNIVERSITY,
            UNIVERSITY_OF_APPLIED_SCIENCES;

            fun formattedName() : String {
                return name.toLowerCase().trim().capitalize().replace("_", " ")
            }
        }

        fun deserialize(participantData: JsonObject) : ExperimentParticipant {
            val name = participantData.getString("name", "UNDEFINED")
            val age = participantData.getInt("age", -1)
            val education = Education.valueOf(participantData.getString("education", "NONE"))
            return ExperimentParticipant(name, age, education)
        }
    }
}