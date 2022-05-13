package hu.nomarcub.teenywar.model.buidingblock

import android.graphics.PointF

class Packet(
    val source: Base,
    val target: Base,
    hitPoints: Int,
    distanceLeft: Float = source.distance(target).toFloat(),
    owner: Agent = source.owner
) :
    StrategicUnit(
        owner, PointF(), hitPoints,
        calculateRadius()
    ) {
    constructor(packet: Packet, newOwner: Agent) : this(
        packet.source,
        packet.target,
        packet.hitPoints,
        packet.distanceLeft,
        newOwner
    )

    companion object {
        private fun calculateRadius() = 0.05f
    }

    private val created = System.currentTimeMillis()
    private val distance = source.distance(target).toFloat()
    public var distanceLeft = distanceLeft
        private set
    private var arrived = false

    override fun step() {
        if (arrived) return

        distanceLeft -= 0.01f
        if (distanceLeft <= 0) {
            target.receive(this)
            arrived = true
        }
    }

    override val center: PointF
        get() = calculatePos()

    private fun calculatePos(): PointF {
        val completion: Float = 1 - distanceLeft / distance
        val from = source.edge(target)
        val to = target.edge(source)

        return PointF(
            from.x * (1 - completion) + to.x * completion,
            from.y * (1 - completion) + to.y * completion
        )
    }

    //TODO update when new members are added
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Packet

        if (source != other.source) return false
        if (target != other.target) return false
        if (created != other.created) return false
        if (hitPoints != other.hitPoints) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + target.hashCode()
        result = 31 * result + created.hashCode()
        result = 31 * result + hitPoints
        return result
    }


}