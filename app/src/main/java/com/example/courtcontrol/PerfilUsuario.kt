package com.example.courtcontrol

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PerfilUsuario : AppCompatActivity() {

    companion object {
        private const val PASSWORD_OCULTO = "Contrasena protegida"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_usuario)

        val tvNombre = findViewById<TextView>(R.id.tvNombreUsuario)
        val tvPassword = findViewById<TextView>(R.id.tvPasswordUsuario)
        val etPassword = findViewById<EditText>(R.id.etPasswordNueva)
        val etConfirmar = findViewById<EditText>(R.id.etPasswordConfirmar)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarPassword)
        val tvTorneosPerfil = findViewById<TextView>(R.id.tvTorneosPerfil)

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
        tvTorneosPerfil.text = construirResumenTorneos(db.obtenerTorneosPerfil(usuarioId))

        btnGuardar.setOnClickListener {
            val nuevaPass = etPassword.text.toString().trim()
            val confirmar = etConfirmar.text.toString().trim()

            if (nuevaPass.isEmpty() || confirmar.isEmpty()) {
                Toast.makeText(this, "Rellena ambos campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!FormValidator.passwordValida(nuevaPass)) {
                etPassword.error = FormValidator.mensajePassword()
                return@setOnClickListener
            }

            if (nuevaPass != confirmar) {
                etConfirmar.error = "Las contrasenas no coinciden"
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
            tvPassword.text = PASSWORD_OCULTO
        }
    }

    private fun construirResumenTorneos(torneos: List<PerfilTorneo>): String {
        if (torneos.isEmpty()) {
            return "Todavia no estas inscrito en ningun torneo."
        }

        return torneos.joinToString("\n\n") { torneo ->
            "${torneo.nombre}\n${torneo.fecha} - ${torneo.lugar}\n${torneo.rolTorneo} - ${torneo.estado}"
        }
    }
}
