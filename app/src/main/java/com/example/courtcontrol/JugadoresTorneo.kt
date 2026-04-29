package com.example.courtcontrol

import android.app.AlertDialog
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class JugadoresTorneo : AppCompatActivity() {

    private lateinit var recyclerParticipantes: RecyclerView
    private lateinit var recyclerEquipos: RecyclerView
    private lateinit var recyclerDraft: RecyclerView
    private lateinit var recyclerPartidos: RecyclerView
    private lateinit var btnInscribirse: Button
    private lateinit var btnIniciarDraft: Button
    private lateinit var tvResumen: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvOrdenDraft: TextView
    private lateinit var tvTurnoDraft: TextView
    private lateinit var tvBracketVisual: TextView
    private lateinit var equiposAdapter: EquiposAdapter
    private lateinit var draftAdapter: DraftJugadoresAdapter
    private lateinit var partidosAdapter: PartidosAdapter

    private var idTorneo: Int = -1
    private var usuarioId: Int = -1
    private lateinit var db: DBHelper
    private var esAdmin: Boolean = false
    private var rolEnTorneo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jugadores_torneo)

        recyclerParticipantes = findViewById(R.id.recyclerJugadores)
        recyclerEquipos = findViewById(R.id.recyclerEquipos)
        recyclerDraft = findViewById(R.id.recyclerDraft)
        recyclerPartidos = findViewById(R.id.recyclerPartidos)
        btnInscribirse = findViewById(R.id.btnInscribirse)
        btnIniciarDraft = findViewById(R.id.btnIniciarDraft)
        tvResumen = findViewById(R.id.tvResumenTorneo)
        tvEstado = findViewById(R.id.tvEstadoTorneo)
        tvOrdenDraft = findViewById(R.id.tvOrdenDraft)
        tvTurnoDraft = findViewById(R.id.tvTurnoDraft)
        tvBracketVisual = findViewById(R.id.tvBracketVisual)

        idTorneo = intent.getIntExtra("id_torneo", -1)
        usuarioId = intent.getIntExtra("usuario_id", -1)

        if (idTorneo == -1 || usuarioId == -1) {
            Toast.makeText(this, "Error al abrir torneo", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db = DBHelper(this)
        esAdmin = db.obtenerRolPorId(usuarioId) == "admin"
        rolEnTorneo = db.obtenerRolInscripcion(usuarioId, idTorneo)

        if (!db.puedeVerDetalleTorneo(usuarioId, idTorneo)) {
            Toast.makeText(this, "Solo podras verlo cuando los equipos esten hechos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recyclerParticipantes.layoutManager = LinearLayoutManager(this)
        recyclerEquipos.layoutManager = LinearLayoutManager(this)
        recyclerDraft.layoutManager = LinearLayoutManager(this)
        recyclerPartidos.layoutManager = LinearLayoutManager(this)

        equiposAdapter = EquiposAdapter(emptyList())
        draftAdapter = DraftJugadoresAdapter(emptyList(), false) { jugador ->
            val ok = db.asignarJugadorDraft(idTorneo, usuarioId, jugador.id_usuario)
            if (ok) {
                Toast.makeText(this, "${jugador.usuario} asignado", Toast.LENGTH_SHORT).show()
                cargarPantalla()
            } else {
                Toast.makeText(this, "No te toca elegir ahora", Toast.LENGTH_SHORT).show()
            }
        }
        partidosAdapter = PartidosAdapter(emptyList(), esAdmin) { partido ->
            mostrarDialogoGanador(partido)
        }

        recyclerEquipos.adapter = equiposAdapter
        recyclerDraft.adapter = draftAdapter
        recyclerPartidos.adapter = partidosAdapter

        btnInscribirse.setOnClickListener { gestionarInscripcion() }
        btnIniciarDraft.setOnClickListener {
            if (!esAdmin) {
                return@setOnClickListener
            }

            val ok = if (db.draftIniciado(idTorneo)) {
                false
            } else {
                db.iniciarDraft(idTorneo)
            }

            if (ok) {
                Toast.makeText(this, "Draft iniciado", Toast.LENGTH_SHORT).show()
                cargarPantalla()
            } else {
                Toast.makeText(this, "Hace falta torneo completo y 4 capitanes", Toast.LENGTH_SHORT).show()
            }
        }

        cargarPantalla()
    }

    override fun onResume() {
        super.onResume()
        if (::db.isInitialized) {
            esAdmin = db.obtenerRolPorId(usuarioId) == "admin"
            rolEnTorneo = db.obtenerRolInscripcion(usuarioId, idTorneo)
            if (!db.puedeVerDetalleTorneo(usuarioId, idTorneo)) {
                finish()
                return
            }
            cargarPantalla()
        }
    }

    private fun cargarPantalla() {
        val torneo = db.obtenerTorneoPorId(idTorneo) ?: run {
            finish()
            return
        }
        val jugadores = db.obtenerUsuariosPorTorneo(idTorneo)
        val equipos = db.obtenerEquiposDelTorneo(idTorneo)
        val partidos = db.obtenerPartidosTorneo(idTorneo)
        val ordenDraft = db.obtenerOrdenDraft(idTorneo)
        val disponiblesDraft = db.obtenerJugadoresDisponiblesDraft(idTorneo)
        val capitanActual = db.obtenerCapitanActualDraft(idTorneo)
        val draftIniciado = db.draftIniciado(idTorneo)
        val draftCompletado = db.draftCompletado(idTorneo)

        recyclerParticipantes.adapter = UsuariosAdapter(jugadores)
        equiposAdapter.actualizarLista(equipos)
        partidosAdapter = PartidosAdapter(partidos, esAdmin) { partido ->
            mostrarDialogoGanador(partido)
        }
        recyclerPartidos.adapter = partidosAdapter

        val puedeElegir = capitanActual?.id_usuario == usuarioId || esAdmin
        draftAdapter = DraftJugadoresAdapter(disponiblesDraft, puedeElegir) { jugador ->
            val ok = db.asignarJugadorDraft(idTorneo, usuarioId, jugador.id_usuario)
            if (ok) {
                Toast.makeText(this, "${jugador.usuario} asignado", Toast.LENGTH_SHORT).show()
                cargarPantalla()
            } else {
                Toast.makeText(this, "No te toca elegir ahora", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerDraft.adapter = draftAdapter

        tvResumen.text =
            "Participantes: ${db.contarInscritos(idTorneo)}/${DBHelper.MAX_JUGADORES_POR_TORNEO} | Capitanes: ${db.contarCapitanes(idTorneo)}/${DBHelper.MAX_EQUIPOS_POR_TORNEO}"
        tvEstado.text = "Estado: ${torneo.estado}"

        tvOrdenDraft.text = if (ordenDraft.isEmpty()) {
            "Orden del draft pendiente"
        } else {
            "Orden del draft: " + ordenDraft.mapIndexed { index, usuario ->
                "${index + 1}. ${usuario.usuario}"
            }.joinToString(" | ")
        }

        tvTurnoDraft.text = when {
            !draftIniciado -> "El draft aun no ha empezado"
            draftCompletado -> "Draft completado"
            capitanActual != null -> "Turno actual: ${capitanActual.usuario}"
            else -> "Turno actual pendiente"
        }

        btnIniciarDraft.isVisible = esAdmin
        btnIniciarDraft.isEnabled = db.puedeIniciarDraft(idTorneo)
        btnIniciarDraft.text = if (draftIniciado) "Draft iniciado" else "Iniciar draft"

        btnInscribirse.isVisible = !draftIniciado && rolEnTorneo == null && torneo.estado == DBHelper.ESTADO_ABIERTO

        findViewById<TextView>(R.id.tvTituloParticipantes).isVisible = esAdmin || rolEnTorneo == "CAPITAN" || !draftCompletado
        recyclerParticipantes.isVisible = findViewById<TextView>(R.id.tvTituloParticipantes).isVisible

        findViewById<TextView>(R.id.tvTituloEquipos).isVisible = equipos.isNotEmpty()
        recyclerEquipos.isVisible = equipos.isNotEmpty()

        findViewById<TextView>(R.id.tvTituloDraft).isVisible = draftIniciado && !draftCompletado
        tvOrdenDraft.isVisible = draftIniciado
        tvTurnoDraft.isVisible = draftIniciado
        recyclerDraft.isVisible = draftIniciado && !draftCompletado

        findViewById<TextView>(R.id.tvTituloPartidos).isVisible = partidos.isNotEmpty()
        findViewById<TextView>(R.id.tvSubtituloPartidos).isVisible = partidos.isNotEmpty()
        tvBracketVisual.isVisible = partidos.isNotEmpty()
        tvBracketVisual.text = construirBracketVisual(partidos)
        recyclerPartidos.isVisible = partidos.isNotEmpty()

        actualizarProgreso(torneo.estado, draftIniciado, draftCompletado, equipos.isNotEmpty(), partidos)
    }

    private fun gestionarInscripcion() {
        val inscritos = db.contarInscritos(idTorneo)

        if (inscritos >= DBHelper.MAX_JUGADORES_POR_TORNEO) {
            Toast.makeText(this, "El torneo esta lleno", Toast.LENGTH_SHORT).show()
            return
        }

        if (db.yaInscrito(usuarioId, idTorneo)) {
            Toast.makeText(this, "Ya estas inscrito", Toast.LENGTH_SHORT).show()
            return
        }

        if (db.debeForzarseCapitan(idTorneo)) {
            mostrarDialogoInscripcion("Solo quedan plazas de capitan", arrayOf("Capitan"))
            return
        }

        if (db.plazasDeCapitanCompletas(idTorneo)) {
            mostrarDialogoInscripcion("Capitanes completos", arrayOf("Jugador"))
            return
        }

        mostrarDialogoInscripcion("Como quieres inscribirte?", arrayOf("Capitan", "Jugador"))
    }

    private fun mostrarDialogoInscripcion(titulo: String, opciones: Array<String>) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setItems(opciones) { _, which ->
                val tipo = if (opciones[which] == "Capitan") "CAPITAN" else "JUGADOR"
                val ok = db.inscribirUsuario(usuarioId, idTorneo, tipo)
                if (ok) {
                    Toast.makeText(this, "Inscrito como $tipo", Toast.LENGTH_SHORT).show()
                    rolEnTorneo = tipo
                    cargarPantalla()
                } else {
                    Toast.makeText(this, "Error al inscribirse", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoGanador(partido: PartidoTorneo) {
        if (!esAdmin) {
            return
        }

        val opciones = arrayOf(partido.equipo1, partido.equipo2)
        val ids = arrayOf(partido.equipo1Id, partido.equipo2Id)
        val inputResultado = EditText(this).apply {
            hint = "Marcador o resultado, ej. 21-18"
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_DONE
            setText(partido.resultado.orEmpty())
            setPadding(32, 16, 32, 16)
        }
        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 8, 32, 0)
            addView(inputResultado)
        }

        AlertDialog.Builder(this)
            .setTitle(if (partido.ganador == null) "Registrar resultado" else "Corregir resultado")
            .setView(contenedor)
            .setItems(opciones) { _, which ->
                val resultado = inputResultado.text.toString().trim().ifBlank {
                    "${opciones[which]} gana"
                }
                val ok = db.registrarResultadoPartido(
                    idPartido = partido.id,
                    idGanador = ids[which],
                    resultado = resultado,
                    permitirActualizar = true
                )
                if (ok) {
                    Toast.makeText(this, "Resultado guardado", Toast.LENGTH_SHORT).show()
                    cargarPantalla()
                } else {
                    Toast.makeText(this, "No se pudo guardar el resultado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarProgreso(
        estado: String,
        draftIniciado: Boolean,
        draftCompletado: Boolean,
        equiposCreados: Boolean,
        partidos: List<PartidoTorneo>
    ) {
        val finalCreada = partidos.any { it.ronda == 2 }
        val pasos = listOf(
            findViewById<TextView>(R.id.tvPasoInscripcion) to true,
            findViewById<TextView>(R.id.tvPasoDraft) to draftIniciado,
            findViewById<TextView>(R.id.tvPasoEquipos) to (equiposCreados || draftCompletado),
            findViewById<TextView>(R.id.tvPasoPartidos) to partidos.isNotEmpty(),
            findViewById<TextView>(R.id.tvPasoFinal) to (finalCreada || estado == DBHelper.ESTADO_FINALIZADO)
        )

        pasos.forEach { (view, activo) ->
            view.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    if (activo) R.color.cc_primary else R.color.cc_surface_alt
                )
            )
            view.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (activo) R.color.white else R.color.cc_text_secondary
                )
            )
        }
    }

    private fun construirBracketVisual(partidos: List<PartidoTorneo>): String {
        val semifinales = partidos.filter { it.ronda == 1 }.sortedBy { it.orden }
        val final = partidos.firstOrNull { it.ronda == 2 }

        val semiTexto = semifinales.joinToString("\n\n") { partido ->
            val ganador = partido.ganador ?: "pendiente"
            "SF${partido.orden}: ${partido.equipo1} vs ${partido.equipo2}\nResultado: ${partido.resultado ?: "-"}\nAvanza: $ganador"
        }

        val finalTexto = final?.let {
            "\n\nFINAL: ${it.equipo1} vs ${it.equipo2}\nResultado: ${it.resultado ?: "-"}\nCampeon: ${it.ganador ?: "pendiente"}"
        } ?: "\n\nFINAL: pendiente de semifinales"

        return semiTexto + finalTexto
    }
}
