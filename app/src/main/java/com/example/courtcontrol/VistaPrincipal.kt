package com.example.courtcontrol

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class VistaPrincipal : AppCompatActivity() {

    private var usuarioId: Int = -1
    private lateinit var db: DBHelper
    private lateinit var torneosAdapter: TorneosAdapter
    private var esAdmin: Boolean = false
    private lateinit var btnAdminUsuarios: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vista_principal)

        usuarioId = intent.getIntExtra("usuario_id", -1)

        if (usuarioId == -1) {
            Toast.makeText(this, "Error usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db = DBHelper(this)
        esAdmin = db.obtenerRolPorId(usuarioId) == "admin"

        btnAdminUsuarios = findViewById(R.id.btnAdminUsuarios)
        btnAdminUsuarios.visibility = if (esAdmin) View.VISIBLE else View.GONE
        btnAdminUsuarios.isEnabled = esAdmin
        btnAdminUsuarios.setOnClickListener {
            if (!esAdmin) {
                return@setOnClickListener
            }

            startActivity(
                Intent(this, GestionUsuariosActivity::class.java).apply {
                    putExtra("usuario_id", usuarioId)
                }
            )
        }

        val ivPerfil = findViewById<ImageView>(R.id.ivPerfil)
        ivPerfil.setOnClickListener {
            val intent = Intent(this, PerfilUsuario::class.java)
            intent.putExtra("usuario_id", usuarioId)
            startActivity(intent)
        }

        val rvTorneos = findViewById<RecyclerView>(R.id.rvTorneos)
        rvTorneos.layoutManager = LinearLayoutManager(this)
        torneosAdapter = TorneosAdapter(
            torneos = db.obtenerTorneosVisibles(usuarioId),
            db = db,
            esAdmin = esAdmin,
            onTorneoClick = { torneo ->
                if (!db.puedeVerDetalleTorneo(usuarioId, torneo.id)) {
                    Toast.makeText(
                        this,
                        "Los jugadores solo pueden entrar cuando los equipos ya estan creados",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    startActivity(
                        Intent(this, JugadoresTorneo::class.java).apply {
                            putExtra("id_torneo", torneo.id)
                            putExtra("usuario_id", usuarioId)
                        }
                    )
                }
            },
            onTorneoLongClick = { torneo ->
                mostrarMenuAdminTorneo(torneo)
            }
        )
        rvTorneos.adapter = torneosAdapter
    }

    override fun onResume() {
        super.onResume()

        if (::torneosAdapter.isInitialized) {
            esAdmin = db.obtenerRolPorId(usuarioId) == "admin"
            btnAdminUsuarios.visibility = if (esAdmin) View.VISIBLE else View.GONE
            btnAdminUsuarios.isEnabled = esAdmin
            torneosAdapter.actualizarLista(db.obtenerTorneosVisibles(usuarioId))
        }
    }

    private fun mostrarMenuAdminTorneo(torneo: Torneo) {
        if (!esAdmin) {
            return
        }

        val opciones = arrayOf("Editar", "Borrar")

        AlertDialog.Builder(this)
            .setTitle(torneo.nombre)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> editarTorneo(torneo.id)
                    1 -> confirmarBorradoTorneo(torneo)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun editarTorneo(idTorneo: Int) {
        startActivity(
            Intent(this, CrearTorneo::class.java).apply {
                putExtra("usuario_id", usuarioId)
                putExtra("torneo_id", idTorneo)
            }
        )
    }

    private fun confirmarBorradoTorneo(torneo: Torneo) {
        AlertDialog.Builder(this)
            .setTitle("Borrar torneo")
            .setMessage("Se borrara ${torneo.nombre} y sus datos asociados.")
            .setPositiveButton("Borrar") { _, _ ->
                val ok = db.borrarTorneo(torneo.id)
                if (ok) {
                    Toast.makeText(this, "Torneo borrado", Toast.LENGTH_SHORT).show()
                    torneosAdapter.actualizarLista(db.obtenerTorneosVisibles(usuarioId))
                } else {
                    Toast.makeText(this, "No se pudo borrar el torneo", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
