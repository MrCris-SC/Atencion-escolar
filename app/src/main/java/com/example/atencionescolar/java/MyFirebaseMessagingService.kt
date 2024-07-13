package com.example.atencionescolar.java

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.atencionescolar.Firebase.FirestoreClass
import com.example.atencionescolar.R
import com.example.atencionescolar.activities.Inicio
import com.example.atencionescolar.activities.PrincipalAdmin
import com.example.atencionescolar.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.content.Context
import com.example.atencionescolar.activities.PrincipalUsuario

class MyFirebaseMessagingService : FirebaseMessagingService() {

    //Metodo para controlar la llegada de notificaciones
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Manejo de mensajes con carga de datos
        remoteMessage.data.isNotEmpty().let {
            Log.i(TAG, "Message data payload: ${remoteMessage.data}")

            val title = remoteMessage.data[Constants.FCM_KEY_TITLE] ?: "Notificación"
            val message = remoteMessage.data[Constants.FCM_KEY_MESSAGE] ?: "Tienes una nueva notificación"

            sendNotification(title, message)
        }

        // Manejo de mensajes con carga de notificación
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.title ?: "Notificación", it.body ?: "Tienes un nuevo mensaje")
        }
    }

    //Refresca el token de FCM en caso de que se requiera
    override fun onNewToken(token: String) {
        Log.e(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    //Metodo para registrar EL TOKEN FCM de los usuarios
    private fun sendRegistrationToServer(token: String?) {
        val sharedPreferences = getSharedPreferences(Constants.PROGEMANAG_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(Constants.FCM_TOKEN, token)
        editor.apply()
    }

    //Metodo para neviar una notificacion
    private fun sendNotification(title: String, message: String) {
        // Obtén el tipo de usuario desde SharedPreferences o Firestore
        val sharedPreferences = getSharedPreferences(Constants.PROGEMANAG_PREFERENCES, Context.MODE_PRIVATE)
        val userType = sharedPreferences.getString(Constants.USER_TYPE, "")

        // Redirige según el tipo de usuario
        val intent: Intent = when (userType) {
            "admin" -> Intent(this, PrincipalAdmin::class.java)
            "user" -> Intent(this, PrincipalUsuario::class.java)
            else -> Intent(this, Inicio::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel Projemanag title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }


    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
