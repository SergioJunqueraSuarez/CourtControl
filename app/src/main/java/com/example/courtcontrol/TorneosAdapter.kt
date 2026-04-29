package com.example.courtcontrol

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TorneosAdapter(
    private var torneos: List<Torneo>,
    private val db: DBHelper,
    private val esAdmin: Boolean,
    private val onTorneoClick: (Torneo) -> Unit,
    private val onTorneoLongClick: (Torneo) -> Unit
) : RecyclerView.Adapter<TorneosAdapter.TorneoViewHolder>() {

    inner class TorneoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreTorneo)
        val tvFechaLugar = itemView.findViewById<TextView>(R.id.tvFechaLugar)
        val tvEstado = itemView.findViewById<TextView>(R.id.tvEstado)
        val tvJugadores = itemView.findViewById<TextView>(R.id.tvJugadores)
        val tvCapitanes = itemView.findViewById<TextView>(R.id.tvCapitanes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TorneoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_torneo, parent, false)
        return TorneoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TorneoViewHolder, position: Int) {
        val torneo = torneos[position]
        val context = holder.itemView.context

        holder.tvNombre.text = torneo.nombre
        holder.tvFechaLugar.text = "${torneo.fecha} - ${torneo.lugar}"
        holder.tvEstado.text = torneo.estado

        val numJugadores = db.contarInscritosPorTorneo(torneo.id)
        val numCapitanes = db.contarCapitanes(torneo.id)
        val capitanesRestantes = db.capitanesRestantes(torneo.id)

        holder.tvJugadores.text = "$numJugadores/${DBHelper.MAX_JUGADORES_POR_TORNEO} jugadores"
        holder.tvCapitanes.text = "Capitanes $numCapitanes/${DBHelper.MAX_EQUIPOS_POR_TORNEO}"

        if (numJugadores >= DBHelper.MAX_JUGADORES_POR_TORNEO) {
            holder.tvJugadores.setTextColor(ContextCompat.getColor(context, R.color.cc_danger))
        } else {
            holder.tvJugadores.setTextColor(ContextCompat.getColor(context, R.color.cc_info))
        }

        if (capitanesRestantes == 0) {
            holder.tvCapitanes.setTextColor(ContextCompat.getColor(context, R.color.cc_success_text))
        } else {
            holder.tvCapitanes.setTextColor(ContextCompat.getColor(context, R.color.cc_warning))
        }

        holder.itemView.setOnClickListener {
            onTorneoClick(torneo)
        }

        holder.itemView.setOnLongClickListener {
            if (esAdmin) {
                onTorneoLongClick(torneo)
                true
            } else {
                false
            }
        }
    }

    override fun getItemCount() = torneos.size

    fun actualizarLista(nuevaLista: List<Torneo>) {
        torneos = nuevaLista
        notifyDataSetChanged()
    }
}
