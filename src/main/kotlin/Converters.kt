package ns.dslab

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToLong

object Converters {
    fun convertFile(path: String, targetPath: String) {
        println("Converting $path")

        val sourceFile = File(path)
        val outFile = File(targetPath)
        outFile.createNewFile()

        val fileData = sourceFile.readText()
        val jsonData = Json.decodeFromString<JsonArray>(fileData)

        val phonemeData: MutableList<List<Triple<String, Long, Long>>> = mutableListOf()

        for (element in jsonData) {
            val currentSequenceData: MutableList<Triple<String, Long, Long>> = mutableListOf()
            val baseOffset = baseOffsetToLabOffset(element.jsonObject["offset"]!!.jsonPrimitive.content)
            var totalStartOffset = 0L

            for ((index, phoneme) in element.jsonObject["ph_seq"]!!.jsonPrimitive.content.split(" ").withIndex()) {
                val duration = durationToLabOffsetPrecision(element.jsonObject["ph_dur"]!!.jsonPrimitive.content.split(" ")[index])
                currentSequenceData.add(Triple(phoneme, baseOffset + totalStartOffset, baseOffset + totalStartOffset + duration))
                totalStartOffset += duration
            }

            phonemeData.add(currentSequenceData)
        }

        // println(phonemeData)

        val labDataStringBuilder = StringBuilder()
        labDataStringBuilder.append("0")
        var sequenceIndex = 0
        while (sequenceIndex < phonemeData.size) {
            val sequence = phonemeData[sequenceIndex]
            sequenceIndex++

            for (phoneme in sequence) {
                if (sequence.indexOf(phoneme) == 0) {
                    labDataStringBuilder.append(" ${phoneme.third} ${phoneme.first}\n")
                    continue
                }

                if (sequence.indexOf(phoneme) == sequence.indexOf(sequence.last())) {
                    labDataStringBuilder.append(phoneme.second)
                    continue
                }

                labDataStringBuilder.append("${phoneme.second} ${phoneme.third} ${phoneme.first}\n")
            }
        }

        labDataStringBuilder.append(phonemeData.last().last().let { " ${it.third} ${it.first}" })

        //println(labDataStringBuilder)

        outFile.writeText(labDataStringBuilder.toString())
    }

    private fun durationToLabOffsetPrecision(durationString: String): Long {
        return (durationString.toDouble() * 10_000_000.0).roundToLong()
    }

    private fun baseOffsetToLabOffset(baseOffset: String): Long {
        val split = baseOffset.split(".")

        if (split.size == 1)
            return split[0].toLong()

        assert(split.size == 2)

        val seconds = split[0].toLong() * 10_000_000L
        val other = if (split[1].length >= 7) split[1].substring(0..<7).toLong() else normalizeNumberLength(split[1])

        return seconds + other
    }

    private fun normalizeNumberLength(numberString: String): Long {
        val initialLength = numberString.length
        assert(initialLength < 7)
        val multiplier = round(10.0.pow(7 - initialLength)).toLong()
        return (numberString.toLong() * multiplier)
    }

    fun convertDirectory(path: String) {
        var totalConverted = 0

        val outputPath = "$path/converted"
        File(outputPath).mkdir()

        for (file in File(path).listFiles()!!) {
            if (file.path.endsWith(".ds")) {
                convertFile(file.path, "$outputPath/${file.name.split(".").dropLast(1).joinToString(".")}.lab")
                totalConverted++
            }
        }

        println("\nConverted $totalConverted files (wrote to $outputPath)")
    }
}