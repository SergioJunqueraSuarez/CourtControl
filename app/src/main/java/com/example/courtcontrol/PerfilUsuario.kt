package com.example.courtcontrol

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PerfilUsuario : AppCompatActivity() {

    companion object {
        private const val PASSWORD_OCULTO = "Contrasena: ******"
        private const val PASSWORD_PREFIJO = "Contrasena: "
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_usuario)

        val tvNombre = findViewById<TextView>(R.id.tvNombreUsuario)
        val tvPassword = findViewById<TextView>(R.id.tvPasswordUsuario)
        val btnToggle = findViewById<Button>(R.id.btnTogglePassword)
        val etPassword = findViewById<EditText>(R.id.etPasswordNueva)
        val etConfirmar = findViewById<EditText>(R.id.etPasswordConfirmar)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarPassword)

        val usuarioId = intent.getIntExtra("usuario_id", -1)
        if (usuarioId == -1) {
            Toast.makeText(this, "Usuario no valido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = DBHelper(this)
        val usuario = runCatching { db.obtenerUsuarioPorId(usuarioId) }.getOrNull()

        if (usuario == null) {
            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvNombre.text = usuario.usuario
        tvPassword.text = PASSWORD_OCULTO

        var passwordActual = usuario.password
        var visible = false

        btnToggle.setOnClickListener {
            visible = !visible
            if (visible) {
                tvPassword.text = "$PASSWORD_PREFIJO$passwordActual"
                btnToggle.text = "Ocultar contrasena"
            } else {
                tvPassword.text = PASSWORD_OCULTO
                btnToggle.text = "Mostrar contrasena"
            }
        }

        btnGuardar.setOnClickListener {
            val nuevaPass = etPassword.text.toString().trim()
            val confirmar = etConfirmar.text.toString().trim()

            if (nuevaPass.isEmpty() || confirmar.isEmpty()) {
                Toast.makeText(this, "Rellena ambos campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nuevaPass != confirmar) {
                Toast.makeText(this, "Las contrasenas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ok = runCatching { db.actualizarPassword(usuarioId, nuevaPass) }.getOrDefault(false)
            if (!ok) {
                Toast.makeText(this, "No se pudo actualizar la contrasena", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Contrasena actualizada", Toast.LENGTH_SHORT).show()
            etPassword.setText("")
            etConfirmar.setText("")
            passwordActual = nuevaPass
            tvPassword.text = PASSWORD_OCULTO
            visible = false
            btnToggle.text = "Mostrar contrasena"
        }
    }
}
