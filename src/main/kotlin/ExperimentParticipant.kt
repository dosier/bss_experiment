import com.eclipsesource.json.JsonObject

/**
 * TODO: add documentation
 *
 * @author  Stan van der Bend (https://www.rune-server.ee/members/StanDev/)
 * @since   2018-12-03
 * @version 1.0
 */
class ExperimentParticipant(private val name : String, private val age : Int, private val education: Education) {

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
    }
}