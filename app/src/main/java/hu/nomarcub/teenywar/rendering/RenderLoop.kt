package hu.nomarcub.teenywar.rendering

import android.content.Context
import android.graphics.Canvas

class RenderLoop(context: Context, private val view: GameView, width: Int, height: Int) : Thread() {

    //TODO make it faster, FPS = ?

    val renderer = Renderer(context, width, height)

    var running = false

    companion object {
        private const val FPS: Long = 30
        private const val TIME_BETWEEN_FRAMES = 1000 / FPS
    }

    private fun sleepThread(time: Long) {
        try {
            Thread.sleep(time)
        } catch (e: InterruptedException) {
            // ignored
        }
    }

    private fun getTime() = System.currentTimeMillis()

    override fun run() {
        while (running) {
            val renderStart = getTime()
            draw()
            val renderEnd = getTime()

            val sleepTime = TIME_BETWEEN_FRAMES - (renderEnd - renderStart)
            if (sleepTime > 0) {
                sleepThread(sleepTime)
            } else {
                sleepThread(5)
            }
        }
    }

    private fun draw() {
        renderer.step()

        var canvas: Canvas? = null

        try {
            canvas = view.holder.lockCanvas()
            synchronized(view.holder) {
                renderer.draw(canvas)
            }

        } catch (e: Exception) {
        } finally {
            if (canvas != null) {
                view.holder.unlockCanvasAndPost(canvas)
            }
        }
    }

//    fun selectBase(base: Base) {
//        renderer.selectBase(base)
//    }

}