package hu.nomarcub.teenywar.rendering

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.util.SparseArray
import hu.nomarcub.teenywar.extensions.multiplyColor
import hu.nomarcub.teenywar.extensions.textOnBackgroundColor
import hu.nomarcub.teenywar.model.buidingblock.StrategicUnit
import hu.nomarcub.teenywar.model.control.Level

class LevelDrawer() : Renderable {

    private var screenWidth = 0
    private var screenHeight = 0

    private val unitPaints = SparseArray<Paint>()
    private val textPaints = SparseArray<Paint>()

    private var highlightPaint = Paint().apply {
        color = Color.CYAN
        isAntiAlias = true
    }

    private val rimMultiplier = 0.6f

    init {
        for (agent in Level.current.agents) {
            registerUnitPaint(agent.color.multiplyColor(rimMultiplier))
            registerUnitPaint(agent.color)
        }

        val white = Paint()
        white.color = Color.WHITE
        white.textAlign = Paint.Align.CENTER
        white.isAntiAlias = true

        val black = Paint()
        black.color = Color.BLACK
        black.textAlign = Paint.Align.CENTER
        black.isAntiAlias = true

        textPaints.put(Color.BLACK, black)
        textPaints.put(Color.WHITE, white)
    }

    override fun setScreenSize(x: Int, y: Int) {
        screenWidth = x
        screenHeight = y
    }

    private fun draw(canvas: Canvas, unit: StrategicUnit) {
        val innerPaint = unitPaints[unit.owner.color] ?: registerUnitPaint(unit.owner.color)
        val rimPaint =
            if (unit.isHighlighted) highlightPaint
            else unitPaints[unit.owner.color.multiplyColor(rimMultiplier)]
                ?: registerUnitPaint(unit.owner.color.multiplyColor(rimMultiplier))

        canvas.drawCircle(
            screenWidth * unit.center.x,
            screenWidth * unit.center.y,
            unit.radius * screenWidth,
            rimPaint
        )

        canvas.drawCircle(
            screenWidth * unit.center.x,
            screenWidth * unit.center.y,
            unit.radius * screenHeight * 0.9f,
            innerPaint
        )

        drawTextOnUnit(unit.hitPoints.toString(), unit, canvas)
    }

    override fun render(canvas: Canvas) {
        for (base in Level.current.bases)
            draw(canvas, base)
        for (packet in Level.current.packets)
            draw(canvas, packet)
    }

    private fun drawTextOnUnit(text: String, unit: StrategicUnit, canvas: Canvas) {
        //TODO what if it doesn't fit right?
        val textPaint = textPaints[unit.owner.color.textOnBackgroundColor()]
        textPaint!!.textSize = unit.radius * screenWidth * 0.6f
//        val width = textPaint.measureText(text) / 2
//        val textSize = textPaint.textSize

        canvas.drawText(
            text,
            screenWidth * unit.center.x,
            screenHeight * unit.center.y + textPaint.textSize * 0.35f,
            textPaint
        )
    }

    private fun registerUnitPaint(@ColorInt color: Int): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = color
        unitPaints.put(paint.color, paint)
        return paint
    }

}
