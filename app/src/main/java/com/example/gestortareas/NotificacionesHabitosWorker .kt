package com.example.gestortareas

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.gestortareas.database.DatabaseHelper
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gestortareas.MainActivity
import java.text.SimpleDateFormat
import java.util.*

class NotificacionesHabitosWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {

        enviarNotificacionesHabitos()


        // Indicar que el trabajo ha sido exitoso
        return Result.success()
    }

    private fun enviarNotificacionesHabitos() {
        // Obtener el ID del usuario actual. Asegúrate de obtener el ID correcto


        // Obtener los hábitos del día
        val dbHelper = DatabaseHelper(applicationContext)
        val habitosDelDia = dbHelper.obtenerHabitosDelDia(dbHelper.obtenerUsuarioIdActual())

        // Crear una notificación por cada hábito
        habitosDelDia.forEach { habito ->
            enviarNotificacionHabito(habito.nombreHabito, habito.horaInicio, habito.horaFin)
        }
    }
    private fun enviarNotificacionHabito(nombreHabito: String, horaInicio: String, horaFin: String) {
        val notificationId = nombreHabito.hashCode()  // Generar un ID único para cada notificación

        // Crear un canal de notificación (obligatorio para Android 8+)
        createNotificationChannel()

        // Intent para abrir la MainActivity al pulsar la notificación
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Crear la notificación
        val builder = NotificationCompat.Builder(applicationContext, "habito_channel")
            .setSmallIcon(R.drawable.ic_tarea)  // Cambia este icono por uno adecuado
            .setContentTitle(nombreHabito)
            .setContentText("De $horaInicio a $horaFin")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Mostrar la notificación
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    // Método para crear el canal de notificación (necesario para Android 8+)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hábitos Diarios"
            val descriptionText = "Canal para notificaciones diarias de hábitos"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("habito_channel", name, importance).apply {
                description = descriptionText
            }

            // Registrar el canal en el sistema
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
