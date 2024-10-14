package com.example.gestortareas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import com.example.gestortareas.database.DatabaseHelper

class RachaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_racha, container, false)

        // Obtener el RecyclerView
        val recyclerViewRachas: RecyclerView = view.findViewById(R.id.recyclerViewRachas)

        // Configurar el LayoutManager para que las tarjetas estén una debajo de otra
        recyclerViewRachas.layoutManager = LinearLayoutManager(requireContext())

        // Lista de ejemplo de hábitos y sus rachas actuales
        val dbHelper = DatabaseHelper(requireContext())
        val listaRachas = dbHelper.obtenerRachasDelUsuario(dbHelper.obtenerUsuarioIdActual())



        // Configurar el adaptador
        val adapter = RachaAdapter(listaRachas)
        recyclerViewRachas.adapter = adapter

        return view
    }
}
