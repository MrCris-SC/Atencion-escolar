package com.example.atencionescolar.Firebase

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import com.example.atencionescolar.activities.AgregarAdmin
import com.example.atencionescolar.activities.Inicio
import com.example.atencionescolar.model.Queja
import com.example.atencionescolar.model.User
import com.example.atencionescolar.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.example.atencionescolar.activities.Registrate
import com.example.atencionescolar.model.Comment

class FirestoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    //Metodo para registrar usuario en firebase
    fun registerUser(activity: Registrate, userInfo: User) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error writing document", e)
            }
    }

    private val firestoreDB = FirebaseFirestore.getInstance()

    //Metodo para registrar admin en firebase
    fun registerAdmin(context: Context, admin: User, onComplete: () -> Unit) {
        // Guardar el usuario en Firestore
        firestoreDB.collection("users")
            .document(admin.id)
            .set(admin, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "User registered successfully: ${admin.id}")
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error registering user", e)
                // Manejo de errores: mostrar un mensaje o tomar alguna acción
            }
    }

    //Metodo para obtener el id de usuario actual
    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: ""
    }

    //Metodo para cargar la info del usuario y dirigirlo a su activity correspondiente
    fun loadUserData(activity: Activity, readBoardsList: Boolean = false) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())
                val loggedInUser = document.toObject(User::class.java)!!
                when (activity) {
                    is Inicio -> {
                        if (loggedInUser.userType == "admin") {
                            activity.redirectToAdminLayout()
                        } else {
                            activity.signInSuccess(loggedInUser)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is Inicio -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error al obtener los datos de usuario", e)
            }
    }

    //Metodo para obtener las quejas para cada usuario
    fun getUserComplaints(onComplete: (ArrayList<Queja>) -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("complaints")
            .whereEqualTo("userId", getCurrentUserID())
            .get()
            .addOnSuccessListener { documents ->
                val quejasList = ArrayList<Queja>()
                for (document in documents) {
                    val queja = document.toObject(Queja::class.java)
                    queja.id = document.id
                    quejasList.add(queja)
                }
                onComplete(quejasList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    //Metodo para que el admin obtenga todas las quejas de todos los usuarios
    fun getAllComplaints(onComplete: (ArrayList<Queja>) -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("complaints")
            .get()
            .addOnSuccessListener { documents ->
                val quejasList = ArrayList<Queja>()
                for (document in documents) {
                    val queja = document.toObject(Queja::class.java)
                    queja.id = document.id
                    quejasList.add(queja)
                }
                onComplete(quejasList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    //Metodo para obtener detalles del usuario, este metodo es para cualquie clase que requiera mostrar detalles
    // o necesite informacion del usuario
    fun getUserDetails(activity: Activity, userId: String, onComplete: (User) -> Unit, onFailure: (Exception) -> Unit) {
        mFireStore.collection(Constants.USERS)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject(User::class.java)!!
                    onComplete(user)
                } else {
                    Log.e(activity.javaClass.simpleName, "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error getting user details", e)
                onFailure(e)
            }
    }

    //Metodo para hacer la llamada a una queja en especifico
    fun getQuejaById(quejaId: String, onComplete: (Queja) -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("complaints")
            .document(quejaId)
            .get()
            .addOnSuccessListener { document ->
                val queja = document.toObject(Queja::class.java)!!
                queja.id = document.id // Agrega el ID del documento a la queja
                onComplete(queja)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {

        mFireStore.collection(Constants.USERS) // Collection Name
            .document(getCurrentUserID()) // Document ID
            .update(userHashMap) // A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Data updated successfully!")

                // Notify the success result.


            }
            .addOnFailureListener { e ->

                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board."
                )
        }


    //Metodo para obtener el usuario mediante su ID
    fun getUserById(userId: String, onComplete: (User) -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)!!
                onComplete(user)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    //Metodo para agregar un comentario a la queja correspondiente
    fun addResponseToQueja(quejaId: String, response: String, onComplete: () -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("complaints")
            .document(quejaId)
            .update("adminResponse", response)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    //Metodo para actualizar los comentarios
    fun updateQuejaComments(quejaId: String, comments: List<Comment>, onComplete: () -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("complaints")
            .document(quejaId)
            .update("comments", comments)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    companion object {
        private const val TAG = "FirestoreClass"
    }
}


