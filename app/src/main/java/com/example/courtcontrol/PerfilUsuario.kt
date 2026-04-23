package com.example.courtcontrol

import android.database.Cursor
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class PerfilUsuario : AppCompatActivity() {

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
            Toast.makeText(this, "Usuario no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = DBHelper(this)

        // 🔹 Cargar datos
        val cursor: Cursor = db.readableDatabase.rawQuery(
            "SELECT usuario, contraseña FROM Usuarios WHERE id_usuario = ?",
            arrayOf(usuarioId.toString())
        )

        var passwordActual = ""

        if (cursor.moveToFirst()) {
            val nombre = cursor.getString(0)
            passwordActual = cursor.getString(1)

            tvNombre.text = nombre
            tvPassword.text = "Contraseña: ******"
        } else {
            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }

        cursor.close()

        // 🔥 Mostrar / ocultar contraseña
        var visible = false
        btnToggle.setOnClickListener {
            visible = !visible
            if (visible) {
                tvPassword.text = "Contraseña: $passwordActual"
                btnToggle.text = "Ocultar contraseña"
            } else {
                tvPassword.text = "Contraseña: ******"
                btnToggle.text = "Mostrar contraseña"
            }
        }

        // Guardar nueva contraseña
        btnGuardar.setOnClickListener {

            val nuevaPass = etPassword.text.toString().trim()
            val confirmar = etConfirmar.text.toString().trim()

            if (nuevaPass.isEmpty() || confirmar.isEmpty()) {
                Toast.makeText(this, "Rellena ambos campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nuevaPass != confirmar) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.writableDatabase.execSQL(
                "UPDATE Usuarios SET contraseña = ? WHERE id_usuario = ?",
                arrayOf(nuevaPass, usuarioId.toString())
            )

            Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show()

            // limpiar campos
            etPassword.setText("")
            etConfirmar.setText("")

            // actualizar variable
            passwordActual = nuevaPass
            tvPassword.text = "Contraseña: ******"
            visible = false
            btnToggle.text = "Mostrar contraseña"
        }
    }
}