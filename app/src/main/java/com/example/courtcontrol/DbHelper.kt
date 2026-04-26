package com.example.courtcontrol

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "CourtControlDB"
        private const val DATABASE_VERSION = 6
        private const val PASSWORD_COLUMN = "password"
        private const val LEGACY_PASSWORD_COLUMN_1 = "contrase\u00f1a"
        private const val LEGACY_PASSWORD_COLUMN_2 = "contrase\u00c3\u00b1a"

        const val MAX_EQUIPOS_POR_TORNEO = 4
        const val MAX_JUGADORES_POR_TORNEO = 20

        const val ESTADO_ABIERTO = "Abierto"
        const val ESTADO_CERRADO = "Cerrado"
        const val ESTADO_EN_JUEGO = "En juego"
        const val ESTADO_FINALIZADO = "Finalizado"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            """
            CREATE TABLE Usuarios (
                id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario TEXT NOT NULL UNIQUE,
                $PASSWORD_COLUMN TEXT NOT NULL,
                rol TEXT NOT NULL
            )
            """.trimIndent()
        )

        db?.execSQL(
            """
            INSERT INTO Usuarios (usuario, $PASSWORD_COLUMN, rol)
            VALUES ('admin', 'admin', 'admin')
            """.trimIndent()
        )

        db?.execSQL(
            """
            CREATE TABLE Torneos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                fecha TEXT,
                lugar TEXT,
                estado TEXT
            )
            """.trimIndent()
        )

        db?.execSQL(
            """
            CREATE TABLE Equipos (
                id_equipo INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                id_torneo INTEGER NOT NULL,
                id_capitan INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db?.execSQL(
            """
            CREATE TABLE Inscripciones (
                id_inscripcion INTEGER PRIMARY KEY AUTOINCREMENT,
                id_usuario INTEGER NOT NULL,
                id_torneo INTEGER NOT NULL,
                rol_torneo TEXT NOT NULL,
                FOREIGN KEY(id_usuario) REFERENCES Usuarios(id_usuario),
                FOREIGN KEY(id_torneo) REFERENCES Torneos(id)
            )
            """.trimIndent()
        )

        db?.execSQL(
            """
            CREATE TABLE Partidos (
                id_partido INTEGER PRIMARY KEY AUTOINCREMENT,
                id_torneo INTEGER NOT NULL,
                equipo1 INTEGER NOT NULL,
                equipo2 INTEGER NOT NULL,
                resultado TEXT,
                ganador INTEGER,
                ronda INTEGER NOT NULL DEFAULT 1,
                orden INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )

        crearTablaEquipoMiembros(db)
        crearTablaDraftOrden(db)
        insertarTorneosIniciales(db)
        insertarDatosPrueba(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            crearTablaEquipoMiembros(db)
        }
        if (oldVersion < 4) {
            crearTablaDraftOrden(db)
            intentarExecSQL(db, "ALTER TABLE Partidos ADD COLUMN ronda INTEGER NOT NULL DEFAULT 1")
            intentarExecSQL(db, "ALTER TABLE Partidos ADD COLUMN orden INTEGER NOT NULL DEFAULT 1")
        }
        if (oldVersion < 5) {
            insertarDatosPrueba(db)
        }
        if (oldVersion < 6) {
            asegurarColumnaPassword(db)
        }
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        asegurarColumnaPassword(db)
        insertarDatosPrueba(db)
    }

    private fun intentarExecSQL(db: SQLiteDatabase?, sql: String) {
        try {
            db?.execSQL(sql)
        } catch (_: Exception) {
        }
    }

    private fun crearTablaEquipoMiembros(db: SQLiteDatabase?) {
        db?.execSQL(
            """
            CREATE TABLE IF NOT EXISTS EquipoMiembros (
                id_equipo INTEGER NOT NULL,
                id_usuario INTEGER NOT NULL,
                PRIMARY KEY(id_equipo, id_usuario),
                FOREIGN KEY(id_equipo) REFERENCES Equipos(id_equipo),
                FOREIGN KEY(id_usuario) REFERENCES Usuarios(id_usuario)
            )
            """.trimIndent()
        )
    }

    private fun crearTablaDraftOrden(db: SQLiteDatabase?) {
        db?.execSQL(
            """
            CREATE TABLE IF NOT EXISTS DraftOrden (
                id_torneo INTEGER NOT NULL,
                posicion INTEGER NOT NULL,
                id_capitan INTEGER NOT NULL,
                PRIMARY KEY(id_torneo, posicion),
                FOREIGN KEY(id_torneo) REFERENCES Torneos(id),
                FOREIGN KEY(id_capitan) REFERENCES Usuarios(id_usuario)
            )
            """.trimIndent()
        )
    }

    private fun asegurarColumnaPassword(db: SQLiteDatabase?) {
        if (db == null) {
            return
        }

        val columnas = mutableSetOf<String>()
        val cursor = db.rawQuery("PRAGMA table_info(Usuarios)", null)
        if (cursor.moveToFirst()) {
            do {
                columnas.add(cursor.getString(1))
            } while (cursor.moveToNext())
        }
        cursor.close()

        if (!columnas.contains(PASSWORD_COLUMN)) {
            intentarExecSQL(db, "ALTER TABLE Usuarios ADD COLUMN $PASSWORD_COLUMN TEXT")
        }

        if (columnas.contains(LEGACY_PASSWORD_COLUMN_1)) {
            intentarExecSQL(
                db,
                "UPDATE Usuarios SET $PASSWORD_COLUMN = $LEGACY_PASSWORD_COLUMN_1 WHERE $PASSWORD_COLUMN IS NULL"
            )
        } else if (columnas.contains(LEGACY_PASSWORD_COLUMN_2)) {
            intentarExecSQL(
                db,
                "UPDATE Usuarios SET $PASSWORD_COLUMN = $LEGACY_PASSWORD_COLUMN_2 WHERE $PASSWORD_COLUMN IS NULL"
            )
        } else {
            intentarExecSQL(
                db,
                "UPDATE Usuarios SET $PASSWORD_COLUMN = '1234' WHERE $PASSWORD_COLUMN IS NULL"
            )
        }
    }

    private fun insertarTorneosIniciales(db: SQLiteDatabase?) {
        val cursor = db?.rawQuery("SELECT COUNT(*) FROM Torneos", null)
        cursor?.moveToFirst()
        val count = cursor?.getInt(0) ?: 0
        cursor?.close()

        if (count == 0) {
            db?.execSQL(
                """
                INSERT INTO Torneos (nombre, fecha, lugar, estado)
                VALUES ('Torneo Verano', '2026-06-12', 'Madrid', '$ESTADO_ABIERTO')
                """.trimIndent()
            )
            db?.execSQL(
                """
                INSERT INTO Torneos (nombre, fecha, lugar, estado)
                VALUES ('Torneo Invierno', '2026-12-01', 'Barcelona', '$ESTADO_ABIERTO')
                """.trimIndent()
            )
            db?.execSQL(
                """
                INSERT INTO Torneos (nombre, fecha, lugar, estado)
                VALUES ('Torneo Primavera', '2026-03-20', 'Valencia', '$ESTADO_ABIERTO')
                """.trimIndent()
            )
        }
    }

    // pruebas
    private fun insertarDatosPrueba(db: SQLiteDatabase?) {
        if (db == null) {
            return
        }

        val cursorUsuarios = db.rawQuery("SELECT COUNT(*) FROM Usuarios", null)
        cursorUsuarios.moveToFirst()
        val totalUsuarios = cursorUsuarios.getInt(0)
        cursorUsuarios.close()

        if (totalUsuarios < 51) {
            for (i in 1..50) {
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO Usuarios (usuario, $PASSWORD_COLUMN, rol)
                    VALUES (?, ?, 'usuario')
                    """.trimIndent(),
                    arrayOf("usuario$i", "1234")
                )
            }
        }

        val cursorInscripciones = db.rawQuery("SELECT COUNT(*) FROM Inscripciones", null)
        cursorInscripciones.moveToFirst()
        val totalInscripciones = cursorInscripciones.getInt(0)
        cursorInscripciones.close()

        if (totalInscripciones > 0) {
            return
        }

        val torneos = mutableListOf<Int>()
        val torneosCursor = db.rawQuery("SELECT id FROM Torneos ORDER BY id ASC", null)
        if (torneosCursor.moveToFirst()) {
            do {
                torneos.add(torneosCursor.getInt(0))
            } while (torneosCursor.moveToNext())
        }
        torneosCursor.close()

        if (torneos.isEmpty()) {
            return
        }

        val usuarios = mutableListOf<Int>()
        val usuariosCursor = db.rawQuery(
            "SELECT id_usuario FROM Usuarios WHERE usuario != 'admin' ORDER BY id_usuario ASC",
            null
        )
        if (usuariosCursor.moveToFirst()) {
            do {
                usuarios.add(usuariosCursor.getInt(0))
            } while (usuariosCursor.moveToNext())
        }
        usuariosCursor.close()

        val cuposPorTorneo = listOf(20, 20, 10)
        var indiceUsuario = 0

        torneos.forEachIndexed { index, idTorneo ->
            val cupo = cuposPorTorneo.getOrElse(index) { 8 }
            val totalParaTorneo = minOf(cupo, usuarios.size - indiceUsuario)
            if (totalParaTorneo <= 0) {
                return@forEachIndexed
            }

            for (offset in 0 until totalParaTorneo) {
                val idUsuario = usuarios[indiceUsuario + offset]
                val rolTorneo = if (offset < MAX_EQUIPOS_POR_TORNEO) "CAPITAN" else "JUGADOR"
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO Inscripciones (id_usuario, id_torneo, rol_torneo)
                    VALUES (?, ?, ?)
                    """.trimIndent(),
                    arrayOf(idUsuario, idTorneo, rolTorneo)
                )
            }

            val estado = when {
                totalParaTorneo >= MAX_JUGADORES_POR_TORNEO -> ESTADO_CERRADO
                else -> ESTADO_ABIERTO
            }
            db.execSQL(
                "UPDATE Torneos SET estado = ? WHERE id = ?",
                arrayOf(estado, idTorneo)
            )

            indiceUsuario += totalParaTorneo
        }
    }

    fun insertarUsuario(usuario: String, password: String, rol: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("usuario", usuario)
            put(PASSWORD_COLUMN, password)
            put("rol", rol)
        }
        val result = db.insert("Usuarios", null, values)
        db.close()
        return result != -1L
    }

    fun usuarioExiste(usuario: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id_usuario FROM Usuarios WHERE usuario = ?",
            arrayOf(usuario)
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    fun validarLogin(usuario: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id_usuario FROM Usuarios WHERE usuario = ? AND $PASSWORD_COLUMN = ?",
            arrayOf(usuario, password)
        )
        val ok = cursor.moveToFirst()
        cursor.close()
        db.close()
        return ok
    }

    fun obtenerUsuario(usuario: String): Usuario? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id_usuario, usuario, $PASSWORD_COLUMN, rol FROM Usuarios WHERE usuario = ?",
            arrayOf(usuario)
        )
        val user = if (cursor.moveToFirst()) {
            Usuario(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3))
        } else {
            null
        }
        cursor.close()
        db.close()
        return user
    }

    fun obtenerUsuarioPorId(idUsuario: Int): Usuario? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id_usuario, usuario, $PASSWORD_COLUMN, rol FROM Usuarios WHERE id_usuario = ?",
            arrayOf(idUsuario.toString())
        )
        val user = if (cursor.moveToFirst()) {
            Usuario(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3))
        } else {
            null
        }
        cursor.close()
        db.close()
        return user
    }

    fun obtenerRolPorId(idUsuario: Int): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT rol FROM Usuarios WHERE id_usuario = ?",
            arrayOf(idUsuario.toString())
        )
        val rol = if (cursor.moveToFirst()) cursor.getString(0) else null
        cursor.close()
        db.close()
        return rol
    }

    fun actualizarPassword(idUsuario: Int, nuevaPassword: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(PASSWORD_COLUMN, nuevaPassword)
        }
        val result = db.update("Usuarios", values, "id_usuario = ?", arrayOf(idUsuario.toString()))
        db.close()
        return result > 0
    }

    fun obtenerTorneos(): List<Torneo> {
        val lista = mutableListOf<Torneo>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, nombre, fecha, lugar, estado FROM Torneos", null)
        if (cursor.moveToFirst()) {
            do {
                lista.add(Torneo(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun obtenerTorneosVisibles(idUsuario: Int): List<Torneo> {
        val esAdmin = obtenerRolPorId(idUsuario) == "admin"
        return obtenerTorneos().filter { torneo ->
            val abiertoATodos = torneo.estado == ESTADO_EN_JUEGO || torneo.estado == ESTADO_FINALIZADO
            val estaLleno = contarInscritos(torneo.id) >= MAX_JUGADORES_POR_TORNEO
            !estaLleno || abiertoATodos || esAdmin || yaInscrito(idUsuario, torneo.id)
        }
    }

    fun obtenerTorneoPorId(idTorneo: Int): Torneo? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, nombre, fecha, lugar, estado FROM Torneos WHERE id = ?",
            arrayOf(idTorneo.toString())
        )
        val torneo = if (cursor.moveToFirst()) {
            Torneo(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4))
        } else {
            null
        }
        cursor.close()
        db.close()
        return torneo
    }

    fun actualizarEstadoTorneo(idTorneo: Int, estado: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply { put("estado", estado) }
        val result = db.update("Torneos", values, "id = ?", arrayOf(idTorneo.toString()))
        db.close()
        return result > 0
    }

    fun obtenerUsuariosPorTorneo(idTorneo: Int): List<UsuarioTorneo> {
        val lista = mutableListOf<UsuarioTorneo>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT U.usuario, I.rol_torneo
            FROM Inscripciones I
            INNER JOIN Usuarios U ON I.id_usuario = U.id_usuario
            WHERE I.id_torneo = ?
            ORDER BY CASE WHEN I.rol_torneo = 'CAPITAN' THEN 0 ELSE 1 END, I.id_inscripcion ASC
            """.trimIndent(),
            arrayOf(idTorneo.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                lista.add(UsuarioTorneo(cursor.getString(0), cursor.getString(1)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun obtenerRolInscripcion(idUsuario: Int, idTorneo: Int): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT rol_torneo FROM Inscripciones WHERE id_usuario = ? AND id_torneo = ?",
            arrayOf(idUsuario.toString(), idTorneo.toString())
        )
        val rol = if (cursor.moveToFirst()) cursor.getString(0) else null
        cursor.close()
        db.close()
        return rol
    }

    fun puedeVerDetalleTorneo(idUsuario: Int, idTorneo: Int): Boolean {
        val estado = obtenerTorneoPorId(idTorneo)?.estado
        if (obtenerRolPorId(idUsuario) == "admin") {
            return true
        }
        if (estado == ESTADO_EN_JUEGO || estado == ESTADO_FINALIZADO) {
            return true
        }
        val rolInscripcion = obtenerRolInscripcion(idUsuario, idTorneo) ?: return contarInscritos(idTorneo) < MAX_JUGADORES_POR_TORNEO
        return rolInscripcion == "CAPITAN"
    }

    fun contarInscritos(idTorneo: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM Inscripciones WHERE id_torneo = ?", arrayOf(idTorneo.toString()))
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    fun contarCapitanes(idTorneo: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM Inscripciones WHERE id_torneo = ? AND rol_torneo = ?",
            arrayOf(idTorneo.toString(), "CAPITAN")
        )
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    fun plazasRestantes(idTorneo: Int): Int = maxOf(0, MAX_JUGADORES_POR_TORNEO - contarInscritos(idTorneo))

    fun capitanesRestantes(idTorneo: Int): Int = maxOf(0, MAX_EQUIPOS_POR_TORNEO - contarCapitanes(idTorneo))

    fun plazasDeCapitanCompletas(idTorneo: Int): Boolean = contarCapitanes(idTorneo) >= MAX_EQUIPOS_POR_TORNEO

    fun debeForzarseCapitan(idTorneo: Int): Boolean {
        val plazasRestantes = plazasRestantes(idTorneo)
        val capitanesRestantes = capitanesRestantes(idTorneo)
        return capitanesRestantes > 0 && plazasRestantes == capitanesRestantes
    }

    fun yaInscrito(idUsuario: Int, idTorneo: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id_inscripcion FROM Inscripciones WHERE id_usuario = ? AND id_torneo = ?",
            arrayOf(idUsuario.toString(), idTorneo.toString())
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    fun inscribirUsuario(idUsuario: Int, idTorneo: Int, tipo: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id_usuario", idUsuario)
            put("id_torneo", idTorneo)
            put("rol_torneo", tipo)
        }
        val result = db.insert("Inscripciones", null, values)
        db.close()
        if (result != -1L) {
            if (contarInscritos(idTorneo) >= MAX_JUGADORES_POR_TORNEO) {
                actualizarEstadoTorneo(idTorneo, ESTADO_CERRADO)
            } else {
                actualizarEstadoTorneo(idTorneo, ESTADO_ABIERTO)
            }
        }
        return result != -1L
    }

    fun crearTorneo(nombre: String, fecha: String, lugar: String, estado: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("fecha", fecha)
            put("lugar", lugar)
            put("estado", estado)
        }
        val result = db.insert("Torneos", null, values)
        db.close()
        return result != -1L
    }

    fun actualizarTorneo(idTorneo: Int, nombre: String, fecha: String, lugar: String, estado: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("fecha", fecha)
            put("lugar", lugar)
            put("estado", estado)
        }
        val result = db.update("Torneos", values, "id = ?", arrayOf(idTorneo.toString()))
        db.close()
        return result > 0
    }

    fun borrarTorneo(idTorneo: Int): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            db.delete("Partidos", "id_torneo = ?", arrayOf(idTorneo.toString()))
            db.delete("DraftOrden", "id_torneo = ?", arrayOf(idTorneo.toString()))
            db.delete("EquipoMiembros", "id_equipo IN (SELECT id_equipo FROM Equipos WHERE id_torneo = ?)", arrayOf(idTorneo.toString()))
            db.delete("Equipos", "id_torneo = ?", arrayOf(idTorneo.toString()))
            db.delete("Inscripciones", "id_torneo = ?", arrayOf(idTorneo.toString()))
            val eliminados = db.delete("Torneos", "id = ?", arrayOf(idTorneo.toString()))
            db.setTransactionSuccessful()
            eliminados > 0
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun contarInscritosPorTorneo(idTorneo: Int): Int = contarInscritos(idTorneo)

    fun obtenerUsuariosNoAdmin(): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id_usuario, usuario, $PASSWORD_COLUMN, rol FROM Usuarios WHERE rol != ? ORDER BY usuario ASC",
            arrayOf("admin")
        )
        if (cursor.moveToFirst()) {
            do {
                lista.add(Usuario(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun actualizarRolUsuario(idUsuario: Int, nuevoRol: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply { put("rol", nuevoRol) }
        val result = db.update("Usuarios", values, "id_usuario = ?", arrayOf(idUsuario.toString()))
        db.close()
        return result > 0
    }

    fun equiposGenerados(idTorneo: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM Equipos WHERE id_torneo = ?", arrayOf(idTorneo.toString()))
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count > 0
    }

    fun draftIniciado(idTorneo: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM DraftOrden WHERE id_torneo = ?", arrayOf(idTorneo.toString()))
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count == MAX_EQUIPOS_POR_TORNEO
    }

    fun draftCompletado(idTorneo: Int): Boolean {
        return draftIniciado(idTorneo) && obtenerJugadoresDisponiblesDraft(idTorneo).isEmpty()
    }

    fun puedeIniciarDraft(idTorneo: Int): Boolean {
        return !draftIniciado(idTorneo) &&
            contarInscritos(idTorneo) == MAX_JUGADORES_POR_TORNEO &&
            contarCapitanes(idTorneo) == MAX_EQUIPOS_POR_TORNEO
    }

    fun iniciarDraft(idTorneo: Int): Boolean {
        if (!puedeIniciarDraft(idTorneo)) {
            return false
        }

        val capitanes = obtenerUsuariosPorRolTorneo(idTorneo, "CAPITAN").shuffled()
        val db = writableDatabase
        db.beginTransaction()

        return try {
            capitanes.forEachIndexed { index, capitan ->
                val equipoValues = ContentValues().apply {
                    put("nombre", capitan.usuario)
                    put("id_torneo", idTorneo)
                    put("id_capitan", capitan.id_usuario)
                }
                val equipoId = db.insert("Equipos", null, equipoValues)
                if (equipoId == -1L) {
                    throw IllegalStateException("No se pudo crear el equipo")
                }

                val miembroCapitanValues = ContentValues().apply {
                    put("id_equipo", equipoId)
                    put("id_usuario", capitan.id_usuario)
                }
                db.insert("EquipoMiembros", null, miembroCapitanValues)

                val draftValues = ContentValues().apply {
                    put("id_torneo", idTorneo)
                    put("posicion", index + 1)
                    put("id_capitan", capitan.id_usuario)
                }
                db.insert("DraftOrden", null, draftValues)
            }

            val estadoValues = ContentValues().apply { put("estado", ESTADO_CERRADO) }
            db.update("Torneos", estadoValues, "id = ?", arrayOf(idTorneo.toString()))

            db.setTransactionSuccessful()
            true
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun obtenerOrdenDraft(idTorneo: Int): List<Usuario> {
        val usuarios = mutableListOf<Usuario>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT U.id_usuario, U.usuario, U.$PASSWORD_COLUMN, U.rol
            FROM DraftOrden D
            INNER JOIN Usuarios U ON U.id_usuario = D.id_capitan
            WHERE D.id_torneo = ?
            ORDER BY D.posicion ASC
            """.trimIndent(),
            arrayOf(idTorneo.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                usuarios.add(Usuario(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return usuarios
    }

    fun obtenerCapitanActualDraft(idTorneo: Int): Usuario? {
        val orden = obtenerOrdenDraft(idTorneo)
        if (orden.isEmpty()) {
            return null
        }
        val picksHechos = contarJugadoresAsignadosEnDraft(idTorneo)
        return orden[picksHechos % orden.size]
    }

    fun obtenerJugadoresDisponiblesDraft(idTorneo: Int): List<Usuario> {
        val usuarios = mutableListOf<Usuario>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT U.id_usuario, U.usuario, U.$PASSWORD_COLUMN, U.rol
            FROM Inscripciones I
            INNER JOIN Usuarios U ON U.id_usuario = I.id_usuario
            WHERE I.id_torneo = ? AND I.rol_torneo = 'JUGADOR'
            AND U.id_usuario NOT IN (
                SELECT EM.id_usuario
                FROM EquipoMiembros EM
                INNER JOIN Equipos E ON E.id_equipo = EM.id_equipo
                WHERE E.id_torneo = ?
            )
            ORDER BY U.usuario ASC
            """.trimIndent(),
            arrayOf(idTorneo.toString(), idTorneo.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                usuarios.add(Usuario(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return usuarios
    }

    fun asignarJugadorDraft(idTorneo: Int, idActor: Int, idJugador: Int): Boolean {
        val esAdmin = obtenerRolPorId(idActor) == "admin"
        val capitanActual = obtenerCapitanActualDraft(idTorneo) ?: return false
        if (!esAdmin && capitanActual.id_usuario != idActor) {
            return false
        }
        if (obtenerJugadoresDisponiblesDraft(idTorneo).none { it.id_usuario == idJugador }) {
            return false
        }

        val equipoId = obtenerEquipoIdPorCapitan(idTorneo, capitanActual.id_usuario) ?: return false
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id_equipo", equipoId)
            put("id_usuario", idJugador)
        }
        val result = db.insert("EquipoMiembros", null, values)
        db.close()

        if (result != -1L && draftCompletado(idTorneo)) {
            generarBracketAleatorio(idTorneo)
        }

        return result != -1L
    }

    private fun contarJugadoresAsignadosEnDraft(idTorneo: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT COUNT(*)
            FROM EquipoMiembros EM
            INNER JOIN Equipos E ON E.id_equipo = EM.id_equipo
            WHERE E.id_torneo = ?
            AND EM.id_usuario NOT IN (
                SELECT id_capitan FROM Equipos WHERE id_torneo = ?
            )
            """.trimIndent(),
            arrayOf(idTorneo.toString(), idTorneo.toString())
        )
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    private fun obtenerEquipoIdPorCapitan(idTorneo: Int, idCapitan: Int): Int? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id_equipo FROM Equipos WHERE id_torneo = ? AND id_capitan = ?",
            arrayOf(idTorneo.toString(), idCapitan.toString())
        )
        val equipoId = if (cursor.moveToFirst()) cursor.getInt(0) else null
        cursor.close()
        db.close()
        return equipoId
    }

    fun obtenerEquiposDelTorneo(idTorneo: Int): List<EquipoTorneo> {
        val equipos = mutableListOf<EquipoTorneo>()
        val db = readableDatabase
        val equiposCursor = db.rawQuery(
            """
            SELECT E.id_equipo, E.nombre, U.usuario
            FROM Equipos E
            INNER JOIN Usuarios U ON U.id_usuario = E.id_capitan
            WHERE E.id_torneo = ?
            ORDER BY E.id_equipo ASC
            """.trimIndent(),
            arrayOf(idTorneo.toString())
        )
        if (equiposCursor.moveToFirst()) {
            do {
                val equipoId = equiposCursor.getInt(0)
                val miembros = mutableListOf<String>()
                val miembrosCursor = db.rawQuery(
                    """
                    SELECT U.usuario
                    FROM EquipoMiembros EM
                    INNER JOIN Usuarios U ON U.id_usuario = EM.id_usuario
                    WHERE EM.id_equipo = ?
                    AND U.id_usuario != (SELECT id_capitan FROM Equipos WHERE id_equipo = ?)
                    ORDER BY U.usuario ASC
                    """.trimIndent(),
                    arrayOf(equipoId.toString(), equipoId.toString())
                )
                if (miembrosCursor.moveToFirst()) {
                    do {
                        miembros.add(miembrosCursor.getString(0))
                    } while (miembrosCursor.moveToNext())
                }
                miembrosCursor.close()

                equipos.add(
                    EquipoTorneo(
                        nombre = equiposCursor.getString(1),
                        capitan = equiposCursor.getString(2),
                        miembros = miembros
                    )
                )
            } while (equiposCursor.moveToNext())
        }
        equiposCursor.close()
        db.close()
        return equipos
    }

    fun obtenerPartidosTorneo(idTorneo: Int): List<PartidoTorneo> {
        val partidos = mutableListOf<PartidoTorneo>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT P.id_partido, P.ronda, P.orden, E1.nombre, E2.nombre, P.ganador, P.equipo1, P.equipo2
            FROM Partidos P
            INNER JOIN Equipos E1 ON E1.id_equipo = P.equipo1
            INNER JOIN Equipos E2 ON E2.id_equipo = P.equipo2
            WHERE P.id_torneo = ?
            ORDER BY P.ronda ASC, P.orden ASC
            """.trimIndent(),
            arrayOf(idTorneo.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                val ganadorId = if (cursor.isNull(5)) null else cursor.getInt(5)
                partidos.add(
                    PartidoTorneo(
                        id = cursor.getInt(0),
                        ronda = cursor.getInt(1),
                        orden = cursor.getInt(2),
                        equipo1 = cursor.getString(3),
                        equipo2 = cursor.getString(4),
                        ganador = when (ganadorId) {
                            cursor.getInt(6) -> cursor.getString(3)
                            cursor.getInt(7) -> cursor.getString(4)
                            else -> null
                        },
                        equipo1Id = cursor.getInt(6),
                        equipo2Id = cursor.getInt(7)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return partidos
    }

    private fun generarBracketAleatorio(idTorneo: Int): Boolean {
        if (obtenerPartidosTorneo(idTorneo).isNotEmpty()) {
            return false
        }

        val equipos = obtenerEquiposIds(idTorneo).shuffled()
        if (equipos.size != MAX_EQUIPOS_POR_TORNEO) {
            return false
        }

        val db = writableDatabase
        db.beginTransaction()
        return try {
            insertarPartido(db, idTorneo, equipos[0], equipos[1], 1, 1)
            insertarPartido(db, idTorneo, equipos[2], equipos[3], 1, 2)
            val values = ContentValues().apply { put("estado", ESTADO_EN_JUEGO) }
            db.update("Torneos", values, "id = ?", arrayOf(idTorneo.toString()))
            db.setTransactionSuccessful()
            true
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun registrarGanadorPartido(idPartido: Int, idGanador: Int): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val infoCursor = db.rawQuery(
                "SELECT id_torneo, ronda, equipo1, equipo2, ganador FROM Partidos WHERE id_partido = ?",
                arrayOf(idPartido.toString())
            )
            if (!infoCursor.moveToFirst()) {
                infoCursor.close()
                return false
            }
            val idTorneo = infoCursor.getInt(0)
            val ronda = infoCursor.getInt(1)
            val equipo1 = infoCursor.getInt(2)
            val equipo2 = infoCursor.getInt(3)
            val yaTieneGanador = !infoCursor.isNull(4)
            infoCursor.close()

            if (yaTieneGanador) {
                return false
            }

            if (idGanador != equipo1 && idGanador != equipo2) {
                return false
            }

            val updateValues = ContentValues().apply {
                put("ganador", idGanador)
                put("resultado", obtenerNombreEquipo(db, idGanador))
            }
            val filasActualizadas = db.update(
                "Partidos",
                updateValues,
                "id_partido = ?",
                arrayOf(idPartido.toString())
            )
            if (filasActualizadas <= 0) {
                return false
            }

            if (ronda == 1) {
                val semiCursor = db.rawQuery(
                    "SELECT ganador FROM Partidos WHERE id_torneo = ? AND ronda = 1 ORDER BY orden ASC",
                    arrayOf(idTorneo.toString())
                )
                val ganadores = mutableListOf<Int>()
                if (semiCursor.moveToFirst()) {
                    do {
                        if (!semiCursor.isNull(0)) {
                            ganadores.add(semiCursor.getInt(0))
                        }
                    } while (semiCursor.moveToNext())
                }
                semiCursor.close()

                if (ganadores.size == 2) {
                    val finalCursor = db.rawQuery(
                        "SELECT COUNT(*) FROM Partidos WHERE id_torneo = ? AND ronda = 2",
                        arrayOf(idTorneo.toString())
                    )
                    finalCursor.moveToFirst()
                    val existeFinal = finalCursor.getInt(0) > 0
                    finalCursor.close()

                    if (!existeFinal) {
                        insertarPartido(db, idTorneo, ganadores[0], ganadores[1], 2, 1)
                    }
                }
            } else if (ronda == 2) {
                val torneoValues = ContentValues().apply { put("estado", ESTADO_FINALIZADO) }
                db.update("Torneos", torneoValues, "id = ?", arrayOf(idTorneo.toString()))
            }

            db.setTransactionSuccessful()
            true
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    private fun insertarPartido(db: SQLiteDatabase, idTorneo: Int, equipo1: Int, equipo2: Int, ronda: Int, orden: Int) {
        val values = ContentValues().apply {
            put("id_torneo", idTorneo)
            put("equipo1", equipo1)
            put("equipo2", equipo2)
            put("ronda", ronda)
            put("orden", orden)
        }
        db.insert("Partidos", null, values)
    }

    private fun obtenerEquiposIds(idTorneo: Int): List<Int> {
        val ids = mutableListOf<Int>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id_equipo FROM Equipos WHERE id_torneo = ? ORDER BY id_equipo ASC",
            arrayOf(idTorneo.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                ids.add(cursor.getInt(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return ids
    }

    private fun obtenerNombreEquipo(db: SQLiteDatabase, idEquipo: Int): String {
        val cursor = db.rawQuery(
            "SELECT nombre FROM Equipos WHERE id_equipo = ?",
            arrayOf(idEquipo.toString())
        )
        val nombre = if (cursor.moveToFirst()) cursor.getString(0) else ""
        cursor.close()
        return nombre
    }

    private fun obtenerUsuariosPorRolTorneo(idTorneo: Int, rolTorneo: String): List<Usuario> {
        val usuarios = mutableListOf<Usuario>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT U.id_usuario, U.usuario, U.$PASSWORD_COLUMN, U.rol
            FROM Inscripciones I
            INNER JOIN Usuarios U ON U.id_usuario = I.id_usuario
            WHERE I.id_torneo = ? AND I.rol_torneo = ?
            ORDER BY I.id_inscripcion ASC
            """.trimIndent(),
            arrayOf(idTorneo.toString(), rolTorneo)
        )
        if (cursor.moveToFirst()) {
            do {
                usuarios.add(Usuario(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return usuarios
    }
}


