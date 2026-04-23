package com.example.courtcontrol

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CrearTorneo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_torneo)

        val etNombre = findViewById<EditText>(R.id.inputTorneoNombre)
        val etFecha = findViewById<EditText>(R.id.inputTorneoFecha)
        val etLugar = findViewById<EditText>(R.id.inputTorneoLugar)
        val etEstado = findViewById<EditText>(R.id.inputTorneoEstado)
        val btnCrear = findViewById<Button>(R.id.btnCrearTorneo)

        val db = DBHelper(this)

        btnCrear.setOnClickListener {

            val nombre = etNombre.text.toString().trim()
            val fecha = etFecha.text.toString().trim()
            val lugar = etLugar.text.toString().trim()
            val estado = etEstado.text.toString().trim()

            if (nombre.isEmpty()) {
                Toast.makeText(this, "Nombre vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ok = db.crearTorneo(nombre, fecha, lugar, estado)

            if (ok) {
                Toast.makeText(this, "Torneo creado", Toast.LENGTH_SHORT).show()

                setResult(RESULT_OK) // 👈 AVISA A VISTA PRINCIPAL
                finish()
            } else {
                Toast.makeText(this, "Error al crear torneo", Toast.LENGTH_SHORT).show()
            }
        }
    }
}