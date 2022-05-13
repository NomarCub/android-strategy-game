package hu.nomarcub.teenywar.model.control

import hu.nomarcub.teenywar.model.buidingblock.Agent
import hu.nomarcub.teenywar.model.buidingblock.Base
import kotlin.random.Random

class MachineController(private val controlledAgent: Agent, private val type: Type = Type.RANDOM) {
    enum class Type {
        RANDOM, NORMAL, AGGRESSIVE
    }

    @Transient
    public lateinit var level: Level

//    private val a: () -> Unit = this::randomStep

    private val controlledBases get() = level.bases.filter { it.owner == controlledAgent }
    private val controlledHitpoints: Int
        get() {
            var sum = 0
            controlledBases.forEach {
                sum += it.hitPoints
            }
            return sum
        }


    private val controlledPackets get() = level.packets.filter { it.owner == controlledAgent }

    private val otherBases get() = level.bases.filter { it.owner != controlledAgent }
    private val otherPackets get() = level.packets.filter { it.owner != controlledAgent }

    private val hostileBases get() = level.bases.filter { it.owner != controlledAgent && it.owner != Agent.neutral }
    private val hostilePackets get() = level.bases.filter { it.owner != controlledAgent && it.owner != Agent.neutral }

    private val neutralBases get() = level.bases.filter { it.owner == Agent.neutral }

    fun step() {
        when (type) {
            Type.RANDOM -> randomStep()
            Type.NORMAL -> normalStep()
            Type.AGGRESSIVE -> aggressiveStep()
            else -> randomStep()
        }
    }

    private fun aggressiveStep() {
        val target = findTargetAggressive() ?: return
        send(target)
    }

    private fun normalStep() {
        val target = findTargetNormal() ?: return
        send(target)
    }

    private fun send(target: Base) {
        if (controlledHitpoints - controlledBases.size * 2 < target.hitPoints)
            return

//        val senders =controlledBases.shuffled()

//        var iteration = 10000
//        while (actualHitpoints(target) > 0 && iteration > 0) {
//            controlledBases.random().send(target)
//            iteration--
//        }
//        Log.e("iteration: ", "$iteration")

        for (base in controlledBases.shuffled()) {
            if (actualHitpoints(target) <= 0)
                break
            base.send(target)
        }
    }

    private fun findTargetNormal(): Base? {
        val targets = otherBases.sortedBy { actualHitpoints(it) }
            .filter { actualHitpoints(it) > -5 }

        if (targets.isEmpty())
            return null

        return targets[0]
    }

    private fun findTargetAggressive(): Base? {
        val targets = hostileBases.sortedBy { actualHitpoints(it) }
            .filter { actualHitpoints(it) > -5 }

        if (targets.isEmpty())
            return null

        return targets[0]
    }

    private fun actualHitpoints(base: Base): Int {
        var ret = base.hitPoints
        level.packets
            .filter { it.target == base && it.owner == controlledAgent }
            .forEach {
                ret -= it.hitPoints
            }
        return ret
    }

    private fun randomStep() {
        for (base in controlledBases) {
            if (Random.nextFloat() > 0.85f)
                base.send(level.bases.random())
        }
    }
}