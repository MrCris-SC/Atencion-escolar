package com.example.atencionescolar.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.atencionescolar.AttachmentAdapter
import com.example.atencionescolar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class CrearQueja : AppCompatActivity() {
    private val attachmentList = mutableListOf<Uri>()
    private lateinit var attachmentAdapter: AttachmentAdapter
    private lateinit var etAsunto: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var tvCharCount: TextView
    private var uploadedImageUrls = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_queja)

        //Declaracion de variables para los elementos en el layout correspondiente
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val attachButton: Button = findViewById(R.id.attachButton)
        val btnUpload: Button = findViewById(R.id.btn_subir)
        val btnSalir: Button = findViewById(R.id.btn_salirC)

        tvCharCount = findViewById(R.id.tv_char_count)

        etAsunto = findViewById(R.id.et_asunto)
        etDescripcion = findViewById(R.id.et_descripcionR)


        etDescripcion.filters = arrayOf(InputFilter.LengthFilter(400))
        etDescripcion.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvCharCount.text = "${s?.length}/400"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        attachmentAdapter = AttachmentAdapter(this, attachmentList)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = attachmentAdapter

        btnSalir.setOnClickListener {
            showExitConfirmationDialog()
        }

        //Metodo para controlar el click del boton para seleccionar multimedia
        attachButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/* video/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            startActivityForResult(intent, PICK_ATTACHMENT_REQUEST)
        }

        //Aqui se suben todos los archivos
        btnUpload.setOnClickListener {
            //llamada al metodo principal
            uploadAllFiles()
        }
    }

    //Dialogo de confirmacion de salida
    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Salir")
            .setMessage("¿Estás seguro de que quieres volver? Se perderá el contenido de la queja si no se ha enviado.")
            .setPositiveButton("Sí") { dialog, which ->
                val intent = Intent(this, PrincipalUsuario::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    //Metodo para controlar el peso de los archivos subidos, establece un limite de 50 mb
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_ATTACHMENT_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedFileUri: Uri? = data.data
            if (selectedFileUri != null) {
                val fileSize = getFileSize(selectedFileUri)
                if (fileSize <= MAX_FILE_SIZE_MB) {
                    attachmentList.add(selectedFileUri)
                    attachmentAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "El archivo es demasiado grande. El tamaño máximo permitido es 30MB.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    //Metodo para obtener el peso del contenido multimedia
    private fun getFileSize(uri: Uri): Long {
        var fileSize: Long = 0
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            fileSize = cursor.getLong(sizeIndex)
        }
        return fileSize / (1024 * 1024) // Convertir a MB
    }

    //Metodo para subir la queja a Firebase
    private fun uploadAllFiles() {
        if (attachmentList.isNotEmpty()) {
            showProgressDialog("Subiendo archivos, por favor espera...")
            for (uri in attachmentList) {
                val fileSize = getFileSize(uri)
                if (fileSize <= MAX_FILE_SIZE_MB) {
                    //Llamada al metodo para subir los archivos, se le pasa la uri de las imagenes
                    // y contenido multimedia
                    uploadFileToFirebaseStorage(uri)
                } else {
                    Toast.makeText(this, "El archivo es demasiado grande. El tamaño máximo permitido es 30MB.", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            //Guarda la queja en la tabla
            saveComplaintToFirestore()
        }
    }

    //Metodo para poder almacenar las imagenes y videos en Storage, obtiene la uri de cada archivo
    private fun uploadFileToFirebaseStorage(fileUri: Uri) {
        val fileName = UUID.randomUUID().toString()
        val storageReference = FirebaseStorage.getInstance().reference.child("attachments/$fileName")

        storageReference.putFile(fileUri)
            .addOnSuccessListener { taskSnapshot ->
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    uploadedImageUrls.add(downloadUrl)
                    if (uploadedImageUrls.size == attachmentList.size) {
                        saveComplaintToFirestore()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload ${fileUri.lastPathSegment}", Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }
    }

    //Metodo para guardar una queja en la tabla de Firestore
    private fun saveComplaintToFirestore() {
        val asunto = etAsunto.text.toString()
        val descripcion = etDescripcion.text.toString()

        if (asunto.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            hideProgressDialog()
            return
        }

        val complaintId = UUID.randomUUID().toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        //Aqui se obtiene la intancia
        val db = FirebaseFirestore.getInstance()
        val data = HashMap<String, Any>()
        data["documentId"] = complaintId
        data["userId"] = userId
        data["asunto"] = asunto
        data["descripcion"] = descripcion
        data["imageUrls"] = uploadedImageUrls

        //Seccion para poder controlar de manera correcta la subida de la queja
        db.collection("complaints")
            .document(complaintId)
            .set(data)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Queja enviada con éxito", Toast.LENGTH_SHORT).show()
                hideProgressDialog()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al enviar la queja", Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }
    }

    private fun showProgressDialog(message: String) {
        // Muestra el progress Dialog
    }

    private fun hideProgressDialog() {
        // Oculta el progress Dialog
    }

    companion object {
        private const val PICK_ATTACHMENT_REQUEST = 1
        private const val MAX_FILE_SIZE_MB = 30
    }
}