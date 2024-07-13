package com.example.atencionescolar.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.atencionescolar.Firebase.FirestoreClass

import com.example.atencionescolar.R

class respuesta : AppCompatActivity() {

    private lateinit var tvDestinatario: TextView
    private lateinit var tvAsunto: TextView
    private lateinit var etRespuesta: EditText
    private lateinit var btnEnviar: Button
    private lateinit var btnSalir: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_respuesta)

        tvDestinatario = findViewById(R.id.tv_destinoR)
        tvAsunto = findViewById(R.id.dt_asuntoR)
        etRespuesta = findViewById(R.id.et_descripcionR1)
        btnEnviar = findViewById(R.id.btn_respuesta)
        btnSalir = findViewById(R.id.btn_salir)

        val userName = intent.getStringExtra("userName")
        val asunto = intent.getStringExtra("asunto")
        val quejaId = intent.getStringExtra("quejaId")

        tvDestinatario.text = userName
        tvAsunto.text = asunto

        btnSalir.setOnClickListener {
            finish()
        }

        btnEnviar.setOnClickListener {
            val respuesta = etRespuesta.text.toString().trim()

            if (respuesta.isNotEmpty()) {
                FirestoreClass().addResponseToQueja(quejaId!!, respuesta, onComplete = {
                    Toast.makeText(this, "Respuesta enviada exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }, onFailure = { exception ->
                    Log.e("respuesta", "Error enviando la respuesta", exception)
                    Toast.makeText(this, "Error enviando la respuesta", Toast.LENGTH_SHORT).show()
                })
            } else {
                Toast.makeText(this, "La respuesta no puede estar vacía", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
