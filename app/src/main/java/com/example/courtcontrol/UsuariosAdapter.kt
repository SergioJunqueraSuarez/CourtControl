package com.example.courtcontrol

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsuariosAdapter(private val lista: List<UsuarioTorneo>) :
    RecyclerView.Adapter<UsuariosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre = view.findViewById<TextView>(R.id.txtUsuario)
        val info = view.findViewById<TextView>(R.id.txtInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        holder.nombre.text = item.usuario

        holder.info.text = when (item.rolTorneo.uppercase()) {
            "CAPITAN" -> "Capitán"
            else -> "Jugador"
        }
    }

    override fun getItemCount(): Int = lista.size
}