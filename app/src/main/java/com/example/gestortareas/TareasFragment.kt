package com.example.gestortareas

import android.app.AlertDialog
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestortareas.database.DatabaseHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class TareasFragment : Fragment() {

    private lateinit var fabCrearTarea: FloatingActionButton
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerViewTareas: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_tareas, container, false)

        fabCrearTarea = view.findViewById(R.id.fab_crear_tarea)
        recyclerViewTareas = view.findViewById(R.id.recycler_view_tareas)

        // Inicializar la base de datos
        dbHelper = DatabaseHelper(requireContext())

        // Configurar el RecyclerView
        recyclerViewTareas.layoutManager = LinearLayoutManager(requireContext())

        // Obtener las tareas y mostrarlas
        mostrarTareas()

        fabCrearTarea.setOnClickListener {
            // Transición al fragmento de crear tarea
            val crearTareaFragment = CrearTareaFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, crearTareaFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }

    private fun mostrarTareas() {
        try {
            // Obtener el ID del usuario actual
            val usuarioId = dbHelper.obtenerUsuarioIdActual()

            // Obtener la lista de hábitos del día actual
            val listaHabitos = dbHelper.obtenerHabitosDelDia(usuarioId)

            // Crear y establecer el adapter para el RecyclerView
            val adapter = TareasAdapter(listaHabitos, requireContext(),true)
            recyclerViewTareas.adapter = adapter

        } catch (e: Exception) {
            Toast.makeText(context, "Error al obtener las tareas: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onResume() {
        super.onResume()
        mostrarTareas()  // Actualiza la lista de tareas cada vez que el fragmento sea visible
    }


    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}
