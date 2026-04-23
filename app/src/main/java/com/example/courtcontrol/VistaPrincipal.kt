package com.example.courtcontrol

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class VistaPrincipal : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vista_principal)

        val usuarioId = intent.getIntExtra("usuario_id", -1)

        if (usuarioId == -1) {
            Toast.makeText(this, "Error usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = DBHelper(this)

        val rol = db.obtenerRolPorId(usuarioId)

        val btnAdmin = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btnAdmin)

        btnAdmin.visibility = if (rol == "admin") View.VISIBLE else View.GONE

        btnAdmin.setOnClickListener {
            startActivity(Intent(this, CrearTorneo::class.java))
        }

        val ivPerfil = findViewById<ImageView>(R.id.ivPerfil)
        ivPerfil.setOnClickListener {
            val intent = Intent(this, PerfilUsuario::class.java)
            intent.putExtra("usuario_id", usuarioId)
            startActivity(intent)
        }

        val rvTorneos = findViewById<RecyclerView>(R.id.rvTorneos)
        rvTorneos.layoutManager = LinearLayoutManager(this)

        val torneos = db.obtenerTorneos()

        rvTorneos.adapter = TorneosAdapter(torneos, usuarioId, db)
    }
}