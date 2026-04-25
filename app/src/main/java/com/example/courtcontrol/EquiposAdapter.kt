package com.example.courtcontrol

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EquiposAdapter(
    private var equipos: List<EquipoTorneo>
) : RecyclerView.Adapter<EquiposAdapter.EquipoViewHolder>() {

    inner class EquipoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreEquipo)
        val tvCapitan = itemView.findViewById<TextView>(R.id.tvCapitanEquipo)
        val tvMiembros = itemView.findViewById<TextView>(R.id.tvMiembrosEquipo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_equipo, parent, false)
        return EquipoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EquipoViewHolder, position: Int) {
        val equipo = equipos[position]
        holder.tvNombre.text = equipo.nombre
        holder.tvCapitan.text = "Capitan: ${equipo.capitan}"
        holder.tvMiembros.text = if (equipo.miembros.isEmpty()) {
            "Jugadores pendientes"
        } else {
            "Jugadores:\n" + equipo.miembros.joinToString(separator = "\n")
        }
    }

    override fun getItemCount(): Int = equipos.size

    fun actualizarLista(nuevaLista: List<EquipoTorneo>) {
        equipos = nuevaLista
        notifyDataSetChanged()
    }
}
