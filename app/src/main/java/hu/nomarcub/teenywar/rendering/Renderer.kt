package hu.nomarcub.teenywar.rendering

import android.content.Context
import android.graphics.Canvas
import hu.nomarcub.teenywar.model.control.Level

class Renderer(private val context: Context, private val width: Int, private val height: Int) {
    init {
    }

    private val background = Background(context)
    private val levelDrawer = LevelDrawer()

    init {
        background.setScreenSize(width, height)
        levelDrawer.setScreenSize(width, height)
    }

    fun step() {
        Level.current.step()
    }

    fun draw(canvas: Canvas) {
        background.render(canvas)
        levelDrawer.render(canvas)
    }

//    fun selectBase(base: Base) {
//        selectedBases.clear()
//        selectedBases.add(base)
//    }

}


