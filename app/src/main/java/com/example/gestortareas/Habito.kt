package com.example.gestortareas

data class Habito(
    val habitoId: Int,
    val nombreHabito: String,
    val tipoHabito: String,  // "check" o "tiempo"
    val horaInicio: String,
    val horaFin: String,
    val completado: Boolean   // Nuevo campo para indicar si el hábito está completado hoy
)
