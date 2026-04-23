package com.example.courtcontrol

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvCrearCuenta = findViewById<TextView>(R.id.tvCrearCuenta)

        val db = DBHelper(this)


        tvCrearCuenta.setOnClickListener {
            startActivity(Intent(this, Crear_cuenta::class.java))
        }

        // LOGIN
        btnLogin.setOnClickListener {

            val usuarioTxt = etUsuario.text.toString().trim()
            val contraseña = etPassword.text.toString().trim()

            if (usuarioTxt.isEmpty() || contraseña.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val valido = db.validarLogin(usuarioTxt, contraseña)

            if (valido) {

                val usuario = db.obtenerUsuario(usuarioTxt)

                if (usuario != null) {
                    Toast.makeText(this, "Bienvenido ${usuario.usuario}", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, VistaPrincipal::class.java)
                    intent.putExtra("usuario_id", usuario.id_usuario)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}