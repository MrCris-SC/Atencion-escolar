package com.example.atencionescolar.activities
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.atencionescolar.Firebase.FirestoreClass
import com.example.atencionescolar.ImageAdapter
import com.example.atencionescolar.R
import com.example.atencionescolar.model.Comment
import com.example.atencionescolar.utils.CommentAdapter
import com.example.atencionescolar.utils.Constants

class datos_activity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvAsunto: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var rvComments: RecyclerView
    private lateinit var etNewComment: EditText
    private lateinit var btnSendComment: Button
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var rvImages: RecyclerView
    private lateinit var imageAdapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos)

        //Declaracion de las variables para poder acceder a los elementos del layout correspondiente
        tvUserName = findViewById(R.id.dt_username)
        tvAsunto = findViewById(R.id.dt_asunto)
        tvDescripcion = findViewById(R.id.dt_descripcion)
        rvComments = findViewById(R.id.rv_comments)
        etNewComment = findViewById(R.id.et_new_comment)
        btnSendComment = findViewById(R.id.btn_send_comment)
        rvImages = findViewById(R.id.dt_recyclerView)
        val btnSalir: Button = findViewById(R.id.btn_salir)

        val quejaId = intent.getStringExtra("documentId") ?: return
        val userType = intent.getStringExtra("userType") ?: return

        rvComments.layoutManager = LinearLayoutManager(this)
        commentAdapter = CommentAdapter(this, mutableListOf())
        rvComments.adapter = commentAdapter

        rvImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = ImageAdapter(this, mutableListOf())
        rvImages.adapter = imageAdapter

        btnSalir.setOnClickListener {
            ExitConfirmationDialog()
        }

        //Aqui se cargan los datos de la queja seleccionada
        FirestoreClass().getQuejaById(
            quejaId,
            onComplete = { queja ->
                tvAsunto.text = queja.asunto
                tvDescripcion.text = queja.descripcion
                commentAdapter.addComments(queja.comments)
                imageAdapter.updateImages(queja.imageUrls)

                //Llamada a la funcion en clase FirestoreClass, se obtiene el Id Del usuario
                FirestoreClass().getUserById(
                    queja.userId,
                    onComplete = { user ->
                        val userName = user.name
                        tvUserName.text = userName
                        btnSendComment.setOnClickListener {
                            val newCommentText = etNewComment.text.toString().trim()
                            if (newCommentText.isNotEmpty()) {
                                val newComment = Comment(
                                    userId = queja.userId,
                                    userName = if (userType == "admin") "Admin" else userName,
                                    comment = newCommentText
                                )
                                queja.comments.add(newComment)

                                //Referencia a la clase firestore
                                FirestoreClass().updateQuejaComments(quejaId, queja.comments,
                                    onComplete = {
                                        commentAdapter.addComment(newComment)
                                        etNewComment.text.clear()
                                    },
                                    onFailure = { exception ->
                                        Log.e("datos_activity", "Error adding comment", exception)
                                    }
                                )
                            }
                        }
                    },
                    onFailure = { exception ->
                        Log.e("datos_activity", "Error fetching user details", exception)
                    }
                )

            },
            onFailure = { exception ->
                Log.e("datos_activity", "Error fetching queja details", exception)
            }
        )
    }

    //Dialogo de confirmacion para salir
    private fun ExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Salir")
            .setMessage("¿Estás seguro de que quieres volver?")
            .setPositiveButton("Sí") { dialog, which ->
                val sharedPreferences = getSharedPreferences(Constants.PROGEMANAG_PREFERENCES, Context.MODE_PRIVATE)
                val userType = sharedPreferences.getString(Constants.USER_TYPE, "")

                val intent = when (userType) {
                    "admin" -> Intent(this, PrincipalAdmin::class.java)
                    "user" -> Intent(this, PrincipalUsuario::class.java)
                    else -> Intent(this, Inicio::class.java)
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
