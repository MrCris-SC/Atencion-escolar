package com.example.atencionescolar.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.atencionescolar.Firebase.FirestoreClass
import com.example.atencionescolar.R
import com.example.atencionescolar.model.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class AgregarAdmin : AppCompatActivity() {

    private lateinit var firestoreClass: FirestoreClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agregar_admin)

        //Crea la referencia a la clase de rifestore
        firestoreClass = FirestoreClass()

        val btnRegister: Button = findViewById(R.id.btn_registrarAdmin)
        btnRegister.setOnClickListener {
            registerAdmin()
        }
    }

    //Registra al administrador
    private fun registerAdmin() {
        val etEmail: TextInputEditText = findViewById(R.id.et_emailAdmin)
        val etName: TextInputEditText = findViewById(R.id.et_nameAdmin)
        val etPassword: TextInputEditText = findViewById(R.id.et_passwordAdmin)
        val name: String = etName.text.toString().trim()
        val email: String = etEmail.text.toString().trim()
        val password: String = etPassword.text.toString().trim()

        if (validateForm(name, email, password)) {
            showProgressDialog("Registrando administrador...")
            //Usa la funcion de FirebaseAuth para obtener la instancia y registrar al usuario
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser? = task.result?.user
                        firebaseUser?.let {
                            val admin = User(
                                id = it.uid,
                                name = name,
                                email = email,
                                userType = "admin" // Asigna el tipo de usuario como "admin"
                            )
                            // Llamada al metodo en FirestoreClass
                            firestoreClass.registerAdmin(this@AgregarAdmin, admin) {
                                hideProgressDialog()
                                userRegisteredSuccess()
                            }
                        }
                    } else {
                        hideProgressDialog()
                        Toast.makeText(
                            this@AgregarAdmin,
                            "Error: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    //Valida el formulario
    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            name.isEmpty() -> {
                showErrorSnackBar("Por favor ingresa un nombre.")
                false
            }
            email.isEmpty() -> {
                showErrorSnackBar("Por favor ingresa un correo electrónico.")
                false
            }
            password.isEmpty() -> {
                showErrorSnackBar("Por favor ingresa una contraseña.")
                false
            }
            else -> true
        }
    }

    private fun showProgressDialog(message: String) {
        // Mostrar un diálogo de progreso
    }

    private fun hideProgressDialog() {
        // Ocultar el diálogo de progreso
    }

    private fun showErrorSnackBar(message: String) {
        // Mostrar un Snackbar de error
    }
   //Cuando se complete el registro--
    private fun userRegisteredSuccess() {
        Toast.makeText(
            this@AgregarAdmin,
            "Administrador registrado correctamente.",
            Toast.LENGTH_SHORT
        ).show()

        // Aquí puedes cerrar la actividad o tomar otra acción
        finish()
    }
}

