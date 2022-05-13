package hu.nomarcub.teenywar.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import hu.nomarcub.teenywar.R
import hu.nomarcub.teenywar.model.buidingblock.Agent
import hu.nomarcub.teenywar.model.control.Level
import kotlinx.android.synthetic.main.row_level.view.*
import kotlinx.android.synthetic.main.textview_unit.view.*


class LevelAdapter : RecyclerView.Adapter<LevelAdapter.ViewHolder>() {

    private val levelList = mutableListOf<Level>()

    var itemClickListener: LevelItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_level, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val level = levelList[position]

        holder.level = level
        holder.tvUnit.text = (position + 1).toString()

        val stats = level.stats()
        holder.tvAgentNames.setLines(stats.size)
        holder.tvAgentNumbers.setLines(stats.size + 1)

        var players = 0
        var machines = 0

        var text = ""
        var numbers = ""

        val machineText = holder.itemView.context.getString(R.string.machine)
        val playerText = holder.itemView.context.getString(R.string.player)
        val neutralText = holder.itemView.context.getString(R.string.neutral)
        val wonText = holder.itemView.context.getString(R.string.won)
        val lostText = holder.itemView.context.getString(R.string.lost)

        for (agent in stats.keys) {
            text += "${when (agent.type) {
                Agent.Type.REMOTE, Agent.Type.LOCAL -> {
                    players++
                    "$playerText $players"
                }
                Agent.Type.NEUTRAL -> {
                    neutralText
                }
                Agent.Type.MACHINE -> {
                    machines++
                    "$machineText $machines"
                }
            }
            }\n"
            numbers += "${stats[agent]}\n"
        }

        holder.tvAgentNames.text = text
        holder.tvAgentNumbers.text = numbers

        holder.tvKD.setLines(2)
        holder.tvKD.text = "$wonText - ${level.hasWon}\n$lostText - ${level.hasLost}"


//        holder.tvAgentNames.text = "Bases: ${level.bases.size}"

        // val resource = when (level.isPaused) {
        //     true -> R.drawable.shape_unit
        //     else -> R.drawable.shape_unit
        // }
        // holder.ivUnit.setImageResource(resource)
    }

    fun addItem(level: Level) {
        val size = levelList.size
        levelList.add(level)
        notifyItemInserted(size)
    }

    fun addAll(levels: List<Level>) {
        val size = levelList.size
        levelList += levels
        notifyItemRangeInserted(size, levels.size)
    }

    fun deleteRow(position: Int) {
        levelList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount() = levelList.size

    fun clear() {
        levelList.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAgentNames: TextView = itemView.tvAgents
        val tvAgentNumbers: TextView = itemView.tvStats
        //        val tvAI: TextView = itemView.tvAI
        val tvUnit: TextView = itemView.tvUnit
        val tvKD: TextView = itemView.tvKD

        var level: Level? = null

        init {
            itemView.setOnClickListener {
                level?.let { level -> itemClickListener?.onItemClick(level, adapterPosition) }
            }

            itemView.setOnLongClickListener { view ->
                itemClickListener?.onItemLongClick(adapterPosition, view)
                true
            }
        }
    }

    interface LevelItemClickListener {
        fun onItemClick(level: Level, id: Int)
        fun onItemLongClick(position: Int, view: View): Boolean
    }

}