import com.eclipsesource.json.JsonObject

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
class ExperimentParticipant(val name : String, private val age : Int, private val education: Education) {

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
            HBO
        }

        fun deserialize(participantData: JsonObject) : ExperimentParticipant {
            val name = participantData.getString("name", "UNDEFINED")
            val age = participantData.getInt("age", -1)
            val education = Education.valueOf(participantData.getString("education", "NONE"))
            return ExperimentParticipant(name, age, education)
        }
    }
}