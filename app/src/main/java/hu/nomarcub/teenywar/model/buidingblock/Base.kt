package hu.nomarcub.teenywar.model.buidingblock

import android.graphics.PointF
import com.google.gson.annotations.SerializedName
import hu.nomarcub.teenywar.model.events.ReceiveEvent
import hu.nomarcub.teenywar.model.events.SendEvent
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.math.max
import kotlin.math.min


class Base(owner: Agent, center: PointF, hitPoints: Int, val capacity: Int, val growCounter: Int) :
    StrategicUnit(
        owner, center, hitPoints,
        calculateRadius(capacity)
    ) {

    constructor(base: Base, newOwner: Agent) : this(
        newOwner,
        base.center,
        base.hitPoints,
        base.capacity,
        base.growCounter
    )

    companion object {
        //TODO make it better
        private fun calculateRadius(capacity: Int) =
            min(max((Math.atan(Math.log(capacity.toDouble())) / 10 * 2 / Math.PI), 0.01), 0.3).toFloat()

        fun random(): Base {
            val r = Random()
            return Base(
                owner = Agent.neutral,
                center = PointF(r.nextFloat(), r.nextFloat()),
                hitPoints = r.nextInt(300),
                capacity = r.nextInt(3000),
                growCounter = r.nextInt(3000)
            )
        }

    }

    private var stepTillGrow = growCounter

    override fun step() {
        if (owner.type == Agent.Type.NEUTRAL || hitPoints > capacity)
            return

        if (stepTillGrow <= 0) {
            hitPoints++
            stepTillGrow = growCounter
        } else
            stepTillGrow--
    }

    @SerializedName("realOwner")
    override var owner = Agent.neutral
        private set
    @SerializedName("realHitPoints")
    override var hitPoints: Int = 0
        private set

    init {
        this.hitPoints = hitPoints
        this.owner = owner
        //TODO: check args
    }


    public fun send(target: Base) {
        if (owner.type == Agent.Type.NEUTRAL) return
        //TODO something other than 50%
        val amount = hitPoints / 2
        if (amount <= 0) return
        hitPoints -= amount
        val packet = Packet(this, target, amount)

//        Level.current.sendPacket(SendEvent(packet))
        EventBus.getDefault().post(SendEvent(packet))
    }

    fun receive(packet: Packet) {
        var wasCaptured = false
        when {
            owner == packet.owner -> hitPoints += packet.hitPoints
            hitPoints == packet.hitPoints -> {
                owner = Agent.neutral
                hitPoints = 0
                wasCaptured = true //TODO why?
            }
            hitPoints <= packet.hitPoints -> {
                hitPoints = packet.hitPoints - hitPoints
                owner = packet.owner
                wasCaptured = true
            }
            else -> hitPoints -= packet.hitPoints
        }

//        Level.current.receivePacket(ReceiveEvent(packet,wasCaptured))
        EventBus.getDefault().post(ReceiveEvent(packet, wasCaptured))
    }

    fun remotePacketSent(hitPoints: Int) {
        this.hitPoints -= hitPoints
        this.hitPoints = max(this.hitPoints, 1)
    }

    //TODO update when adding new members
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Base

        if (capacity != other.capacity) return false
        if (growCounter != other.growCounter) return false
        if (center.x != other.center.x) return false
        if (center.y != other.center.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = capacity
        result = 31 * result + growCounter
        result = 31 * result + center.x.hashCode()
        result = 31 * result + center.y.hashCode()
        return result
    }

}