package com.example.gestortareas

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestortareas.database.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

class VistaFragment : Fragment() {

    private lateinit var spinnerVista: Spinner
    private lateinit var contenedorDia: View
    private lateinit var contenedorSemana: View
    private lateinit var contenedorMes: View
    private lateinit var recyclerViewDia: RecyclerView
    private lateinit var recyclerViewSemana: RecyclerView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var btnSemanaAnterior: Button
    private lateinit var btnSemanaSiguiente: Button
    private lateinit var txtSemanaActual: TextView
    private lateinit var diasSemana: List<Button> // Lista de botones para los días de la semana
    private var diaActual: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    private var calendario: Calendar = Calendar.getInstance()
    private var diaSeleccionadoIndex = obtenerIndiceDiaSemana(diaActual) // Guardar el índice del día seleccionado
    private var botonSeleccionadoAnterior: Button? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vista, container, false)
        dbHelper = DatabaseHelper(requireContext())

        // Inicializar vistas
        spinnerVista = view.findViewById(R.id.spinner_vista)
        contenedorDia = view.findViewById(R.id.contenedor_dia)
        contenedorSemana = view.findViewById(R.id.contenedor_semana)
        contenedorMes = view.findViewById(R.id.contenedor_mes)
        recyclerViewDia = view.findViewById(R.id.recycler_view_tareas_dia)
        recyclerViewSemana = view.findViewById(R.id.recycler_view_calendario_semana)
        btnSemanaAnterior = view.findViewById(R.id.btn_semana_anterior)
        btnSemanaSiguiente = view.findViewById(R.id.btn_semana_siguiente)
        txtSemanaActual = view.findViewById(R.id.txt_semana_actual)

        // Lista de días de la semana (Lunes a Domingo)
        diasSemana = listOf(
            view.findViewById(R.id.dia_lunes),
            view.findViewById(R.id.dia_martes),
            view.findViewById(R.id.dia_miercoles),
            view.findViewById(R.id.dia_jueves),
            view.findViewById(R.id.dia_viernes),
            view.findViewById(R.id.dia_sabado),
            view.findViewById(R.id.dia_domingo)
        )

        // Configurar el RecyclerView para día
        recyclerViewDia.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewSemana.layoutManager = LinearLayoutManager(requireContext())


        // Configurar la selección del Spinner
        spinnerVista.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> mostrarVistaDia()
                    1 -> mostrarVistaSemana()
                    2 -> mostrarVistaMes()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Configurar botones de cambio de semana
        btnSemanaAnterior.setOnClickListener { cambiarSemana(-1) }
        btnSemanaSiguiente.setOnClickListener { cambiarSemana(1) }

        // Configurar los botones de los días de la semana
        diasSemana.forEachIndexed { index, button ->
            button.setOnClickListener {
                // Si hay un botón previamente seleccionado, restablecer su color
                botonSeleccionadoAnterior?.setBackgroundColor(resources.getColor(R.color.color_button_default)) // O el color original deseado

                // Cambiar el color de fondo del botón actual
                button.setBackgroundColor(resources.getColor(R.color.color_button_background))

                // Llamar al método de selección de día
                seleccionarDiaSemana(index)  // Actualiza las tareas
                diaSeleccionadoIndex = index  // Guarda el índice del día seleccionado
                actualizarSeleccionDiaSemana()  // Actualiza la selección visual

                // Guardar el botón actual como el anterior
                botonSeleccionadoAnterior = button
            }
        }

        return view
    }

    // Mostrar vista del día
    private fun mostrarVistaDia() {
        contenedorDia.visibility = View.VISIBLE
        contenedorSemana.visibility = View.GONE
        contenedorMes.visibility = View.GONE
        mostrarTareasDia()
    }

    // Mostrar vista de la semana
    private fun mostrarVistaSemana() {
        contenedorDia.visibility = View.GONE
        contenedorSemana.visibility = View.VISIBLE
        contenedorMes.visibility = View.GONE

        actualizarDiasSemana()

        // Seleccionar automáticamente el día actual
        seleccionarDiaSemana(diaSeleccionadoIndex)
    }

    private fun mostrarVistaMes() {
        contenedorDia.visibility = View.GONE
        contenedorSemana.visibility = View.GONE
        contenedorMes.visibility = View.VISIBLE
    }

    // Método para seleccionar un día de la semana
    private fun seleccionarDiaSemana(diaSemana: Int) {
        try {
            val calendarioSemana = calendario.clone() as Calendar
            calendarioSemana.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            calendarioSemana.add(Calendar.DAY_OF_WEEK, diaSemana)

            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val fechaSeleccionada = sdf.format(calendarioSemana.time)

            val diaSeleccionado = obtenerDiaDeLaSemana(calendarioSemana.get(Calendar.DAY_OF_WEEK))

            mostrarTareasSemana(diaSeleccionado, fechaSeleccionada)

            // Cambiar el índice del día seleccionado
            diaSeleccionadoIndex = diaSemana

            actualizarSeleccionDiaSemana()

            Toast.makeText(context, "Mostrando hábitos para $diaSeleccionado ($fechaSeleccionada)", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al obtener las tareas: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Obtener el nombre del día de la semana
    private fun obtenerDiaDeLaSemana(diaSemana: Int): String {
        return when (diaSemana) {
            Calendar.MONDAY -> "Lunes"
            Calendar.TUESDAY -> "Martes"
            Calendar.WEDNESDAY -> "Miércoles"
            Calendar.THURSDAY -> "Jueves"
            Calendar.FRIDAY -> "Viernes"
            Calendar.SATURDAY -> "Sábado"
            Calendar.SUNDAY -> "Domingo"
            else -> "Lunes"
        }
    }

    // Obtener el índice correspondiente al día de la semana actual (Lunes = 0, Domingo = 6)
    private fun obtenerIndiceDiaSemana(diaSemana: Int): Int {
        return when (diaSemana) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0 // Por defecto, lunes
        }
    }

    private fun mostrarTareasDia() {
        try {
            val usuarioId = dbHelper.obtenerUsuarioIdActual()
            val listaHabitos = dbHelper.obtenerHabitosDelDia(usuarioId)
            val adapter = TareasAdapter(listaHabitos, requireContext(),true)
            recyclerViewDia.adapter = adapter
        } catch (e: Exception) {
            Toast.makeText(context, "Error al obtener las tareas: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarTareasSemana(diaSeleccionado: String, fechaSeleccionada: String) {
        try {
            val usuarioId = dbHelper.obtenerUsuarioIdActual()
            val listaHabitos = dbHelper.obtenerHabitosDelDia(usuarioId, diaSeleccionado, fechaSeleccionada)
            val adapter = TareasAdapter(listaHabitos, requireContext(),false)
            recyclerViewSemana.adapter = adapter
        } catch (e: Exception) {
            Toast.makeText(context, "Error al obtener las tareas: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cambiarSemana(direccion: Int) {
        calendario.add(Calendar.WEEK_OF_YEAR, direccion)
        actualizarDiasSemana()
    }

    // Actualizar los botones de los días de la semana con las fechas correctas
    private fun actualizarDiasSemana() {
        calendario.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val formatoFecha = SimpleDateFormat("dd MMM", Locale.getDefault())

        for (i in diasSemana.indices) {
            val diaActual = calendario.time
            diasSemana[i].text = formatoFecha.format(diaActual)
            calendario.add(Calendar.DAY_OF_YEAR, 1)
        }

        calendario.add(Calendar.DAY_OF_YEAR, -7) // Restaurar la semana original
    }

    private fun actualizarSeleccionDiaSemana() {
        diasSemana.forEachIndexed { index, button ->
            button.isSelected = index == diaSeleccionadoIndex
        }
    }
}
