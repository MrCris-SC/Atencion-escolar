package com.example.atencionescolar.activities

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import com.example.atencionescolar.FragmentosAdministrador.InicioFrag
import com.example.atencionescolar.FragmentosUsuario.PerfilUsuario
import com.example.atencionescolar.InicioUsuario

import com.example.atencionescolar.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

import android.Manifest;
import android.content.Context
import android.content.SharedPreferences
import com.example.atencionescolar.Firebase.FirestoreClass
import com.example.atencionescolar.utils.Constants


class PrincipalUsuario : BaseActivity(), NavigationView.OnNavigationItemSelectedListener{

    private lateinit var mSharedPreferences: SharedPreferences

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
        } else {
            Log.d(TAG, "Notification permission denied")
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission already granted")
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Show an educational UI to explain why the permission is needed
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal_usuario)

        //val drawer_layout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar : Toolbar = findViewById(R.id.toolbar_main_activity)
        setupActionBar(toolbar)
        val nav_viewa = findViewById<NavigationView>(R.id.nav_viewB)
        nav_viewa.setNavigationItemSelectedListener(this)
        val fab: FloatingActionButton = findViewById(R.id.button_add)

        mSharedPreferences =
            this.getSharedPreferences(Constants.PROGEMANAG_PREFERENCES, MODE_PRIVATE)
// Por ejemplo, en PrincipalUsuario o PrincipalAdmin
        FirestoreClass().getUserDetails(this, FirestoreClass().getCurrentUserID(), { user ->
            saveUserType(user.userType)
        }, { error ->
            Log.e("Error", "Failed to get user type")
        })

        // Variable is used get the value either token is updated in the database or not.
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)
        askNotificationPermission()
        if (tokenUpdated) {
            // Get the current logged in user details.
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this@PrincipalUsuario, true)
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


        if (savedInstanceState == null) {
            val fragment = InicioUsuario()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
        fab.setOnClickListener {
            // Lógica que se ejecutará cuando se haga clic en el botón flotante
            // Por ejemplo, puedes abrir una nueva actividad aquí
            val intent = Intent(this, CrearQueja::class.java)
            startActivity(intent)
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and handle the token as needed
            Log.d(TAG, "FCM token: $token")
        }

    }
    // Declare the launcher at the top of your Activity/Fragment:


    override fun onBackPressed() {
        super.onBackPressed()
        val drawer_layout = findViewById<DrawerLayout>(R.id.drawer_layoutB)
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            // A double back press function is added in Base Activity.
            doubleBackToExit()
        }
    }
    // END

    // TODO (Step 7: Implement members of NavigationView.OnNavigationItemSelectedListener.)
    // START
    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        // TODO (Step 9: Add the click events of navigation menu items.)
        // START
        val drawer_layout: DrawerLayout = findViewById(R.id.drawer_layoutB)
        when (menuItem.itemId) {
            R.id.PerfilUser -> {
                // Reemplaza PendientesFragment por el nombre de tu fragmento

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PerfilUsuario())
                    .commit()
            }

            R.id.InicioUser -> {
                // Reemplaza InicioFragment por el nombre de tu fragmento

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, InicioUsuario())
                    .commit()
            }
            R.id.SalirUser -> {

                showLogoutConfirmationDialog()
                true
            }

        }
        drawer_layout.closeDrawer(GravityCompat.START)
        // END
        return true
    }

    //Guarda el tipo de usuario al iniciar sesión
    fun saveUserType(userType: String) {
        val sharedPreferences = getSharedPreferences(Constants.PROGEMANAG_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(Constants.USER_TYPE, userType)
        editor.apply()
    }
    // END

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

    //Metodo para cerrar sesión
    private fun logout() {
        //Llamada al metodo en FirestoreClass
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, Intro::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
    private fun setupActionBar(toolbar: Toolbar) {

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }

    }
    private fun toggleDrawer() {

        val drawer_layout = findViewById<DrawerLayout>(R.id.drawer_layoutB)
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    fun tokenUpdateSuccess() {

        hideProgressDialog()

        // Here we have added a another value in shared preference that the token is updated in the database successfully.
        // So we don't need to update it every time.
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        // Obtiene la informacion del usuario actual
        // Muestra el dialogo de espera
        showProgressDialog(resources.getString(R.string.please_wait))
        //Llamada al metodo en FirestoreClass
        FirestoreClass().loadUserData(this@PrincipalUsuario, true)
    }

    //Actualiza el token de usaurio en firebase
    private fun updateFCMToken(token: String) {

        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        // Actualiza los datos en la base de datos
        // Muestra el dialogo de progreso
        showProgressDialog(resources.getString(R.string.please_wait))
        //Llamada al metodo en FirestoreClass
        FirestoreClass().updateUserProfileData(this@PrincipalUsuario, userHashMap)
        hideProgressDialog()
    }
}