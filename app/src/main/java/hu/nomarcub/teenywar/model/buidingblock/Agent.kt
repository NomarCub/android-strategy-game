package hu.nomarcub.teenywar.model.buidingblock

import android.graphics.Color
import android.support.annotation.ColorInt
import hu.nomarcub.teenywar.extensions.multiplyColor

class Agent(val type: Type, val name: String, @ColorInt val color: Int) {

    enum class Type {
        LOCAL, REMOTE, NEUTRAL, MACHINE
    }

    companion object {
        //TODO color from resource
        val neutral: Agent =
            Agent(
                Type.NEUTRAL,
                "Neutral",
                Color.WHITE.multiplyColor(0.75f)
            )
    }

    init {
        //TODO arg checks
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Agent

        if (type != other.type) return false
        if (name != other.name) return false
        if (color != other.color) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + color
        return result
    }
}
