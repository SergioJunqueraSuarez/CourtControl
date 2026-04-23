package com.example.courtcontrol

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TorneosAdapter(
    private var torneos: List<Torneo>,
    private val usuarioId: Int,
    private val db: DBHelper
) : RecyclerView.Adapter<TorneosAdapter.TorneoViewHolder>() {

    private val maxJugadores = 20

    inner class TorneoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreTorneo)
        val tvFechaLugar = itemView.findViewById<TextView>(R.id.tvFechaLugar)
        val tvEstado = itemView.findViewById<TextView>(R.id.tvEstado)
        val tvJugadores = itemView.findViewById<TextView>(R.id.tvJugadores)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TorneoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_torneo, parent, false)
        return TorneoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TorneoViewHolder, position: Int) {

        val torneo = torneos[position]

        holder.tvNombre.text = torneo.nombre
        holder.tvFechaLugar.text = "${torneo.fecha} - ${torneo.lugar}"
        holder.tvEstado.text = torneo.estado

        val numJugadores = db.contarInscritosPorTorneo(torneo.id)

        holder.tvJugadores.text = "$numJugadores/$maxJugadores jugadores"

        if (numJugadores >= maxJugadores) {
            holder.tvJugadores.setTextColor(android.graphics.Color.RED)
        } else {
            holder.tvJugadores.setTextColor(android.graphics.Color.parseColor("#1976D2"))
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, JugadoresTorneo::class.java)

            intent.putExtra("id_torneo", torneo.id)
            intent.putExtra("usuario_id", usuarioId)

            context.startActivity(intent)
        }
    }

    override fun getItemCount() = torneos.size

    fun actualizarLista(nuevaLista: List<Torneo>) {
        torneos = nuevaLista
        notifyDataSetChanged()
    }
}