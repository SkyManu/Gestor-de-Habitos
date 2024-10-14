package com.example.gestortareas

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gestortareas.database.DatabaseHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    // Instancia del DatabaseHelper
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        programarActualizacionRachas()
        programarNotificacionesHabitos()



        // Inicializar el DatabaseHelper
        dbHelper = DatabaseHelper(this)
        dbHelper.addUser("a","a","a")
        dbHelper.authenticateUser("a", "a")

        // Cargar el fragmento de inicio de sesión como el fragmento inicial

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, TareasFragment())
            .commit()

        // Obtener el BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Ocultar la barra de navegación al inicio
        //bottomNavigationView.visibility = View.GONE

        // Manejo de navegación
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.navigation_tareas -> selectedFragment = TareasFragment()
                R.id.navigation_vista -> selectedFragment = VistaFragment()
                R.id.navigation_informe -> selectedFragment = RachaFragment()
            }
            selectedFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, it)
                    .commit()
            }
            true
        }
    }
    fun programarNotificacionesHabitos() {
        // Obtener la hora actual
        val calendario = Calendar.getInstance()

        // Configurar la hora de ejecución a las 7:00 AM
        calendario.set(Calendar.HOUR_OF_DAY, 7)
        calendario.set(Calendar.MINUTE, 0)
        calendario.set(Calendar.SECOND, 0)

        // Calcular el tiempo inicial hasta la próxima ejecución a las 7:00 AM
        val ahora = Calendar.getInstance().timeInMillis
        val tiempoInicial = if (calendario.timeInMillis > ahora) {
            calendario.timeInMillis - ahora
        } else {
            calendario.add(Calendar.DAY_OF_MONTH, 1)
            calendario.timeInMillis - ahora
        }

        // Crear el trabajo periódico (se repetirá cada 24 horas)
        val workRequest = PeriodicWorkRequestBuilder<NotificacionesHabitosWorker>(1, TimeUnit.DAYS) // Diferente Worker
            .setInitialDelay(tiempoInicial, TimeUnit.MILLISECONDS)
            .build()

        // Programar el trabajo con WorkManager (cambiar el nombre para evitar conflicto)
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "notificaciones_habitos_trabajo", // Nombre único
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }



    fun programarActualizacionRachas() {
        // Obtener la hora actual
        val calendario = Calendar.getInstance()

        // Configurar la hora de ejecución a las 23:59
        calendario.set(Calendar.HOUR_OF_DAY, 23)
        calendario.set(Calendar.MINUTE, 59)
        calendario.set(Calendar.SECOND, 0)

        // Calcular el tiempo inicial hasta la próxima ejecución a las 23:59
        val ahora = Calendar.getInstance().timeInMillis
        val tiempoInicial = if (calendario.timeInMillis > ahora) {
            calendario.timeInMillis - ahora
        } else {
            calendario.add(Calendar.DAY_OF_MONTH, 1)
            calendario.timeInMillis - ahora
        }

        // Crear el trabajo periódico (se repetirá cada 24 horas)
        val workRequest = PeriodicWorkRequestBuilder<ActualizarRachasWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(tiempoInicial, TimeUnit.MILLISECONDS)
            .build()

        // Programar el trabajo con WorkManager
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "actualizar_rachas_trabajo", // Nombre único para evitar duplicados
            ExistingPeriodicWorkPolicy.REPLACE, // Reemplazar si ya está programado
            workRequest
        )
    }



    // Método para mostrar la barra de navegación después de iniciar sesión
    fun mostrarNavegacion() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.visibility = View.VISIBLE
    }
}
