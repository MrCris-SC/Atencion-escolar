package com.example.atencionescolar.activities

import android.os.AsyncTask
import android.util.Log
import com.example.atencionescolar.utils.Constants
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class SendNotificationToAdminAsyncTask(private val adminToken: String) :
    AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg params: String): String {
        var result = ""
        var connection: HttpURLConnection? = null

        try {
            val url = URL(Constants.FCM_BASE_URL)
            connection = url.openConnection() as HttpURLConnection

            connection.doOutput = true
            connection.doInput = true
            connection.instanceFollowRedirects = false
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("charset", "utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty(
                Constants.FCM_AUTHORIZATION,
                "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
            )
            connection.useCaches = false

            val wr = DataOutputStream(connection.outputStream)

            val jsonRequest = JSONObject()
            val dataObject = JSONObject()
            dataObject.put(Constants.FCM_KEY_TITLE, params[0]) // Título de la notificación
            dataObject.put(Constants.FCM_KEY_MESSAGE, params[1]) // Mensaje de la notificación
            jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
            jsonRequest.put(Constants.FCM_KEY_TO, adminToken)

            wr.writeBytes(jsonRequest.toString())
            wr.flush()

            val httpResult: Int = connection.responseCode

            if (httpResult == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val sb = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    sb.append(line + "\n")
                }

                result = sb.toString()
            } else {
                result = connection.responseMessage
            }
        } catch (e: SocketTimeoutException) {
            result = "Connection Timeout"
        } catch (e: Exception) {
            result = "Error : " + e.message
        } finally {
            connection?.disconnect()
        }

        return result
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        Log.d("NotificationResult", result)
        // Manejar el resultado según sea necesario
    }
}
