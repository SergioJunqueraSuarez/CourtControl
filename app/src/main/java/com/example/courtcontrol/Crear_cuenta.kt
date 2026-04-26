package com.example.courtcontrol

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Crear_cuenta : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.crear_cuenta)

        val etUsuario = findViewById<EditText>(R.id.etUsuarioRegistro)
        val etPassword = findViewById<EditText>(R.id.etPasswordRegistro)
        val etRepetir = findViewById<EditText>(R.id.etRepetirPassword)
        val btnCrear = findViewById<Button>(R.id.btnCrearCuenta)

        val db = DBHelper(this)

        btnCrear.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val repetir = etRepetir.text.toString().trim()

            if (usuario.isEmpty() || password.isEmpty() || repetir.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != repetir) {
                Toast.makeText(this, "Las contrasenas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (runCatching { db.usuarioExiste(usuario) }.getOrDefault(false)) {
                Toast.makeText(this, "El usuario ya existe", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ok = runCatching {
                db.insertarUsuario(
                usuario = usuario,
                password = password,
                rol = "usuario"
                )
            }.getOrDefault(false)

            if (ok) {
                Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Error al crear la cuenta", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
