package com.example.gestortareas.database

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.gestortareas.Habito
import com.example.gestortareas.Racha
import com.example.gestortareas.TareasFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.gestortareas.database.DatabaseHelper



class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "gestor_tareas.db"
        private const val DATABASE_VERSION = 1

        // Tabla Usuarios
        private const val TABLE_USUARIOS = "Usuarios"
        private const val COLUMN_USUARIO_ID = "usuario_id"
        private const val COLUMN_NOMBRE = "nombre"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_CONTRASENA = "contraseña"

        // Tabla Habitos
        private const val TABLE_HABITOS = "Habitos"
        private const val COLUMN_HABITO_ID = "habito_id"
        private const val COLUMN_USUARIO_ID_HABITO = "usuario_id" // Cambiado a usuario_id
        private const val COLUMN_NOMBRE_HABITO = "nombre_habito"
        private const val COLUMN_TIPO_HABITO = "tipo_habito"
        private const val COLUMN_TIEMPO_OBJETIVO = "tiempo_objetivo"
        private const val COLUMN_DIAS_SEMANALES = "dias_semanales"
        private const val COLUMN_FECHA_INICIO = "fecha_inicio"
        private const val COLUMN_FECHA_FIN = "fecha_fin"
        private const val COLUMN_HORA_INICIO = "hora_inicio"  // Nueva columna
        private const val COLUMN_HORA_FIN = "hora_fin"

        // Tabla RegistroDiario
        private const val TABLE_REGISTRO_DIARIO = "RegistroDiario"
        private const val COLUMN_REGISTRO_ID = "registro_id"
        private const val COLUMN_HABITO_ID_REGISTRO = "habito_id"
        private const val COLUMN_FECHA = "fecha"
        private const val COLUMN_COMPLETADO = "completado"
        private const val COLUMN_TIEMPO_DEDICADO = "tiempo_dedicado"

        // Tabla Rachas
        private const val TABLE_RACHAS = "Rachas"
        private const val COLUMN_RACHA_ID = "racha_id"
        private const val COLUMN_HABITO_ID_RACHA = "habito_id" // Relación con la tabla de hábitos
        private const val COLUMN_RACHA_ACTUAL = "racha_actual"
        private const val COLUMN_RACHA_MAXIMA = "racha_maxima"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)


    override fun onCreate(db: SQLiteDatabase) {
        // Crear tablas
        val createUsuariosTable = ("CREATE TABLE $TABLE_USUARIOS (" +
                "$COLUMN_USUARIO_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NOMBRE TEXT NOT NULL, " +
                "$COLUMN_EMAIL TEXT UNIQUE NOT NULL, " +
                "$COLUMN_CONTRASENA TEXT NOT NULL)")

        // Cambios en la tabla Habitos
        val createHabitosTable = ("CREATE TABLE $TABLE_HABITOS (" +
                "$COLUMN_HABITO_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USUARIO_ID_HABITO INTEGER, " +
                "$COLUMN_NOMBRE_HABITO TEXT NOT NULL, " +
                "$COLUMN_TIPO_HABITO TEXT CHECK($COLUMN_TIPO_HABITO IN ('check', 'tiempo')), " +
                "$COLUMN_DIAS_SEMANALES TEXT, " +  // Cambiado a TEXT para manejar una lista de días
                "$COLUMN_FECHA_INICIO INTEGER, " +
                "$COLUMN_FECHA_FIN INTEGER, " +
                "$COLUMN_HORA_INICIO TEXT, " +
                "$COLUMN_HORA_FIN TEXT, " +
                "FOREIGN KEY ($COLUMN_USUARIO_ID_HABITO) REFERENCES $TABLE_USUARIOS($COLUMN_USUARIO_ID))")

        // Cambios en la tabla RegistroDiario
        val createRegistroDiarioTable = (
                "CREATE TABLE $TABLE_REGISTRO_DIARIO (" +
                        "$COLUMN_REGISTRO_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "$COLUMN_HABITO_ID_REGISTRO INTEGER, " +
                        "$COLUMN_FECHA INTEGER NOT NULL, " +
                        "$COLUMN_COMPLETADO INTEGER, " +
                        "$COLUMN_TIEMPO_DEDICADO TEXT, " +  // Almacena tiempo dedicado como string (ej. "120" para 120 minutos)
                        "FOREIGN KEY ($COLUMN_HABITO_ID_REGISTRO) REFERENCES $TABLE_HABITOS($COLUMN_HABITO_ID))"
                )

        val createRachasTable = (
                "CREATE TABLE $TABLE_RACHAS (" +
                        "$COLUMN_RACHA_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "$COLUMN_HABITO_ID_RACHA INTEGER, " +
                        "$COLUMN_RACHA_ACTUAL INTEGER DEFAULT 0, " +  // Inicialmente 0
                        "$COLUMN_RACHA_MAXIMA INTEGER DEFAULT 0, " +  // Inicialmente 0
                        "FOREIGN KEY ($COLUMN_HABITO_ID_RACHA) REFERENCES $TABLE_HABITOS($COLUMN_HABITO_ID))"
                )



        db.execSQL(createUsuariosTable)
        db.execSQL(createHabitosTable)
        db.execSQL(createRegistroDiarioTable)
        db.execSQL(createRachasTable)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    // Método para agregar un usuario
    fun addUser(nombre: String, email: String, contrasena: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_NOMBRE, nombre)
        contentValues.put(COLUMN_EMAIL, email)
        contentValues.put(COLUMN_CONTRASENA, contrasena)

        val result = db.insert(TABLE_USUARIOS, null, contentValues)
        db.close()
        return result
    }

    // Método para autenticar un usuario
    fun authenticateUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_USUARIO_ID FROM $TABLE_USUARIOS WHERE $COLUMN_EMAIL = ? AND $COLUMN_CONTRASENA = ?", arrayOf(email, password))

        if (cursor.moveToFirst()) {
            val usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USUARIO_ID))
            almacenarUsuarioId(usuarioId)  // Almacenar el usuarioId en SharedPreferences
            registrarHabitosDelDia(usuarioId)
            cursor.close()
            db.close()
            return true
        } else {
            cursor.close()
            db.close()
            return false
        }
    }



    // Método para agregar un hábito vinculado a un usuario
// Método para agregar un hábito vinculado a un usuario
    fun addHabit(
        usuarioId: Int,
        nombreHabito: String,
        tipoHabito: String,
        diasSemanales: String,
        fechaInicio: Int,
        fechaFin: Int,
        horaInicio: String,
        horaFin: String
    ): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_USUARIO_ID_HABITO, usuarioId)
        contentValues.put(COLUMN_NOMBRE_HABITO, nombreHabito)
        contentValues.put(COLUMN_TIPO_HABITO, tipoHabito)
        contentValues.put(COLUMN_DIAS_SEMANALES, diasSemanales)
        contentValues.put(COLUMN_FECHA_INICIO, fechaInicio)
        contentValues.put(COLUMN_FECHA_FIN, fechaFin)
        contentValues.put(COLUMN_HORA_INICIO, horaInicio)
        contentValues.put(COLUMN_HORA_FIN, horaFin)
        val result = db.insert(TABLE_HABITOS, null, contentValues)
        registrarHabitosDelDia(obtenerUsuarioIdActual())
        val habitId = result.toInt()  // Obtener el ID del hábito recién creado
        if (habitId > 0) {
            insertarRacha(habitId)
        }
        db.close()
        return result
    }

    fun insertarRacha(habitoId: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_HABITO_ID_RACHA, habitoId)
        contentValues.put(COLUMN_RACHA_ACTUAL, 0)  // Racha actual comienza en 0
        contentValues.put(COLUMN_RACHA_MAXIMA, 0)  // Racha máxima también comienza en 0
        db.insert(TABLE_RACHAS, null, contentValues)
        db.close()
    }
    fun actualizarRachasDelDia() {
        val db = this.writableDatabase

        // Obtener la fecha actual en formato yyyyMMdd
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val fechaActual = sdf.format(Date()).toInt()

        // 1. Obtener todos los registros de la fecha actual
        val cursorRegistros = db.rawQuery(
            "SELECT $COLUMN_HABITO_ID_REGISTRO, $COLUMN_COMPLETADO FROM $TABLE_REGISTRO_DIARIO WHERE $COLUMN_FECHA = ?",
            arrayOf(fechaActual.toString())
        )

        if (cursorRegistros.moveToFirst()) {
            do {
                val habitoId = cursorRegistros.getInt(cursorRegistros.getColumnIndexOrThrow(COLUMN_HABITO_ID_REGISTRO))
                val completado = cursorRegistros.getInt(cursorRegistros.getColumnIndexOrThrow(COLUMN_COMPLETADO)) == 1

                // 2. Obtener la racha asociada a este hábito
                val cursorRachas = db.rawQuery(
                    "SELECT $COLUMN_RACHA_ACTUAL, $COLUMN_RACHA_MAXIMA FROM $TABLE_RACHAS WHERE $COLUMN_HABITO_ID_RACHA = ?",
                    arrayOf(habitoId.toString())
                )

                if (cursorRachas.moveToFirst()) {
                    val rachaActual = cursorRachas.getInt(cursorRachas.getColumnIndexOrThrow(COLUMN_RACHA_ACTUAL))
                    val rachaMaxima = cursorRachas.getInt(cursorRachas.getColumnIndexOrThrow(COLUMN_RACHA_MAXIMA))

                    val nuevoRachaActual: Int
                    val nuevoRachaMaxima: Int

                    // 3. Si completado = 0, resetear la racha actual
                    if (!completado) {
                        nuevoRachaActual = 0
                        nuevoRachaMaxima = rachaMaxima  // La racha máxima no cambia si no se completa
                    } else {
                        // 4. Si completado = 1, incrementar la racha actual y actualizar la racha máxima si es necesario
                        nuevoRachaActual = rachaActual + 1
                        nuevoRachaMaxima = if (nuevoRachaActual > rachaMaxima) nuevoRachaActual else rachaMaxima
                    }

                    // Actualizar la tabla de rachas
                    val contentValues = ContentValues().apply {
                        put(COLUMN_RACHA_ACTUAL, nuevoRachaActual)
                        put(COLUMN_RACHA_MAXIMA, nuevoRachaMaxima)
                    }
                    db.update(
                        TABLE_RACHAS,
                        contentValues,
                        "$COLUMN_HABITO_ID_RACHA = ?",
                        arrayOf(habitoId.toString())
                    )
                }
                cursorRachas.close()

            } while (cursorRegistros.moveToNext())
        }
        cursorRegistros.close()
        db.close()
    }

    // Método para verificar si existe un registro diario para un hábito en una fecha específica
    fun existeRegistroDiario(habitoId: Int, fecha: Int): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_REGISTRO_DIARIO WHERE habito_id = ? AND $COLUMN_FECHA = ?"
        val cursor = db.rawQuery(query, arrayOf(habitoId.toString(), fecha.toString()))

        val existe = cursor.count > 0
        cursor.close()
        return existe
    }
    fun obtenerHabitosDelDia(usuarioId: Int, diaSemana: String? = null, fecha: String? = null): List<Habito> {
        val db = this.readableDatabase
        val listaHabitos = mutableListOf<Habito>()

        // Obtener la fecha actual si no se pasa una específica
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val fechaActual = fecha ?: sdf.format(Date())  // Usar la fecha actual si no se proporciona una

        // Obtener el día de la semana correspondiente a la fecha
        val diaDeHoy = diaSemana ?: obtenerDiaSemanaDeFecha(fechaActual)  // Si no se especifica un día, usar el correspondiente a la fecha

        // Query para obtener hábitos que coincidan con el día y la fecha dentro de [fechaInicio, fechaFin]
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM $TABLE_HABITOS WHERE $COLUMN_USUARIO_ID_HABITO = ? AND $COLUMN_DIAS_SEMANALES LIKE ? AND ? BETWEEN $COLUMN_FECHA_INICIO AND $COLUMN_FECHA_FIN",
            arrayOf(usuarioId.toString(), "%$diaDeHoy%", fechaActual)
        )

        if (cursor.moveToFirst()) {
            do {
                val habitoId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HABITO_ID))
                val nombreHabito = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE_HABITO))
                val tipoHabito = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIPO_HABITO))
                val horaInicio = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HORA_INICIO))
                val horaFin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HORA_FIN))

                // Verificar si el hábito ya tiene un registro completado para esa fecha
                val completado = estaCompletadoHoy(habitoId, fechaActual.toInt())

                // Añadir el hábito a la lista con el estado completado
                listaHabitos.add(Habito(habitoId, nombreHabito, tipoHabito, horaInicio, horaFin, completado))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return listaHabitos
    }

    fun registrarHabitosDelDia(usuarioId: Int) {
        val db = this.writableDatabase
        val fechaActual = obtenerFechaActual() // Obtener la fecha actual en formato yyyyMMdd
        val diaActual = obtenerDiaSemanaActual() // Obtener el día de la semana actual (ej. Lunes, Martes, etc.)

        // Obtener todos los hábitos del usuario que estén activos en la fecha actual y correspondan al día de la semana
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM $TABLE_HABITOS WHERE $COLUMN_USUARIO_ID_HABITO = ? AND ? BETWEEN $COLUMN_FECHA_INICIO AND $COLUMN_FECHA_FIN AND $COLUMN_DIAS_SEMANALES LIKE ?",
            arrayOf(usuarioId.toString(), fechaActual.toString(), "%$diaActual%")
        )

        if (cursor.moveToFirst()) {
            do {
                val habitoId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HABITO_ID))

                // Verificar si ya existe un registro para el hábito en la fecha actual
                if (!existeRegistroDiario(habitoId, fechaActual.toInt())) {
                    // Si no existe, crear un nuevo registro para el hábito
                    insertarRegistroDiario(habitoId, fechaActual.toInt(), false, 0)
                }

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
    }



    // Función auxiliar para obtener el día de la semana de una fecha en formato yyyyMMdd
    fun obtenerDiaSemanaDeFecha(fecha: String): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val date = sdf.parse(fecha)
        val cal = Calendar.getInstance()
        cal.time = date

        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Lunes"
            Calendar.TUESDAY -> "Martes"
            Calendar.WEDNESDAY -> "Miércoles"
            Calendar.THURSDAY -> "Jueves"
            Calendar.FRIDAY -> "Viernes"
            Calendar.SATURDAY -> "Sábado"
            Calendar.SUNDAY -> "Domingo"
            else -> ""  // En caso de que algo salga mal
        }
    }



    // Método para verificar si un hábito ha sido completado hoy
    fun estaCompletadoHoy(habitoId: Int, fecha: Int): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_COMPLETADO FROM $TABLE_REGISTRO_DIARIO WHERE $COLUMN_HABITO_ID_REGISTRO = ? AND $COLUMN_FECHA = ?",
            arrayOf(habitoId.toString(), fecha.toString())
        )

        var completado = false
        if (cursor.moveToFirst()) {
            completado = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETADO)) == 1
        }
        cursor.close()
        return completado
    }
    private fun obtenerDiaSemanaActual(): String {
        val calendar = Calendar.getInstance()
        val diasSemana = arrayOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
        val diaActual = calendar.get(Calendar.DAY_OF_WEEK)
        return diasSemana[diaActual - 1]
    }


    // Método para actualizar el estado de completado de un registro diario
    fun actualizarRegistroDiario(habitoId: Int, fecha: Int, completado: Boolean, tiempoDedicado: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_COMPLETADO, if (completado) 1 else 0)
        contentValues.put(COLUMN_TIEMPO_DEDICADO, tiempoDedicado)


        db.update(
            TABLE_REGISTRO_DIARIO,
            contentValues,
            "$COLUMN_HABITO_ID_REGISTRO = ? AND $COLUMN_FECHA = ?",
            arrayOf(habitoId.toString(), fecha.toString())
        )
    }


    // Método para insertar un registro diario
    fun insertarRegistroDiario(habitoId: Int, fecha: Int, completado: Boolean, tiempoDedicado: Int): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(COLUMN_HABITO_ID_REGISTRO, habitoId)
        contentValues.put(COLUMN_FECHA, fecha)
        contentValues.put(COLUMN_COMPLETADO, if (completado) 1 else 0)  // Guardar como 1 si está completado, 0 si no.
        contentValues.put(COLUMN_TIEMPO_DEDICADO, tiempoDedicado.toString())  // Guardar el tiempo dedicado como texto.

        val result = db.insert(TABLE_REGISTRO_DIARIO, null, contentValues)
        db.close()
        return result
    }
    // Obtener la fecha actual en formato yyyyMMdd
    fun obtenerFechaActual(): Int {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date()).toInt()
    }
    fun obtenerRachasDelUsuario(usuarioId: Int): List<Racha> {
        val rachas = mutableListOf<Racha>()
        val db = this.readableDatabase

        val query = """
        SELECT r.$COLUMN_RACHA_ID, r.$COLUMN_HABITO_ID_RACHA, r.$COLUMN_RACHA_ACTUAL, r.$COLUMN_RACHA_MAXIMA, h.$COLUMN_NOMBRE_HABITO, h.$COLUMN_TIPO_HABITO
        FROM $TABLE_RACHAS r
        INNER JOIN $TABLE_HABITOS h ON r.$COLUMN_HABITO_ID_RACHA = h.$COLUMN_HABITO_ID
        WHERE h.$COLUMN_USUARIO_ID_HABITO = ?
    """

        val cursor = db.rawQuery(query, arrayOf(usuarioId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val rachaId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RACHA_ID))
                val habitoId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HABITO_ID_RACHA))
                val rachaActual = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RACHA_ACTUAL))
                val rachaMaxima = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RACHA_MAXIMA))
                val nombreHabito = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE_HABITO))
                val tipoHabito = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIPO_HABITO))

                var tiempoDedicado = 0
                if (tipoHabito == "tiempo") {
                    // Obtener el tiempo total dedicado a este hábito
                    tiempoDedicado = obtenerTiempoDedicadoPorHabito(habitoId)
                }

                // Obtener el conteo de registros completados y totales para este hábito
                val (registrosCompletados, totalRegistros) = obtenerConteoRegistrosPorHabito(habitoId)

                // Agregar la racha a la lista con el nuevo conteo de registros
                rachas.add(Racha(rachaId, habitoId, rachaActual, rachaMaxima, nombreHabito, tiempoDedicado, registrosCompletados, totalRegistros))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return rachas
    }

    // Método para obtener el número total de registros y cuántos de ellos están completados para un hábito
    fun obtenerConteoRegistrosPorHabito(habitoId: Int): Pair<Int, Int> {
        val db = this.readableDatabase

        // Contar el número total de registros del hábito
        val cursorTotal = db.rawQuery(
            "SELECT COUNT(*) AS total FROM $TABLE_REGISTRO_DIARIO WHERE $COLUMN_HABITO_ID_REGISTRO = ?",
            arrayOf(habitoId.toString())
        )
        var totalRegistros = 0
        if (cursorTotal.moveToFirst()) {
            totalRegistros = cursorTotal.getInt(cursorTotal.getColumnIndexOrThrow("total"))
        }
        cursorTotal.close()

        // Contar el número de registros completados del hábito
        val cursorCompletados = db.rawQuery(
            "SELECT COUNT(*) AS completados FROM $TABLE_REGISTRO_DIARIO WHERE $COLUMN_HABITO_ID_REGISTRO = ? AND $COLUMN_COMPLETADO = 1",
            arrayOf(habitoId.toString())
        )
        var registrosCompletados = 0
        if (cursorCompletados.moveToFirst()) {
            registrosCompletados = cursorCompletados.getInt(cursorCompletados.getColumnIndexOrThrow("completados"))
        }
        cursorCompletados.close()

        db.close()

        // Retornar el par de valores (completados, total)
        return Pair(registrosCompletados, totalRegistros)
    }



    // Método para obtener el tiempo total dedicado por un hábito
    fun obtenerTiempoDedicadoPorHabito(habitoId: Int): Int {
        val db = this.readableDatabase
        var tiempoTotal = 0

        // Obtener todos los registros de este hábito
        val cursor = db.rawQuery(
            "SELECT $COLUMN_TIEMPO_DEDICADO FROM $TABLE_REGISTRO_DIARIO WHERE $COLUMN_HABITO_ID_REGISTRO = ?",
            arrayOf(habitoId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val tiempoDedicado = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIEMPO_DEDICADO)).toIntOrNull() ?: 0
                tiempoTotal += tiempoDedicado
            } while (cursor.moveToNext())
        }

        cursor.close()
        return tiempoTotal
    }



    // Método para eliminar todas las tareas de un hábito específico
    fun eliminarTodasLasTareas(habitoId: Int) {
        val db = writableDatabase
        // Elimina todas las tareas que correspondan al hábito
        db.delete("Habitos", "habito_id = ?", arrayOf(habitoId.toString()))
        // También eliminar registros y rachas asociadas
        db.delete("RegistroDiario", "habito_id = ?", arrayOf(habitoId.toString()))
        db.delete("Rachas", "habito_id = ?", arrayOf(habitoId.toString()))
    }

    // Método para obtener hábitos de un usuario específico
    fun getHabitos(usuarioId: Int): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_HABITOS WHERE $COLUMN_USUARIO_ID_HABITO = ?", arrayOf(usuarioId.toString()))
    }

    // Método para obtener todos los usuarios
    fun getAllUsers(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_USUARIOS", null)
    }

    // Método para obtener el ID del usuario actual
    fun obtenerUsuarioIdActual(): Int {
        return sharedPreferences.getInt("usuario_id", -1) // -1 si no hay usuario autenticado
    }

    fun almacenarUsuarioId(usuarioId: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("usuario_id", usuarioId)
        editor.apply()
    }
}
