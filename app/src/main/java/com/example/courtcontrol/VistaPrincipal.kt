package com.example.courtcontrol

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
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
    private lateinit var btnCrearTorneoAdmin: Button
    private lateinit var spinnerEstado: Spinner
    private lateinit var spinnerMes: Spinner
    private var todosLosTorneosVisibles: List<Torneo> = emptyList()

    private val estadosFiltro = listOf(
        "Todos los estados",
        DBHelper.ESTADO_ABIERTO,
        DBHelper.ESTADO_CERRADO,
        DBHelper.ESTADO_EN_JUEGO,
        DBHelper.ESTADO_FINALIZADO
    )

    private val mesesFiltro = listOf(
        "Todos los meses",
        "Enero",
        "Febrero",
        "Marzo",
        "Abril",
        "Mayo",
        "Junio",
        "Julio",
        "Agosto",
        "Septiembre",
        "Octubre",
        "Noviembre",
        "Diciembre"
    )

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
        btnCrearTorneoAdmin = findViewById(R.id.btnCrearTorneoAdmin)
        spinnerEstado = findViewById(R.id.spinnerEstado)
        spinnerMes = findViewById(R.id.spinnerMes)

        configurarFiltros()
        configurarBotonAdmin()
        configurarPerfil()
        configurarListaTorneos()
    }

    override fun onResume() {
        super.onResume()

        if (::torneosAdapter.isInitialized) {
            esAdmin = db.obtenerRolPorId(usuarioId) == "admin"
            btnAdminUsuarios.visibility = if (esAdmin) View.VISIBLE else View.GONE
            btnAdminUsuarios.isEnabled = esAdmin
            btnCrearTorneoAdmin.visibility = if (esAdmin) View.VISIBLE else View.GONE
            btnCrearTorneoAdmin.isEnabled = esAdmin
            recargarTorneos()
        }
    }

    private fun configurarFiltros() {
        spinnerEstado.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            estadosFiltro
        )
        spinnerMes.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            mesesFiltro
        )

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                aplicarFiltros()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        spinnerEstado.onItemSelectedListener = listener
        spinnerMes.onItemSelectedListener = listener
    }

    private fun configurarBotonAdmin() {
        btnAdminUsuarios.visibility = if (esAdmin) View.VISIBLE else View.GONE
        btnAdminUsuarios.isEnabled = esAdmin
        btnCrearTorneoAdmin.visibility = if (esAdmin) View.VISIBLE else View.GONE
        btnCrearTorneoAdmin.isEnabled = esAdmin
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
        btnCrearTorneoAdmin.setOnClickListener {
            if (!esAdmin) {
                return@setOnClickListener
            }

            startActivity(
                Intent(this, CrearTorneo::class.java).apply {
                    putExtra("usuario_id", usuarioId)
                }
            )
        }
    }

    private fun configurarPerfil() {
        val ivPerfil = findViewById<ImageView>(R.id.ivPerfil)
        ivPerfil.setOnClickListener {
            val intent = Intent(this, PerfilUsuario::class.java)
            intent.putExtra("usuario_id", usuarioId)
            startActivity(intent)
        }
    }

    private fun configurarListaTorneos() {
        val rvTorneos = findViewById<RecyclerView>(R.id.rvTorneos)
        rvTorneos.layoutManager = LinearLayoutManager(this)
        torneosAdapter = TorneosAdapter(
            torneos = emptyList(),
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
        recargarTorneos()
    }

    private fun recargarTorneos() {
        todosLosTorneosVisibles = db.obtenerTorneosVisibles(usuarioId)
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        if (!::torneosAdapter.isInitialized) {
            return
        }

        val estadoSeleccionado = spinnerEstado.selectedItem?.toString().orEmpty()
        val mesSeleccionado = spinnerMes.selectedItemPosition

        val filtrados = todosLosTorneosVisibles.filter { torneo ->
            val coincideEstado =
                estadoSeleccionado == estadosFiltro.first() || torneo.estado == estadoSeleccionado
            val coincideMes =
                mesSeleccionado == 0 || obtenerMesDeFecha(torneo.fecha) == mesSeleccionado

            coincideEstado && coincideMes
        }

        torneosAdapter.actualizarLista(filtrados)
    }

    private fun obtenerMesDeFecha(fecha: String): Int? {
        val valor = fecha.trim()
        val mes = when {
            valor.length >= 7 && valor[4] == '-' -> valor.substring(5, 7).toIntOrNull()
            valor.length >= 5 && valor[2] == '/' -> valor.substring(3, 5).toIntOrNull()
            else -> null
        }
        return mes?.takeIf { it in 1..12 }
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
                    recargarTorneos()
                } else {
                    Toast.makeText(this, "No se pudo borrar el torneo", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
