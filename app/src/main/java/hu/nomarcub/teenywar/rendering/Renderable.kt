package hu.nomarcub.teenywar.rendering

import android.graphics.Canvas

interface Renderable {
    fun setScreenSize(x: Int, y: Int)
    fun render(canvas: Canvas)
}
