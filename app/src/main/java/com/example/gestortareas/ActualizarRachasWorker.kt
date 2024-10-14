package com.example.gestortareas

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.gestortareas.database.DatabaseHelper



class ActualizarRachasWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        // Llamar al método para actualizar rachas
        val dbHelper = DatabaseHelper(applicationContext)
        dbHelper.actualizarRachasDelDia()

        // Indicar que el trabajo ha sido exitoso
        return Result.success()
    }


}
