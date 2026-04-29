package com.example.courtcontrol

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class PartidosAdapter(
    private var partidos: List<PartidoTorneo>,
    private val esAdmin: Boolean,
    private val onSeleccionarGanador: (PartidoTorneo) -> Unit
) : RecyclerView.Adapter<PartidosAdapter.PartidoViewHolder>() {

    inner class PartidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card = itemView.findViewById<MaterialCardView>(R.id.cardPartido)
        val tvRonda = itemView.findViewById<TextView>(R.id.tvRondaPartido)
        val tvEquipo1 = itemView.findViewById<TextView>(R.id.tvEquipo1Partido)
        val tvEquipo2 = itemView.findViewById<TextView>(R.id.tvEquipo2Partido)
        val tvVersus = itemView.findViewById<TextView>(R.id.tvVersusPartido)
        val tvResultado = itemView.findViewById<TextView>(R.id.tvResultadoPartido)
        val tvGanador = itemView.findViewById<TextView>(R.id.tvGanadorPartido)
        val btnResultado = itemView.findViewById<Button>(R.id.btnResultadoPartido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_partido, parent, false)
        return PartidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PartidoViewHolder, position: Int) {
        val partido = partidos[position]
        val esFinal = partido.ronda > 1

        holder.tvRonda.text = if (esFinal) "Final" else "Semifinal ${partido.orden}"
        holder.tvEquipo1.text = partido.equipo1
        holder.tvEquipo2.text = partido.equipo2
        holder.tvVersus.text = if (esFinal) "FINAL" else "VS"
        holder.tvResultado.text = partido.resultado?.let { "Marcador: $it" } ?: "Marcador pendiente"
        holder.tvGanador.text = partido.ganador?.let { "Ganador: $it" } ?: "Resultado pendiente"

        if (esFinal) {
            holder.card.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF7E8"))
            holder.card.strokeColor = android.graphics.Color.parseColor("#F4B740")
            holder.tvRonda.setBackgroundColor(android.graphics.Color.parseColor("#FDE68A"))
            holder.tvRonda.setTextColor(android.graphics.Color.parseColor("#92400E"))
            holder.tvVersus.setTextColor(android.graphics.Color.parseColor("#B45309"))
        } else {
            holder.card.setCardBackgroundColor(android.graphics.Color.WHITE)
            holder.card.strokeColor = android.graphics.Color.parseColor("#D7E2F0")
            holder.tvRonda.setBackgroundColor(android.graphics.Color.parseColor("#E8F1FB"))
            holder.tvRonda.setTextColor(android.graphics.Color.parseColor("#1F4E79"))
            holder.tvVersus.setTextColor(android.graphics.Color.parseColor("#6B7280"))
        }

        if (partido.ganador == partido.equipo1) {
            holder.tvEquipo1.setBackgroundColor(android.graphics.Color.parseColor("#DCFCE7"))
            holder.tvEquipo2.setBackgroundColor(android.graphics.Color.parseColor("#F8FAFC"))
        } else if (partido.ganador == partido.equipo2) {
            holder.tvEquipo1.setBackgroundColor(android.graphics.Color.parseColor("#F8FAFC"))
            holder.tvEquipo2.setBackgroundColor(android.graphics.Color.parseColor("#DCFCE7"))
        } else {
            holder.tvEquipo1.setBackgroundColor(android.graphics.Color.parseColor("#F8FAFC"))
            holder.tvEquipo2.setBackgroundColor(android.graphics.Color.parseColor("#F8FAFC"))
        }

        holder.tvGanador.setBackgroundColor(
            if (partido.ganador == null) {
                android.graphics.Color.parseColor("#F3F4F6")
            } else {
                android.graphics.Color.parseColor("#ECFDF5")
            }
        )
        holder.tvGanador.setTextColor(
            if (partido.ganador == null) {
                android.graphics.Color.parseColor("#4B5563")
            } else {
                android.graphics.Color.parseColor("#166534")
            }
        )

        holder.btnResultado.visibility = if (esAdmin) View.VISIBLE else View.GONE
        holder.btnResultado.text = if (partido.ganador == null) "Registrar resultado" else "Corregir resultado"
        holder.btnResultado.isEnabled = true
        holder.btnResultado.alpha = 1f
        holder.btnResultado.setOnClickListener {
            onSeleccionarGanador(partido)
        }
    }

    override fun getItemCount(): Int = partidos.size

    fun actualizarLista(nuevaLista: List<PartidoTorneo>) {
        partidos = nuevaLista
        notifyDataSetChanged()
    }
}
