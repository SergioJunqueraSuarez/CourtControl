package com.example.courtcontrol

data class PartidoTorneo(
    val id: Int,
    val ronda: Int,
    val orden: Int,
    val equipo1: String,
    val equipo2: String,
    val resultado: String?,
    val ganador: String?,
    val equipo1Id: Int,
    val equipo2Id: Int
)
