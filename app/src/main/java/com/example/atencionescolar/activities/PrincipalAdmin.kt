package com.example.atencionescolar.activities

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import com.example.atencionescolar.Firebase.FirestoreClass
import com.example.atencionescolar.FragmentosAdministrador.InicioFrag
import com.example.atencionescolar.FragmentosAdministrador.PendientesFrag
import com.example.atencionescolar.FragmentosUsuario.PerfilUsuario
import com.example.atencionescolar.InicioUsuario
import com.example.atencionescolar.R
import com.example.atencionescolar.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging


class PrincipalAdmin : BaseActivity(), NavigationView.OnNavigationItemSelectedListener  {

    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        askNotificationPermission()
        //val drawer_layout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar : Toolbar = findViewById(R.id.toolbar_main_activity)
        setupActionBar(toolbar)
        val nav_viewa = findViewById<NavigationView>(R.id.nav_viewa)
        nav_viewa.setNavigationItemSelectedListener(this)
        val fab: FloatingActionButton = findViewById(R.id.button_add)
        if (savedInstanceState == null) {
            val fragment = InicioUsuario()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
        fab.setOnClickListener {
            // Lógica que se ejecutará cuando se haga clic en el botón flotante
            val intent = Intent(this, CrearQueja::class.java)
            startActivity(intent)
        }
        mSharedPreferences =
            this.getSharedPreferences(Constants.PROGEMANAG_PREFERENCES, MODE_PRIVATE)
        FirestoreClass().getUserDetails(this, FirestoreClass().getCurrentUserID(), { user ->
            saveUserType(user.userType)
        }, { error ->
            Log.e("Error", "Failed to get user type")
        })
        //Variable usada para saber si el token de usuario se a actualizado o no
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if (tokenUpdated) {
            // Obtiene los detalles del usuario loggeado
            // Muestra el progress Dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this@PrincipalAdmin, true)
        } else {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result

                // Log and update the token
                Log.d(TAG, "FCM Registration Token: $token")
                updateFCMToken(token)
            }
        }


    }

    //Al dar doble click muestra advertencia de salida
    override fun onBackPressed() {
        super.onBackPressed()
        val drawer_layout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            // A double back press function is added in Base Activity.
            doubleBackToExit()

        }
    }
    // END

    // Implementacion del controlador del menu.)
    // START
    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        // TODO (Step 9: Add the click events of navigation menu items.)
        // START
        val drawer_layout: DrawerLayout = findViewById(R.id.drawer_layout)
        when (menuItem.itemId) {
            R.id.InicioAdmin -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, InicioFrag())
                    .commit()
            }

            R.id.PendientesAdmin -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PendientesFrag())
                    .commit()
            }
            R.id.SalirAdmin -> {

                showLogoutConfirmationDialog()
                true
            }
            R.id.RegistrarAdmin -> {
                val intent = Intent(this, AgregarAdmin::class.java)
                startActivity(intent)
            }

        }
        drawer_layout.closeDrawer(GravityCompat.START)
        // FIN
        return true
    }
    // Fin del controlador

    private val requestPermissionLauncher = this.registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Solicida el permiso al telefono si este tiene restricciones.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    //Metodo para lanzar la notificacion de permiso al usuario
    private fun askNotificationPermission() {
        // Necesario para sdk 33
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {

            } else {
                //
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    //Dialogo de confirmacion para cerrar sesión
    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Cerrar Sesión")
            setMessage("¿Estás seguro de que quieres cerrar sesión?")
            setPositiveButton("Sí") { dialog, _ ->
                logout()
                dialog.dismiss()
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    //Guarda el tipo de usario al iniciar secion, esto para las solicitudes en segundo plano
    fun saveUserType(userType: String) {
        val sharedPreferences = getSharedPreferences(Constants.PROGEMANAG_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(Constants.USER_TYPE, userType)
        editor.apply()
    }

    //Metodo para cerrar sesion
    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, Intro::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    //Controlador del ActionBar
    private fun setupActionBar(toolbar: Toolbar) {

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }

    }


    private fun toggleDrawer() {

        val drawer_layout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    //Metodo para guardar el token de usaurio al momento de actualizarlo
    fun tokenUpdateSuccess() {



        // Here we have added a another value in shared preference that the token is updated in the database successfully.
        // So we don't need to update it every time.
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        // Get the current logged in user details.
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this@PrincipalAdmin, true)
        hideProgressDialog()
    }

    //Metodo para actualizar el token de usuario en firebase
    private fun updateFCMToken(token: String) {

        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        // Update the data in the database.
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this@PrincipalAdmin, userHashMap)
        hideProgressDialog()
    }
}