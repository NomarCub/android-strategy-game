package hu.nomarcub.teenywar.rendering

import android.content.Context
import android.graphics.Canvas
import android.support.v4.content.ContextCompat
import hu.nomarcub.teenywar.R

class Background(private val context: Context) : Renderable {

    private var width: Int = 0
    private var height: Int = 0
    private var color = ContextCompat.getColor(context, R.color.gameBackground)

    override fun setScreenSize(x: Int, y: Int) {
        this.width = x
        this.height = y
    }

    override fun render(canvas: Canvas) {
        canvas.drawColor(color)
    }

}
