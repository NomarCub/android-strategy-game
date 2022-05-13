package hu.nomarcub.teenywar.model.control

import android.graphics.Color
import android.graphics.PointF
import hu.nomarcub.teenywar.model.buidingblock.Agent
import hu.nomarcub.teenywar.model.buidingblock.Base
import hu.nomarcub.teenywar.model.buidingblock.Packet
import hu.nomarcub.teenywar.model.events.ReceiveEvent
import hu.nomarcub.teenywar.model.events.SendEvent
import org.greenrobot.eventbus.Subscribe

class Level(
    bases: List<Base> = listOf(),
    packets: List<Packet> = listOf(),
    machineControllers: List<MachineController> = listOf()
) {
    companion object {
        var current: Level = Level()

        //TODO val neutralColor = ContextCompat.getColor(this as Context, R.color.neutralAgent);
        public fun makeLevel(): Level {
            val player = Agent(Agent.Type.LOCAL, "Pityu", Color.parseColor("#FFFF0000"))
            val enemy = Agent(Agent.Type.MACHINE, "Terminator", Color.parseColor("#FF0000FF"))
            val neutral = Agent.neutral

            val bases = mutableListOf<Base>()

            bases.add(Base(neutral, PointF(0.4f, 0.4f), 20, 60, 30))
            bases.add(Base(neutral, PointF(0.6f, 0.6f), 20, 60, 30))
            bases.add(Base(player, PointF(0.4f, 0.6f), 20, 60, 30))
            bases.add(Base(enemy, PointF(0.6f, 0.4f), 20, 60, 30))

            val ctrl = MachineController(enemy)

            return Level(
                bases = bases,
                machineControllers = listOf(ctrl)
            )
        }
    }

    private val _bases = bases.toMutableList()

    public val bases: List<Base>
        get() = _bases.toList()
    private val _packets = packets.toMutableList()

    init {
        machineControllers.forEach { it.level = this }
    }

    public val packets: List<Packet>
        @Synchronized get() = _packets.toList()
    @Transient
    private val selectedBases = mutableListOf<Base>()

    val machineControllers = machineControllers.toMutableList()

    public val agents: Set<Agent>
        get() {
            val ret = mutableSetOf<Agent>()
            for (base in bases) ret.add(base.owner)
            for (packet in packets) ret.add(packet.owner)
            return ret
        }

    @Transient
    public var isPaused = true

    public var hasWon = 0
    public var hasLost = 0

    public fun step() {
        if (isPaused) return

        for (base in bases)
            base.step()
        for (packet in packets)
            packet.step()
        for (mc in machineControllers)
            mc.step()
    }

    //TODO more flexibility
    public fun handleTouch(point: PointF) {
        if (isPaused) return
        val touchedBase = findBase(point)
        if (touchedBase == null) {
            selectedBases.clear()
            for (base in bases) {
                base.isHighlighted = false
            }
        } else {
            if (selectedBases.isEmpty() && touchedBase.owner.type == Agent.Type.LOCAL) {
                selectedBases.add(touchedBase)
                touchedBase.isHighlighted = true
            } else if (!selectedBases.isEmpty()) {
                for (base in selectedBases) {
                    if (base != touchedBase)
                        base.send(touchedBase)
                }
            }
        }
    }

    private fun findBase(point: PointF): Base? {
        for (base in bases) {
            if (base.contains(point)) {
                return base
            }
        }
        return null
    }

    @Synchronized
    public fun addRemotePacket(packet: Packet) {
        bases.find { it == packet.target }?.let {
            _packets.add(Packet(packet.source, it, packet.hitPoints, packet.distanceLeft, packet.owner))
        }

        bases.find { it == packet.source }?.remotePacketSent(packet.hitPoints)
    }

    @Synchronized
    public fun changeAgent(from: Agent, to: Agent) {
        val changedBases = bases.filter { it.owner == from }
        val changedPackets = packets.filter { it.owner == from }

        for (base in changedBases) {
            _bases.remove(base)
            _bases.add(Base(base, to))
        }
        for (packet in changedPackets) {
            _packets.remove(packet)
            _packets.add(Packet(packet, to))
        }
        fixGson()
    }

    //TODO use proper Gson solution instead
    @Synchronized
    public fun fixGson() {
        val temp = mutableListOf<Packet>()

        for (packet in packets) {
            bases.find { it == packet.target }?.let {
                temp.add(Packet(packet.source, it, packet.hitPoints, packet.distanceLeft, packet.owner))
            }
        }

        _packets.clear()
        _packets.addAll(temp)

        machineControllers.forEach { it.level = this }
    }

    public fun stats(): Map<Agent, Int> {
        val ret = mutableMapOf<Agent, Int>()
        for (agent in agents) {
            var sum = 0
            bases.filter { it.owner == agent }.forEach { sum += it.hitPoints }
            packets.filter { it.owner == agent }.forEach { sum += it.hitPoints }
            ret[agent] = sum
        }
        return ret
    }

    public val won
        get() = agents.all { it.type == Agent.Type.NEUTRAL || it.type == Agent.Type.LOCAL }

    public val lost
        get() = agents.none { it.type == Agent.Type.LOCAL }

    @Subscribe//(threadMode = ThreadMode.ASYNC)
    @Synchronized
    public fun onPacketReceived(event: ReceiveEvent) {
        _packets.remove(event.packet)
        if (event.wasCaptured) {
            selectedBases.remove(event.packet.target)
            event.packet.target.isHighlighted = false
        }
    }

    @Subscribe//(threadMode = ThreadMode.ASYNC)
    @Synchronized
    public fun onPacketSent(event: SendEvent) {
        _packets.add(event.packet)
    }

}