package com.example.gestortareas

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.gestortareas.database.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

class CrearTareaFragment : Fragment() {

    private lateinit var editTextNombreTarea: EditText
    private lateinit var botonFechaInicio: Button
    private lateinit var botonFechaFin: Button
    private lateinit var botonHoraInicio: Button
    private lateinit var botonHoraFin: Button
    private lateinit var spinnerRepetir: Spinner
    private lateinit var spinnerTipoTarea: Spinner // Spinner para tipo de tarea
    private lateinit var botonGuardarTarea: Button
    private lateinit var dbHelper: DatabaseHelper
    private val calendar: Calendar = Calendar.getInstance()

    // Variable para almacenar los días seleccionados
    private var diasSeleccionados: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_tarea, container, false)

        // Inicializar elementos
        editTextNombreTarea = view.findViewById(R.id.edit_text_nombre_tarea)
        botonFechaInicio = view.findViewById(R.id.boton_fecha_inicio)
        botonFechaFin = view.findViewById(R.id.boton_fecha_fin)
        botonHoraInicio = view.findViewById(R.id.boton_hora_inicio)
        botonHoraFin = view.findViewById(R.id.boton_hora_fin)
        spinnerRepetir = view.findViewById(R.id.spinner_repetir)
        spinnerTipoTarea = view.findViewById(R.id.spinner_tipo_tarea)
        botonGuardarTarea = view.findViewById(R.id.boton_crear_tarea)

        // Establecer fechas y horas predeterminadas
        establecerFechasPredeterminadas()

        // Configurar eventos
        botonFechaInicio.setOnClickListener { seleccionarFechaInicio() }
        botonFechaFin.setOnClickListener { seleccionarFechaFin() }
        botonHoraInicio.setOnClickListener { seleccionarHoraInicio() }
        botonHoraFin.setOnClickListener { seleccionarHoraFin() }

        // Configurar el Spinner para seleccionar los días
        configurarSpinner()

        botonGuardarTarea.setOnClickListener { registrarTarea() }

        return view
    }

    private fun configurarSpinner() {
        spinnerRepetir.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    // Selecciona "Todos los días"
                    diasSeleccionados = "Lunes/Martes/Miércoles/Jueves/Viernes/Sábado/Domingo"
                } else {
                    // Selecciona "Personalizar"
                    mostrarDialogoPersonalizar()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun mostrarDialogoPersonalizar() {
        val dias = arrayOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val diasSeleccionadosTemp = BooleanArray(dias.size)
        val diasSeleccionadosLista = mutableListOf<String>()

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Selecciona los días")
        builder.setMultiChoiceItems(dias, diasSeleccionadosTemp) { _, which, isChecked ->
            if (isChecked) {
                diasSeleccionadosLista.add(dias[which])
            } else {
                diasSeleccionadosLista.remove(dias[which])
            }
        }
        builder.setPositiveButton("Aceptar") { _, _ ->
            if (diasSeleccionadosLista.isNotEmpty()) {
                diasSeleccionados = diasSeleccionadosLista.joinToString("/")
            } else {
                diasSeleccionados = ""
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.create().show()
    }

    private fun registrarTarea() {
        val nombreTarea = editTextNombreTarea.text.toString()
        val fechaInicio = botonFechaInicio.text.toString()
        val fechaFin = botonFechaFin.text.toString()
        val horaInicio = botonHoraInicio.text.toString()
        val horaFin = botonHoraFin.text.toString()
        val tipoTareaSeleccionada = spinnerTipoTarea.selectedItem.toString()

        val fecha_ini = convertirFechaAEntero(fechaInicio)
        val fecha_fin = convertirFechaAEntero(fechaFin)

        val tipoHabit = if (tipoTareaSeleccionada == "Check") {
            "check" // Tipo de hábito de solo marcar como completado
        } else {
            "tiempo" // Tipo de hábito que contabiliza el tiempo
        }

        if (nombreTarea.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, introduce un nombre para el habito.", Toast.LENGTH_SHORT).show()
            return
        }
        if (diasSeleccionados== "") {
            Toast.makeText(requireContext(), "Por favor, introduce en que dias se va a realizar el habito.", Toast.LENGTH_SHORT).show()
            return
        }



        // Obtener el ID del usuario y guardar la tarea
        dbHelper = DatabaseHelper(requireContext())
        val usuarioId = dbHelper.obtenerUsuarioIdActual()
        val resultado = dbHelper.addHabit(
            usuarioId,
            nombreTarea,
            tipoHabit,  // Tipo de hábito
            diasSeleccionados,  // Días seleccionados
            fecha_ini,
            fecha_fin,
            horaInicio,
            horaFin
        )

        if (resultado != -1L) {
            Toast.makeText(requireContext(), "Tarea registrada con éxito", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Error al registrar la tarea", Toast.LENGTH_SHORT).show()
        }
    }
    fun convertirFechaAEntero(fecha: String): Int {
        // Definir el formato de la fecha original
        val formatoOriginal = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Parsear la cadena de fecha a un objeto Date
        val date: Date = formatoOriginal.parse(fecha) ?: return 0 // Manejar el caso de fecha no válida

        // Definir el nuevo formato de la fecha
        val formatoNuevo = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        // Formatear el objeto Date en el nuevo formato
        val fechaFormateada = formatoNuevo.format(date)

        // Convertir la cadena formateada a entero
        return fechaFormateada.toInt()
    }

    private fun establecerFechasPredeterminadas() {
        val fechaInicio = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        botonFechaInicio.text = fechaInicio

        calendar.add(Calendar.MONTH, 6)
        val fechaFin = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        botonFechaFin.text = fechaFin
        calendar.add(Calendar.MONTH, -6)

        establecerHorasPredeterminadas()
    }

    private fun establecerHorasPredeterminadas() {
        val horaInicio = calendar.get(Calendar.HOUR_OF_DAY) + 1
        botonHoraInicio.text = String.format("%02d:%02d", horaInicio, 0)
        botonHoraFin.text = String.format("%02d:%02d", horaInicio + 1, 0)
    }

    private fun seleccionarFechaInicio() {
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            botonFechaInicio.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun seleccionarFechaFin() {
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            botonFechaFin.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun seleccionarHoraInicio() {
        TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
            botonHoraInicio.text = String.format("%02d:%02d", hourOfDay, minute)
        }, calendar.get(Calendar.HOUR_OF_DAY) + 1, 0, true).show()
    }

    private fun seleccionarHoraFin() {
        TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
            botonHoraFin.text = String.format("%02d:%02d", hourOfDay, minute)
        }, calendar.get(Calendar.HOUR_OF_DAY) + 2, 0, true).show()
    }
}
