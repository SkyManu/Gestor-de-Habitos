package com.example.gestortareas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gestortareas.database.DatabaseHelper

class LoginFragment : Fragment() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el diseño para el fragmento
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        // Inicializar elementos de la interfaz
        editTextEmail = view.findViewById(R.id.edit_text_email)
        editTextPassword = view.findViewById(R.id.edit_text_password)
        buttonLogin = view.findViewById(R.id.button_login)

        // Inicializar DatabaseHelper
        dbHelper = DatabaseHelper(requireContext())

        // Configurar el botón de inicio de sesión
        buttonLogin.setOnClickListener { login() }

        return view
    }

    private fun login() {
        dbHelper.addUser("a","a","a")
        dbHelper.addUser("b","b","b")

        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()

        // Verificar si el correo y la contraseña son correctos
        if (dbHelper.authenticateUser(email, password)) {
            // Si las credenciales son válidas, transicionar al fragmento de tareas
            val tareasFragment = TareasFragment()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, tareasFragment)
            transaction.addToBackStack(null)
            transaction.commit()

            // Mostrar la barra de navegación
            (activity as MainActivity).mostrarNavegacion()
        } else {
            Toast.makeText(requireContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
        }
    }
}
