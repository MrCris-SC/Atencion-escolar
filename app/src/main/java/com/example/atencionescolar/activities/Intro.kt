package com.example.atencionescolar.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.atencionescolar.R

class Intro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val btn_inicio =  findViewById<Button>(R.id.btn_inicio)
        val btn_registrate = findViewById<Button>(R.id.btn_Registrate)
        btn_inicio.setOnClickListener {

            // Lanza la actividad de inicio.
            startActivity(Intent(this@Intro, Inicio::class.java))
        }

        btn_registrate.setOnClickListener {

            //Lanza la actividad de registro
            startActivity(Intent(this@Intro, Registrate::class.java))
        }
    }
}