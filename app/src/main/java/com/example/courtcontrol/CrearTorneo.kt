package com.example.courtcontrol

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.time.format.DateTimeParseException
import java.util.Locale

class CrearTorneo : AppCompatActivity() {

    private lateinit var db: DBHelper
    private val formatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("uuuu-MM-dd", Locale.US)
        .withResolverStyle(ResolverStyle.STRICT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val usuarioId = intent.getIntExtra("usuario_id", -1)
        db = DBHelper(this)
        val esAdmin = usuarioId != -1 && db.obtenerRolPorId(usuarioId) == "admin"

        if (!esAdmin) {
            Toast.makeText(this, "Acceso solo para administradores", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContentView(R.layout.activity_crear_torneo)

        val torneoId = intent.getIntExtra("torneo_id", -1)
        val editando = torneoId != -1

        val tvTitulo = findViewById<TextView>(R.id.tvCrearTorneoTitulo)
        val etNombre = findViewById<EditText>(R.id.inputTorneoNombre)
        val etFecha = findViewById<EditText>(R.id.inputTorneoFecha)
        val etLugar = findViewById<EditText>(R.id.inputTorneoLugar)
        val etEstado = findViewById<EditText>(R.id.inputTorneoEstado)
        val btnGuardar = findViewById<Button>(R.id.btnCrearTorneo)

        tvTitulo.text = if (editando) "Editar torneo" else "Crear torneo"
        btnGuardar.text = if (editando) "Guardar cambios" else "Crear torneo"
        etEstado.isEnabled = false
        etEstado.isFocusable = false

        etFecha.setOnClickListener {
            mostrarSelectorFecha(etFecha)
        }
        etFecha.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                mostrarSelectorFecha(etFecha)
            }
        }

        if (editando) {
            val torneo = db.obtenerTorneoPorId(torneoId)

            if (torneo == null) {
                Toast.makeText(this, "Torneo no encontrado", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            etNombre.setText(torneo.nombre)
            etFecha.setText(torneo.fecha)
            etLugar.setText(torneo.lugar)
            etEstado.setText(torneo.estado)
        } else {
            etEstado.setText(DBHelper.ESTADO_ABIERTO)
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val fecha = etFecha.text.toString().trim()
            val lugar = etLugar.text.toString().trim()
            val estado = etEstado.text.toString().trim()

            if (nombre.isEmpty() || fecha.isEmpty() || lugar.isEmpty() || estado.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!fechaValida(fecha)) {
                Toast.makeText(this, "La fecha debe tener formato yyyy-MM-dd", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ok = if (editando) {
                db.actualizarTorneo(torneoId, nombre, fecha, lugar, db.obtenerTorneoPorId(torneoId)?.estado ?: DBHelper.ESTADO_ABIERTO)
            } else {
                db.crearTorneo(nombre, fecha, lugar, DBHelper.ESTADO_ABIERTO)
            }

            if (ok) {
                Toast.makeText(
                    this,
                    if (editando) "Torneo actualizado" else "Torneo creado",
                    Toast.LENGTH_SHORT
                ).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Error al guardar el torneo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarSelectorFecha(etFecha: EditText) {
        val fechaActual = try {
            LocalDate.parse(etFecha.text.toString().trim(), formatter)
        } catch (_: DateTimeParseException) {
            LocalDate.now()
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val fechaSeleccionada = LocalDate.of(year, month + 1, dayOfMonth)
                etFecha.setText(fechaSeleccionada.format(formatter))
            },
            fechaActual.year,
            fechaActual.monthValue - 1,
            fechaActual.dayOfMonth
        ).show()
    }

    private fun fechaValida(fecha: String): Boolean {
        return try {
            LocalDate.parse(fecha, formatter)
            true
        } catch (_: DateTimeParseException) {
            false
        }
    }
}
