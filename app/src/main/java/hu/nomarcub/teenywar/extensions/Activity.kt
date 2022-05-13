package hu.nomarcub.teenywar.extensions

import android.app.Activity
import android.graphics.Color
import android.preference.PreferenceManager
import com.google.gson.Gson
import hu.nomarcub.teenywar.model.buidingblock.Agent
import hu.nomarcub.teenywar.model.control.Level
import hu.nomarcub.teenywar.model.control.MachineController
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

const val localLevelsAssetPath = "levels/local"
const val multiLevelsAssetPath = "levels/multi"

public val gson = Gson()

public val Activity.volume: Int
    get() = PreferenceManager.getDefaultSharedPreferences(this).getInt("volume", 0)

public val Activity.localColor: Int
    get() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        return Color.rgb(
            sharedPreferences.getInt("red", 0),
            sharedPreferences.getInt("green", 0),
            sharedPreferences.getInt("blue", 0)
        )
    }

public val Activity.localAgent: Agent
    get() = Agent(Agent.Type.LOCAL, "Bartholomew", localColor)

public val Activity.difficulty
    get() = when (PreferenceManager.getDefaultSharedPreferences(this).getString("machine", "0") ?: "0") {
        "0" -> MachineController.Type.RANDOM
        "1" -> MachineController.Type.NORMAL
        "2" -> MachineController.Type.AGGRESSIVE
        else -> MachineController.Type.RANDOM
    }

private fun Activity.getLevelsFromAsset(multi: Boolean): List<Level> {
    val levels = mutableListOf<Level>()

    val path = if (multi) multiLevelsAssetPath else localLevelsAssetPath

    assets.list(path)?.forEach { asset ->
        val level = gson.fromJson(InputStreamReader(assets.open("$path/$asset")), Level::class.java)

        if (!multi)
            level.agents.find { it.type == Agent.Type.LOCAL }?.let { it ->
                level.changeAgent(it, localAgent)
            }
        levels.add(level)
    }

    return levels
}

public fun Activity.getLevelsFromSavedFile(multi: Boolean): List<Level> {
    val levels = mutableListOf<Level>()

    val levelFolderName = "levels"
    val localLevelsFolderName = "local"
    val multiLevelsFolderName = "multi"

    File(File(filesDir, levelFolderName), if (multi) multiLevelsFolderName else localLevelsFolderName).apply {
        list().forEach {
            val level = gson.fromJson(File("$absolutePath/$it").bufferedReader(), Level::class.java)
            level.fixGson()
            if (!multi)
                setupMachine(level)
            levels.add(level)
        }
    }

    return levels
}

private fun Activity.setupMachine(level: Level) {
    level.apply {
        machineControllers.clear()
        agents.filter { it.type == Agent.Type.MACHINE }.forEach { agent ->
            val mc = MachineController(agent, difficulty)
            mc.level = this
            machineControllers.add(mc)
        }
    }
}

public fun Activity.migrateLevels() {
    val localLevels = getLevelsFromAsset(false)
    val multiLevels = getLevelsFromAsset(true)

    val levelFolderName = "levels"
    val localLevelsFolderName = "local"
    val multiLevelsFolderName = "multi"

    val levelDir = File(filesDir, levelFolderName)
    levelDir.mkdirs()
    val localLevelsDir = File(levelDir, localLevelsFolderName)
    localLevelsDir.mkdirs()
    val multiLevelsDir = File(levelDir, multiLevelsFolderName)
    multiLevelsDir.mkdirs()


    for ((index, level) in localLevels.withIndex()) {
        BufferedWriter(FileOutputStream(File(localLevelsDir, "level$index.json")).writer()).apply {
            write(gson.toJson(level))
            close()
        }
    }

    for ((index, level) in multiLevels.withIndex()) {
        BufferedWriter(FileOutputStream(File(multiLevelsDir, "level$index.json")).writer()).apply {
            write(gson.toJson(level))
            close()
        }
    }
}

public fun Activity.levelWon(multi: Boolean, levelID: Int, won: Boolean) {
    val level = getLevelsFromSavedFile(multi)[levelID]

    if (won)
        level.hasWon++
    else
        level.hasLost++

    val levelFolderName = "levels"
    val localLevelsFolderName = "local"
    val multiLevelsFolderName = "multi"

    val levelDir = File(filesDir, levelFolderName)
    levelDir.mkdirs()
    val localLevelsDir = File(levelDir, localLevelsFolderName)
    localLevelsDir.mkdirs()
    val multiLevelsDir = File(levelDir, multiLevelsFolderName)
    multiLevelsDir.mkdirs()

    BufferedWriter(
        FileOutputStream(
            File(if (multi) multiLevelsDir else localLevelsDir, "level$levelID.json")
        ).writer()
    ).apply {
        write(gson.toJson(level))
        close()
    }
}

//        openFileOutput("level.txt", Context.MODE_PRIVATE).bufferedWriter().apply {
//            write(gson.toJson(getLevelsFromAsset(false)[0]))
//            close()
//        }
//        val levelIn = gson.fromJson(openFileInput("level.txt").bufferedReader(), Level::class.java)
