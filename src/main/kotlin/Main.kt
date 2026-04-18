package ns.dslab

import java.io.File

val workingDirectory = System.getProperty("user.dir")!!

fun main() {
    println("""NeuroSynthProject DiffSingerScript (.ds) to .lab converter
        |
        |Use 'convert <path>' to convert a file or folder with files
    """.trimMargin())

    while (true) {
        print("(dslab) $workingDirectory>")
        val command = readln()
        val commandParts = command.split(" ")

        if (commandParts.isEmpty()) continue

        when (commandParts[0]) {
            "exit" -> break
            "convert" -> {
                if (commandParts.size < 2) {
                    println("ERROR: path not specified (must have a value)")
                    continue
                }

                val path = commandParts.drop(1).joinToString(" ")

                if (!File(path).exists()) {
                    println("ERROR: file or directory '$path' does not exist")
                    continue
                }

                try {
                    if (File(path).isDirectory) {
                        Converters.convertDirectory(path)
                    } else {
                        Converters.convertFile(path, "${path.split(".").dropLast(1).joinToString(".")}.lab")
                    }
                } catch (e: Exception) {
                    println("Error when converting")
                    e.printStackTrace()
                }
            }
        }
    }
}