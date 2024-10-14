package com.example.gestortareas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Racha(
    val rachaId: Int,
    val habitoId: Int,
    val rachaActual: Int,
    val rachaMaxima: Int,
    val nombreHabito: String, // Añadir el nombre del hábito
    val tiempo: Int,
    val registrosCompletados: Int = 0,  // Nuevo campo
    val totalRegistros: Int = 0 // Total de registros
)

class RachaAdapter(private val listaRachas: List<Racha>) : RecyclerView.Adapter<RachaAdapter.RachaViewHolder>() {

    class RachaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreHabito: TextView = itemView.findViewById(R.id.tvNombreHabito)
        val tvRachaActual: TextView = itemView.findViewById(R.id.tvRachaActual)
        val tvTiempoDedicado: TextView = itemView.findViewById(R.id.tvTiempoDedicado) // Añadir TextView para el tiempo
        val tvRegistros: TextView = itemView.findViewById(R.id.tvRegistros) // TextView para registros completados / total
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RachaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_racha, parent, false)
        return RachaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RachaViewHolder, position: Int) {
        val racha = listaRachas[position]
        holder.tvNombreHabito.text = racha.nombreHabito
        holder.tvRachaActual.text = "${racha.rachaActual} Días"

        val horas = racha.tiempo / 60
        val minutos = racha.tiempo % 60
        holder.tvTiempoDedicado.text = "${horas}h${minutos}m" // Mostrar el tiempo

        // Mostrar registros completados / total
        holder.tvRegistros.text = "${racha.registrosCompletados} / ${racha.totalRegistros} "
    }

    override fun getItemCount(): Int {
        return listaRachas.size
    }
}
