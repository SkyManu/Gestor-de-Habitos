package com.example.gestortareas

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.gestortareas.database.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.recyclerview.widget.ItemTouchHelper

class TareasAdapter(
    private val listaHabitos: List<Habito>,
    private val context: Context,
    private val isEditable: Boolean // Nuevo parámetro
) : RecyclerView.Adapter<TareasAdapter.HabitoViewHolder>() {


    // Clase interna para manejar el arrastre
    private inner class TareasSwipeCallback : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false // No manejamos el movimiento
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val habito = listaHabitos[position]
            mostrarMenuEliminar(habito, position)
        }
    }
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val itemTouchHelper = ItemTouchHelper(TareasSwipeCallback())
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }



    class HabitoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombreHabito: TextView = view.findViewById(R.id.tv_nombre_habito)
        val checkBoxCompletado: CheckBox = view.findViewById(R.id.cb_completado)
        val tvHoraInicio: TextView = view.findViewById(R.id.tv_hora_inicio) // TextView para la hora de inicio
        val tvHoraFin: TextView = view.findViewById(R.id.tv_hora_fin) // TextView para la hora de fin

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tarea, parent, false)
        return HabitoViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitoViewHolder, position: Int) {
        val habito = listaHabitos[position]

        // Establecer el nombre del hábito
        holder.nombreHabito.text = habito.nombreHabito

        // Establecer el estado del CheckBox según si el hábito está completado o no
        holder.checkBoxCompletado.isChecked = habito.completado

        // Mostrar las horas de inicio y fin del hábito
        holder.tvHoraInicio.text = " De: ${habito.horaInicio}"
        holder.tvHoraFin.text = " A: ${habito.horaFin}"

        holder.checkBoxCompletado.isEnabled = isEditable // Deshabilitar el checkbox
        holder.checkBoxCompletado.isClickable = isEditable // Asegurar que no sea

        // Manejar el cambio de estado del CheckBox
        holder.checkBoxCompletado.setOnCheckedChangeListener { _, isChecked ->
            if (isEditable) { // Solo manejar el evento si es editable
                if (isChecked) {
                    if (habito.tipoHabito == "tiempo") {
                        // Si el hábito requiere tiempo, mostrar el diálogo para ingresar tiempo dedicado
                        mostrarDialogoTiempo(context, habito)
                    } else {
                        // Marcar la tarea como completada sin tiempo dedicado
                        marcarTareaComoCompletada(habito, true, 0)
                    }
                } else {
                    // Marcar la tarea como no completada
                    marcarTareaComoCompletada(habito, false, 0)
                }
            }
        }
    }


    override fun getItemCount(): Int {
        return listaHabitos.size
    }

    private fun mostrarMenuEliminar(habito: Habito, position: Int) {
        val options = arrayOf("Eliminar habito")

        AlertDialog.Builder(context)
            .setTitle("¿Estas seguro?")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> eliminarTodasLasTareas(habito.habitoId) // Eliminar todas las tareas
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }


    private fun eliminarTodasLasTareas(habitoId: Int) {
        val dbHelper = DatabaseHelper(context)
        // Eliminar todas las tareas de la base de datos
        dbHelper.eliminarTodasLasTareas(habitoId)
        // Notificar al adaptador que los datos han cambiado
        notifyDataSetChanged() // Si estás eliminando varias tareas, es mejor refrescar todo
    }




    private fun obtenerFechaActual(): String {
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatoFecha.format(Date())
    }

    private fun marcarTareaComoCompletada(habito: Habito, completado: Boolean, tiempoDedicado: Int) {
        // Verificar si el registro ya existe y actualizarlo si es necesario
        val dbHelper = DatabaseHelper(context)
        val fechaHoystr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val fechaHoy = fechaHoystr.toInt()

        if (dbHelper.existeRegistroDiario(habito.habitoId, fechaHoy)) {
            // Actualizar el registro existente
            dbHelper.actualizarRegistroDiario(habito.habitoId, fechaHoy, completado,tiempoDedicado)
        } else {
            // Insertar un nuevo registro
            dbHelper.insertarRegistroDiario(habito.habitoId, fechaHoy, completado, tiempoDedicado)
        }
    }

    private fun mostrarDialogoTiempo(context: Context, habito: Habito) {
        // Inflar el layout personalizado del diálogo
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_ingresar_tiempo, null)

        val numberPickerHoras = dialogView.findViewById<NumberPicker>(R.id.np_horas)
        val numberPickerMinutos = dialogView.findViewById<NumberPicker>(R.id.np_minutos)

        // Configurar el rango de los NumberPickers
        numberPickerHoras.minValue = 0
        numberPickerHoras.maxValue = 23
        numberPickerMinutos.minValue = 0
        numberPickerMinutos.maxValue = 59

        // Crear y mostrar el diálogo
        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Seleccionar tiempo dedicado")
            .setPositiveButton("Guardar") { _, _ ->
                val horas = numberPickerHoras.value
                val minutos = numberPickerMinutos.value
                val tiempoTotal = (horas * 60) + minutos // Convertir horas y minutos a minutos totales
                marcarTareaComoCompletada(habito, true, tiempoTotal)
            }
            .setNegativeButton("Cancelar", null)

        builder.create().show()
    }
}


