package com.example.courtcontrol

object FormValidator {
    private val usuarioRegex = Regex("^[A-Za-z0-9._-]{3,20}$")

    fun usuarioValido(usuario: String): Boolean = usuarioRegex.matches(usuario)

    fun passwordValida(password: String): Boolean {
        return password.length >= 6 &&
            password.any { it.isLetter() } &&
            password.any { it.isDigit() }
    }

    fun mensajeUsuario(): String = "El usuario debe tener 3-20 caracteres: letras, numeros, punto, guion o guion bajo"

    fun mensajePassword(): String = "La contrasena debe tener al menos 6 caracteres, una letra y un numero"
}
