package hu.nomarcub.teenywar.extensions

import android.graphics.Color

public fun Int.textOnBackgroundColor(): Int {
    val intensity = (Color.red(this) + Color.green(this) + Color.blue(this)) / 3
    return when {
        intensity < 127 -> Color.WHITE
        else -> Color.BLACK
    }
}

public fun Int.multiplyColor(factor: Float): Int {
    val a = Color.alpha(this)
    val r = Math.round(Color.red(this) * factor)
    val g = Math.round(Color.green(this) * factor)
    val b = Math.round(Color.blue(this) * factor)
    return Color.argb(
        a,
        Math.min(r, 255),
        Math.min(g, 255),
        Math.min(b, 255)
    )
}