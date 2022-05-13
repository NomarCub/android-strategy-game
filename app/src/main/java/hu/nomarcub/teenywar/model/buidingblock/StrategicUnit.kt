package hu.nomarcub.teenywar.model.buidingblock

import android.graphics.PointF
import hu.nomarcub.teenywar.extensions.distanceSquared

abstract class StrategicUnit(
    open val owner: Agent,
    open val center: PointF,
    open val hitPoints: Int,
    val radius: Float
) {

    @Transient
    public var isHighlighted = false

    public abstract fun step()

    public fun contains(point: PointF) = center.distanceSquared(point) < radius * radius

    public fun intersects(other: StrategicUnit) =
        center.distanceSquared(other.center) < (radius + other.radius) * (radius + other.radius)

    public fun distance(other: StrategicUnit) = Math.sqrt(edge(other).distanceSquared(other.edge(this)).toDouble())

    //TODO calculate
    public fun edge(other: StrategicUnit) = center

}