package com.example.atencionescolar.FragmentosAdministrador

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.atencionescolar.Firebase.FirestoreClass
import com.example.atencionescolar.QuejaAdapter
import com.example.atencionescolar.R
import com.example.atencionescolar.model.Queja


// Fragmento para cargar las quejas, aqui se controlan los elementos como el recicler View entre otros

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class InicioFrag : Fragment() {
    private lateinit var quejaAdapter: QuejaAdapter
    private lateinit var quejaRecyclerView: RecyclerView
    private val userType: String = "admin"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_blank, container, false)

        quejaRecyclerView = view.findViewById(R.id.adminINI_recycler_view)
        quejaRecyclerView.layoutManager = LinearLayoutManager(context)
        quejaAdapter = QuejaAdapter(requireContext(), arrayListOf(), userType)
        quejaRecyclerView.adapter = quejaAdapter


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        getQuejasList()

    }

    //Metodo para cargar todas las quejas, aqui se hace la llamada al metodo correspondiente en firestore Class
    private fun getQuejasList() {
        FirestoreClass().getAllComplaints(
            onComplete = { quejasList ->
                populateQuejasListToUI(quejasList)
            },
            onFailure = { e ->
                Log.e("InicioFrag", "Error fetching complaints", e)
            }
        )
    }

    private fun populateQuejasListToUI(quejasList: ArrayList<Queja>) {
        quejaAdapter.updateList(quejasList)
    }
}
