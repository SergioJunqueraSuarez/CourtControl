package com.example.courtcontrol

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminUsuariosAdapter(
    private var usuarios: List<Usuario>,
    private val onPromoverClick: (Usuario) -> Unit
) : RecyclerView.Adapter<AdminUsuariosAdapter.UsuarioViewHolder>() {

    inner class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsuario = itemView.findViewById<TextView>(R.id.tvUsuarioAdmin)
        val tvRol = itemView.findViewById<TextView>(R.id.tvRolAdmin)
        val btnPromover = itemView.findViewById<Button>(R.id.btnPromoverAdmin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.tvUsuario.text = usuario.usuario
        holder.tvRol.text = "Rol actual: ${usuario.rol}"
        holder.btnPromover.setOnClickListener {
            onPromoverClick(usuario)
        }
    }

    override fun getItemCount(): Int = usuarios.size

    fun actualizarLista(nuevaLista: List<Usuario>) {
        usuarios = nuevaLista
        notifyDataSetChanged()
    }
}
