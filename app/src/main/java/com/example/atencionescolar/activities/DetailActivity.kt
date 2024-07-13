package com.example.atencionescolar.activities

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.atencionescolar.R
class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val mediaUrl = intent.getStringExtra("MEDIA_URL")

        val imageView: ImageView = findViewById(R.id.imageViewD)
        val videoView: VideoView = findViewById(R.id.videoViewD)

        if (mediaUrl != null) {
            if (mediaUrl.endsWith(".mp4")) {
                imageView.visibility = View.GONE
                videoView.visibility = View.VISIBLE

                // Configurar los controles del VideoView
                val mediaController = MediaController(this)
                mediaController.setAnchorView(videoView)
                videoView.setMediaController(mediaController)

                // Establecer la URL del video
                videoView.setVideoURI(Uri.parse(mediaUrl))

                // Listener para comenzar la reproducción cuando esté listo
                videoView.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.start()
                }

                // Listener para manejar errores de reproducción
                videoView.setOnErrorListener { mediaPlayer, what, extra ->
                    // Manejar el error
                    Toast.makeText(this, "Error al reproducir el video", Toast.LENGTH_SHORT).show()
                    true
                }

                // Listener para comprobar el estado de buffering
                videoView.setOnInfoListener { mp, what, extra ->
                    when (what) {
                        MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                            // Mostrar un indicador de carga, si es necesario
                        }
                        MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                            // Ocultar el indicador de carga
                        }
                    }
                    true
                }

                // Listener para eventos de completitud del video
                videoView.setOnCompletionListener {
                    // Acción cuando el video termine, si es necesario
                }

                // Iniciar la reproducción del video
                videoView.requestFocus()
                videoView.start()
            } else {
                videoView.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                Glide.with(this)
                    .load(mediaUrl)
                    .into(imageView)
            }
        } else {
            // Manejar el caso donde mediaUrl es null
            Toast.makeText(this, "No se pudo cargar el contenido multimedia", Toast.LENGTH_SHORT).show()
        }
    }
}
