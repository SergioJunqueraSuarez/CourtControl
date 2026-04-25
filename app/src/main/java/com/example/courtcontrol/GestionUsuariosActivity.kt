package com.example.courtcontrol

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GestionUsuariosActivity : AppCompatActivity() {

    private lateinit var db: DBHelper
    private lateinit var adapter: AdminUsuariosAdapter
    private lateinit var tvEmpty: TextView
    private var usuarioId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        usuarioId = intent.getIntExtra("usuario_id", -1)
        db = DBHelper(this)
        val esAdmin = usuarioId != -1 && db.obtenerRolPorId(usuarioId) == "admin"

        if (!esAdmin) {
            Toast.makeText(this, "Acceso solo para administradores", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContentView(R.layout.activity_gestion_usuarios)

        val recycler = findViewById<RecyclerView>(R.id.rvUsuariosAdmin)
        tvEmpty = findViewById(R.id.tvSinUsuariosAdmin)

        recycler.layoutManager = LinearLayoutManager(this)
        adapter = AdminUsuariosAdapter(emptyList()) { usuario ->
            val ok = db.actualizarRolUsuario(usuario.id_usuario, "admin")
            if (ok) {
                Toast.makeText(this, "${usuario.usuario} ahora es administrador", Toast.LENGTH_SHORT).show()
                cargarUsuarios()
            } else {
                Toast.makeText(this, "No se pudo actualizar el rol", Toast.LENGTH_SHORT).show()
            }
        }
        recycler.adapter = adapter

        cargarUsuarios()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            cargarUsuarios()
        }
    }

    private fun cargarUsuarios() {
        val usuarios = db.obtenerUsuariosNoAdmin()
        adapter.actualizarLista(usuarios)
        tvEmpty.visibility = if (usuarios.isEmpty()) View.VISIBLE else View.GONE
    }
}
