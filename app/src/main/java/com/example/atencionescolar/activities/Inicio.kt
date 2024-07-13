package com.example.atencionescolar.activities


import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import com.google.firebase.auth.FirebaseAuth
import com.example.atencionescolar.Firebase.FirestoreClass
import com.example.atencionescolar.model.User
import com.example.atencionescolar.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class Inicio : BaseActivity() {
    private var isPasswordVisible = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        val etPassword = findViewById<TextInputEditText>(R.id.et_password)
        val tilPassword = findViewById<TextInputLayout>(R.id.til_password)

        tilPassword.setEndIconOnClickListener {
            if (isPasswordVisible) {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                tilPassword.setEndIconDrawable(R.drawable.ic_visibility_off)
                isPasswordVisible = false
            } else {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                tilPassword.setEndIconDrawable(R.drawable.ic_visibility)
                isPasswordVisible = true
            }
            // Mover el cursor al final del texto
            etPassword.setSelection(etPassword.text?.length ?: 0)
        }


        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()
        val btn_sign_in : Button = findViewById(R.id.btn_sign_in)
        btn_sign_in.setOnClickListener {
            signInRegisteredUser()
        }
    }

    //COntrolador del actionBar
    private fun setupActionBar() {
        val toolbar_inicio : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_sign_in_activity)
        setSupportActionBar(toolbar_inicio)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar_inicio.setNavigationOnClickListener { onBackPressed() }
    }

    /**
     * Funcion para iniciar sesion usando correo y contraseña.
     */
    private fun signInRegisteredUser() {
        // Here we get the text from editText and trim the space
        val et_email : AppCompatEditText = findViewById(R.id.et_email)
        val et_password : AppCompatEditText = findViewById(R.id.et_password)

        val email: String = et_email.text.toString().trim { it <= ' ' }
        val password: String = et_password.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            // Inicia sesion usando FirebaseAuth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Calling the FirestoreClass signInUser function to get the data of user from database.
                        val getBoardsList : Boolean
                        FirestoreClass().loadUserData(this@Inicio)
                    } else {
                        Toast.makeText(
                            this@Inicio,
                            task.exception!!.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    /**
     *Valida el formulario
     */
    private fun validateForm(email: String, password: String): Boolean {
        return if (TextUtils.isEmpty(email)) {
            showErrorSnackBar("Please enter email.")
            false
        } else if (TextUtils.isEmpty(password)) {
            showErrorSnackBar("Please enter password.")
            false
        } else {
            true
        }
    }

    //Redirige al usuario en la actividad correspondiente
    fun signInSuccess(user: User) {

        hideProgressDialog()

        startActivity(Intent(this@Inicio, PrincipalUsuario::class.java))
        this.finish()
    }

    //Redirecciona al usuario en la actividad correspondiente
    fun redirectToAdminLayout() {
        // Redirigir a la vista del administrador
        val intent = Intent(this@Inicio, PrincipalAdmin::class.java)
        startActivity(intent)
        finish()
    }
}