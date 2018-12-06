import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.WriterConfig
import java.io.FileWriter
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * TODO: add documentation
 *
 * @author  Stan van der Bend (https://www.rune-server.ee/members/StanDev/)
 * @since   2018-12-06
 * @version 1.0
 */
class ExperimentResult(participant: ExperimentParticipant, lists :HashMap<WordListCategory, Array<WordList>>, score : HashMap<WordListCategory, Int>, answers : HashMap<WordListCategory, Array<WordList>>) : JsonObject() {

    private val dataPath = Paths.get("data", "results", participant.name)!!
    private val savePath =  dataPath.resolve("${timeStampPattern.format(LocalDateTime.now())}.json")

    init {

        dataPath.toFile().mkdirs()

        val scores = JsonObject()

        for (entry in score.entries)
            scores.add(entry.key.name, entry.value)

        add("score", scores)

        val details = JsonObject()

        for (category in answers.keys) {

            val answersInCategory = answers[category]!!
            val categoryDetails = JsonObject()

            for (listIndex in 0 until answersInCategory.size) {

                val answersInList = answersInCategory[listIndex]

                if (answersInList.isEmpty())
                    continue

                val listDetails = JsonObject()

                val answerDetails = JsonArray()
                for (answer in answersInList)
                    answerDetails.add(answer)

                listDetails.add("answers", answerDetails)
                listDetails.add("list", lists[category]!![listIndex].serialize())

                categoryDetails.add("index-$listIndex", listDetails)
            }
            details.add(category.name, categoryDetails)
        }
        add("details", details)
    }

    fun save(){

        val file = savePath.toFile()
        val fileWriter = FileWriter(file)

        if(file.mkdirs())
            println("Created dirs at $savePath")
        if(file.createNewFile())
            println("Created file at $savePath")

        writeTo(fileWriter, WriterConfig.PRETTY_PRINT)

        fileWriter.flush()
        fileWriter.close()

        println("Completed session!")
    }

    companion object {

        val timeStampPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")!!
    }
}