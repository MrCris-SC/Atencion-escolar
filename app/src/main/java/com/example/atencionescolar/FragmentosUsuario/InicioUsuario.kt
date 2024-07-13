package com.example.atencionescolar

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.atencionescolar.Firebase.FirestoreClass
import com.example.atencionescolar.activities.datos_activity
import com.example.atencionescolar.model.Queja
import com.google.firebase.messaging.FirebaseMessaging

class InicioUsuario : Fragment() {
    private lateinit var quejaAdapter: QuejaAdapter
    private lateinit var quejaRecyclerView: RecyclerView
    private val userType: String = "user"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inicio_usuario, container, false)

        quejaRecyclerView = view.findViewById(R.id.queja_recycler_view)
        quejaRecyclerView.layoutManager = LinearLayoutManager(context)
        quejaAdapter = QuejaAdapter(requireContext(), arrayListOf(), userType)
        quejaRecyclerView.adapter = quejaAdapter

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                // Aquí puedes hacer lo que quieras con el FCM Token, como enviarlo al servidor
                // Por ejemplo, puedes almacenarlo en la base de datos junto con la información del usuario
                // o enviarlo al servidor como parte de una solicitud de actualización del perfil del usuario.
            } else {
                Log.e(TAG, "Error al obtener el FCM Token: ", task.exception)
            }
        }


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getQuejasList()
    }

    private fun getQuejasList() {
        FirestoreClass().getUserComplaints(
            onComplete = { quejasList ->
                populateQuejasListToUI(quejasList)
            },
            onFailure = { e ->
                Log.e("InicioUsuario", "Error fetching complaints", e)
            }
        )
    }

    private fun populateQuejasListToUI(quejasList: ArrayList<Queja>) {
        quejaAdapter.updateList(quejasList)
    }
}
