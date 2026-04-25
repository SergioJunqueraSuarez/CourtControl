package com.example.courtcontrol

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DraftJugadoresAdapter(
    private var jugadores: List<Usuario>,
    private val puedeSeleccionar: Boolean,
    private val onSeleccionar: (Usuario) -> Unit
) : RecyclerView.Adapter<DraftJugadoresAdapter.DraftJugadorViewHolder>() {

    inner class DraftJugadorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre = itemView.findViewById<TextView>(R.id.tvJugadorDraftNombre)
        val btnSeleccionar = itemView.findViewById<Button>(R.id.btnSeleccionarDraft)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraftJugadorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_jugador_draft, parent, false)
        return DraftJugadorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DraftJugadorViewHolder, position: Int) {
        val jugador = jugadores[position]
        holder.tvNombre.text = jugador.usuario
        holder.btnSeleccionar.isEnabled = puedeSeleccionar
        holder.btnSeleccionar.setOnClickListener {
            if (puedeSeleccionar) {
                onSeleccionar(jugador)
            }
        }
    }

    override fun getItemCount(): Int = jugadores.size

    fun actualizarLista(nuevaLista: List<Usuario>) {
        jugadores = nuevaLista
        notifyDataSetChanged()
    }
}
