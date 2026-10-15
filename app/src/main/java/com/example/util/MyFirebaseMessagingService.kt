package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.aistudio.neolbharat.nbapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .update("fcmToken", token)
            } catch (e: Exception) {
                // Ignore failure
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Neol Bharat Institute"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "You have a new update in Neol Bharat."

        showNotification(applicationContext, title, body)
    }

    companion object {
        const val CHANNEL_ID = "neol_bharat_channel"
        const val CHANNEL_NAME = "Neol Bharat Notifications"

        fun showNotification(
            context: Context,
            title: String,
            body: String,
            notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
        ) {
            createNotificationChannel(context)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            try {
                NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // Permission not granted
            }
        }

        private fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for student fees, course unlocks, attendance, and educational content"
                    enableVibration(true)
                }
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                manager?.createNotificationChannel(channel)
            }
        }
    }
}
