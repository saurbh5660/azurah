package com.live.azurah.firebase

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.live.azurah.R
import com.live.azurah.activity.ChatActivity
import com.live.azurah.activity.NotificationActivity
import com.live.azurah.retrofit.ApiConstants
import org.json.JSONObject
import java.util.Date

class MyMessagingService : FirebaseMessagingService() {

        override fun onMessageReceived(remoteMessage: RemoteMessage) {
                super.onMessageReceived(remoteMessage)

                Log.d("FCM_DEBUG", remoteMessage.data.toString())


                val title = (remoteMessage.data["title"] ?: "").uppercase()
                val body = remoteMessage.data["message"] ?: ""
                val type = remoteMessage.data["type"] ?: ""

                val intent: Intent
                if (type == "3") {
                        if (!ApiConstants.isNotification){
                                val senderId = remoteMessage.data["sender_id"] ?: ""
                                val senderName = remoteMessage.data["sender_name"] ?: ""
                                val senderImage = remoteMessage.data["sender_image"] ?: ""
                                val constantId = remoteMessage.data["chat_constant_id"] ?: ""
                                val username = remoteMessage.data["sender_username"] ?: ""

                                intent = Intent(this, ChatActivity::class.java).apply {
                                        putExtra("uid2", senderId)
                                        putExtra("name", senderName)
                                        putExtra("image", senderImage)
                                        putExtra("username", username)
                                        putExtra("constant_id", constantId)
                                        putExtra("isNotification", "1")
                                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                createNotification(title, body, intent)
                        }
                } else {
                        intent = Intent(this, NotificationActivity::class.java).apply {
                                putExtra("isNotification", "1")
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        createNotification(title, body, intent)

                }
        }

        private fun createNotification(title: String, message: String, intent: Intent) {
                val notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val channelId = applicationContext.packageName

                // Notification Channel for Android O+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                                channelId,
                                "General Notifications",
                                NotificationManager.IMPORTANCE_HIGH
                        ).apply {
                                description = "General notification channel"
                                enableLights(true)
                                lightColor = Color.RED
                                enableVibration(true)
                                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                        }
                        notificationManager.createNotificationChannel(channel)
                }

                val uniqueId = Date().time.toInt()
                val pendingIntent = PendingIntent.getActivity(
                        this,
                        uniqueId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )

                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                        .setAutoCancel(true)
                        .setSound(sound)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)

                notificationManager.notify(uniqueId, notificationBuilder.build())
        }

        override fun onNewToken(token: String) {
                super.onNewToken(token)
                Log.d("FCM_TOKEN", "New FCM Token: $token")
        }
}
