package com.example.courtcontrol

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "CourtControlDB"
        private const val DATABASE_VERSION = 2
    }

    override fun onCreate(db: SQLiteDatabase?) {

        // ---------------- USUARIOS ----------------
        db?.execSQL("""
            CREATE TABLE Usuarios (
                id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario TEXT NOT NULL UNIQUE,
                contraseña TEXT NOT NULL,
                rol TEXT NOT NULL
            )
        """)

        // 🔥 USUARIO ADMIN POR DEFECTO
        db?.execSQL("""
            INSERT INTO Usuarios (usuario, contraseña, rol)
            VALUES ('admin', 'admin', 'admin')
        """)

        // ---------------- TORNEOS ----------------
        db?.execSQL("""
            CREATE TABLE Torneos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                fecha TEXT,
                lugar TEXT,
                estado TEXT
            )
        """)

        // ---------------- EQUIPOS ----------------
        db?.execSQL("""
            CREATE TABLE Equipos (
                id_equipo INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                id_torneo INTEGER NOT NULL,
                id_capitan INTEGER NOT NULL
            )
        """)

        // ---------------- INSCRIPCIONES ----------------
        db?.execSQL("""
            CREATE TABLE Inscripciones (
            id_inscripcion INTEGER PRIMARY KEY AUTOINCREMENT,
            id_usuario INTEGER NOT NULL,
            id_torneo INTEGER NOT NULL,
            rol_torneo TEXT NOT NULL,
        
            FOREIGN KEY(id_usuario) REFERENCES Usuarios(id_usuario),
            FOREIGN KEY(id_torneo) REFERENCES Torneos(id)
            );
            
        """)

        // ---------------- PARTIDOS ----------------
        db?.execSQL("""
            CREATE TABLE Partidos (
                id_partido INTEGER PRIMARY KEY AUTOINCREMENT,
                id_torneo INTEGER NOT NULL,
                equipo1 INTEGER NOT NULL,
                equipo2 INTEGER NOT NULL,
                resultado TEXT,
                ganador INTEGER
            )
        """)

        // 🔥 TORNEOS DEMO (solo si es primera instalación)
        insertarTorneosIniciales(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Partidos")
        db?.execSQL("DROP TABLE IF EXISTS Inscripciones")
        db?.execSQL("DROP TABLE IF EXISTS Equipos")
        db?.execSQL("DROP TABLE IF EXISTS Torneos")
        db?.execSQL("DROP TABLE IF EXISTS Usuarios")
        onCreate(db)
    }

    // ======================================================
    // 🔥 INSERT TORNEOS SOLO UNA VEZ
    // ======================================================
    private fun insertarTorneosIniciales(db: SQLiteDatabase?) {

        val cursor = db?.rawQuery("SELECT COUNT(*) FROM Torneos", null)
        cursor?.moveToFirst()
        val count = cursor?.getInt(0) ?: 0
        cursor?.close()

        if (count == 0) {

            db?.execSQL("""
                INSERT INTO Torneos (nombre, fecha, lugar, estado)
                VALUES ('Torneo Verano', '12/06/2026', 'Madrid', 'Abierto')
            """)

            db?.execSQL("""
                INSERT INTO Torneos (nombre, fecha, lugar, estado)
                VALUES ('Torneo Invierno', '01/12/2026', 'Barcelona', 'Cerrado')
            """)

            db?.execSQL("""
                INSERT INTO Torneos (nombre, fecha, lugar, estado)
                VALUES ('Torneo Primavera', '20/03/2026', 'Valencia', 'En progreso')
            """)
        }
    }

    // ======================================================
    // 🔥 USUARIOS (LOS TUYOS, NO BORRADOS)
    // ======================================================

    fun insertarUsuario(usuario: String, contraseña: String, rol: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("usuario", usuario)
            put("contraseña", contraseña)
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

    fun validarLogin(usuario: String, contraseña: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id_usuario FROM Usuarios WHERE usuario = ? AND contraseña = ?",
            arrayOf(usuario, contraseña)
        )

        val ok = cursor.moveToFirst()
        cursor.close()
        db.close()
        return ok
    }

    fun obtenerUsuario(usuario: String): Usuario? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id_usuario, usuario, contraseña, rol FROM Usuarios WHERE usuario = ?",
            arrayOf(usuario)
        )

        var user: Usuario? = null

        if (cursor.moveToFirst()) {
            user = Usuario(
                id_usuario = cursor.getInt(0),
                usuario = cursor.getString(1),
                contraseña = cursor.getString(2),
                rol = cursor.getString(3)
            )
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
            put("contraseña", nuevaPassword)
        }

        val result = db.update(
            "Usuarios",
            values,
            "id_usuario = ?",
            arrayOf(idUsuario.toString())
        )

        db.close()
        return result > 0
    }

    // ======================================================
    // 🔥 TORNEOS
    // ======================================================

    fun obtenerTorneos(): List<Torneo> {
        val lista = mutableListOf<Torneo>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM Torneos", null)

        if (cursor.moveToFirst()) {
            do {
                lista.add(
                    Torneo(
                        id = cursor.getInt(0),
                        nombre = cursor.getString(1),
                        fecha = cursor.getString(2),
                        lugar = cursor.getString(3),
                        estado = cursor.getString(4)
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return lista
    }

    fun obtenerUsuariosPorTorneo(idTorneo: Int): List<UsuarioTorneo> {
        val lista = mutableListOf<UsuarioTorneo>()
        val db = readableDatabase

        val cursor = db.rawQuery("""
        SELECT U.usuario, I.rol_torneo
        FROM Inscripciones I
        INNER JOIN Usuarios U ON I.id_usuario = U.id_usuario
        WHERE I.id_torneo = ?
    """.trimIndent(), arrayOf(idTorneo.toString()))

        if (cursor.moveToFirst()) {
            do {
                lista.add(
                    UsuarioTorneo(
                        usuario = cursor.getString(0),
                        rolTorneo = cursor.getString(1)
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return lista
    }
    fun contarInscritos(idTorneo: Int): Int {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM Inscripciones WHERE id_torneo = ?",
            arrayOf(idTorneo.toString())
        )

        cursor.moveToFirst()
        val count = cursor.getInt(0)

        cursor.close()
        db.close()

        return count
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
            put("rol_torneo", tipo) // ✅ CORRECTO
        }

        val result = db.insert("Inscripciones", null, values)
        db.close()

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
    fun contarInscritosPorTorneo(idTorneo: Int): Int {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM Inscripciones WHERE id_torneo = ?",
            arrayOf(idTorneo.toString())
        )

        cursor.moveToFirst()
        val count = cursor.getInt(0)

        cursor.close()
        db.close()

        return count
    }

}