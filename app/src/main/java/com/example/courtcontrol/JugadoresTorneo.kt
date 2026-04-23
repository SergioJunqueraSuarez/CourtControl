package com.example.courtcontrol

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class JugadoresTorneo : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var btnInscribirse: Button
    private var idTorneo: Int = -1
    private var usuarioId: Int = -1
    private lateinit var db: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jugadores_torneo)

        recycler = findViewById(R.id.recyclerJugadores)
        btnInscribirse = findViewById(R.id.btnInscribirse)

        idTorneo = intent.getIntExtra("id_torneo", -1)
        usuarioId = intent.getIntExtra("usuario_id", -1)

        if (idTorneo == -1 || usuarioId == -1) {
            Toast.makeText(this, "Error al abrir torneo", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db = DBHelper(this)

        cargarJugadores()

        btnInscribirse.setOnClickListener {
            gestionarInscripcion()
        }
    }

    private fun cargarJugadores() {
        val jugadores = db.obtenerUsuariosPorTorneo(idTorneo)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = UsuariosAdapter(jugadores)
    }

    private fun gestionarInscripcion() {

        val maxJugadores = 10
        val inscritos = db.contarInscritos(idTorneo)

        if (inscritos >= maxJugadores) {
            Toast.makeText(this, "El torneo está lleno", Toast.LENGTH_SHORT).show()
            return
        }

        if (db.yaInscrito(usuarioId, idTorneo)) {
            Toast.makeText(this, "Ya estás inscrito", Toast.LENGTH_SHORT).show()
            return
        }

        val opciones = arrayOf("Capitán", "Jugador")

        AlertDialog.Builder(this)
            .setTitle("¿Cómo quieres inscribirte?")
            .setItems(opciones) { _, which ->

                val tipo = if (which == 0) "CAPITAN" else "JUGADOR"

                val ok = db.inscribirUsuario(usuarioId, idTorneo, tipo)

                if (ok) {
                    Toast.makeText(this, "Inscrito como $tipo", Toast.LENGTH_SHORT).show()
                    cargarJugadores() // ✅ mejor que recreate()
                } else {
                    Toast.makeText(this, "Error al inscribirse", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}