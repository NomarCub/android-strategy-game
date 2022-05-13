package hu.nomarcub.teenywar.extensions

import android.graphics.PointF

public fun PointF.distanceSquared(other: PointF) =
    (this.x - other.x) * (this.x - other.x) + (this.y - other.y) * (this.y - other.y)