package hu.nomarcub.teenywar.rendering

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import hu.nomarcub.teenywar.model.control.Level

class GameView : SurfaceView {

    var renderLoop: RenderLoop? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                // empty
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                var retry = true
                renderLoop?.running = false
                while (retry) {
                    try {
                        renderLoop?.join()
                        retry = false
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                val loop = RenderLoop(context, this@GameView, width, height)
                loop.running = true
                loop.start()

                renderLoop = loop
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            Level.current.handleTouch(PointF(event.x / width, event.y / height))
        }
        return super.onTouchEvent(event)
    }

//    private fun selectBase(base: Base) {
//        renderLoop?.selectBase(base)
//    }

}
