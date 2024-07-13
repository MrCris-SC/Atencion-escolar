package com.example.atencionescolar.activities
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.example.atencionescolar.R
import com.example.atencionescolar.databinding.DialogProgressBinding


//En esta clase se encuentran funciones reutilizables para las distintas actividades para los usuarios
open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false

    /**
     * Instancia para utilizar el progress Dialog
     */
    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * FUncion para mostrar el ProgressDialog
     */
    fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(this)

        val binding = DialogProgressBinding.inflate(layoutInflater)
        mProgressDialog.setContentView(binding.root)
        binding.tvProgressText.text = text


        //Muestra el dialogo
        mProgressDialog.show()
    }
    /**
     * This function is used to dismiss the progress dialog if it is visible to user.
     */
    fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }

    //Funcion para obtener el Id del usuario actual mediante una llamada a FirebaseAuth (Servcio)
    // aqui no se usa una clase
    fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    //Funcion para salir de la app con pulsar dos veces el boton de regreso
    fun doubleBackToExit() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true

        //Muestra el toast al usuario
        Toast.makeText(
            this,
            resources.getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_SHORT
        ).show()

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }


    fun showErrorSnackBar(message: String) {
        val snackBar =
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(
            ContextCompat.getColor(
                this@BaseActivity,
                R.color.snackbar_error_color
            )
        )
        snackBar.show()
    }
}