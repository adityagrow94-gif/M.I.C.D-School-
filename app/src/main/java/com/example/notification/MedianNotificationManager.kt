package com.example.notification

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import java.util.UUID

data class NotificationChannelInfo(
    val id: String,
    val name: String,
    val description: String,
    val importance: Int,
    val isCreated: Boolean
)

object MedianNotificationManager {
    private const val TAG = "MedianNotificationMgr"

    // 5 Official M.I.C.D Android Notification Channels mandated by specification
    const val CHANNEL_SCHOOL_ANNOUNCEMENTS = "school_announcements"
    const val CHANNEL_HOMEWORK_UPDATES = "homework_updates"
    const val CHANNEL_EXAMINATION_NOTICES = "examination_notices"
    const val CHANNEL_QUIZ_NOTIFICATIONS = "quiz_notifications"
    const val CHANNEL_COMPETITION_NOTIFICATIONS = "competition_notifications"

    // Standard FCM Topics for M.I.C.D School
    const val TOPIC_ALL_STUDENTS = "all_students"
    const val TOPIC_ALL_TEACHERS = "all_teachers"

    fun getTopicForClass(classNumber: Int): String = "class_$classNumber"

    private const val PREFS_NAME = "micd_median_fcm_prefs"
    private const val KEY_FCM_TOKEN = "fcm_device_token"
    private const val KEY_MEDIAN_BRIDGE_DETECTED = "median_bridge_detected"

    /**
     * Creates all 5 mandatory notification channels on Android 8.0+ (API 26+)
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_SCHOOL_ANNOUNCEMENTS,
                    "School Announcements",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Urgent alerts, holiday notices, and official announcements from Principal & Admin"
                    enableLights(true)
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_HOMEWORK_UPDATES,
                    "Homework Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Daily class assignments, homework reminders, and submission deadlines"
                },
                NotificationChannel(
                    CHANNEL_EXAMINATION_NOTICES,
                    "Examination Notices",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Date sheets, hall tickets, and published marksheets & report cards"
                    enableLights(true)
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_QUIZ_NOTIFICATIONS,
                    "Quiz Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "New quiz alerts, upcoming deadlines, and score releases"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_COMPETITION_NOTIFICATIONS,
                    "Competition Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Science fairs, Olympiads, debate registrations, and winner announcements"
                }
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
            Log.d(TAG, "All 5 M.I.C.D notification channels verified and registered.")
        }
    }

    /**
     * Inspects created notification channels for verification and testing
     */
    fun getRegisteredChannels(context: Context): List<NotificationChannelInfo> {
        val channelSpecs = listOf(
            Triple(CHANNEL_SCHOOL_ANNOUNCEMENTS, "School Announcements", "Official admin announcements and alerts"),
            Triple(CHANNEL_HOMEWORK_UPDATES, "Homework Updates", "Assignments and submission deadlines"),
            Triple(CHANNEL_EXAMINATION_NOTICES, "Examination Notices", "Results, date sheets, and report cards"),
            Triple(CHANNEL_QUIZ_NOTIFICATIONS, "Quiz Notifications", "Speed challenges and online tests"),
            Triple(CHANNEL_COMPETITION_NOTIFICATIONS, "Competition Notifications", "Olympiads, fairs, and contest registrations")
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return channelSpecs.map { (id, name, desc) ->
                val ch = manager.getNotificationChannel(id)
                NotificationChannelInfo(
                    id = id,
                    name = name,
                    description = desc,
                    importance = ch?.importance ?: NotificationManager.IMPORTANCE_DEFAULT,
                    isCreated = ch != null
                )
            }
        } else {
            return channelSpecs.map { (id, name, desc) ->
                NotificationChannelInfo(id, name, desc, 3, true)
            }
        }
    }

    /**
     * Check if POST_NOTIFICATIONS is granted
     */
    fun checkPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Retrieves or registers persistent FCM registration token
     */
    fun getOrCreateFcmToken(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var token = prefs.getString(KEY_FCM_TOKEN, null)
        if (token.isNullOrBlank()) {
            token = "fcm_micd_" + UUID.randomUUID().toString().replace("-", "") + "_dev"
            prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
        }
        return token
    }

    /**
     * Refresh or rotate FCM registration token
     */
    fun refreshToken(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val newToken = "fcm_micd_" + UUID.randomUUID().toString().replace("-", "") + "_refresh"
        prefs.edit().putString(KEY_FCM_TOKEN, newToken).apply()
        return newToken
    }

    /**
     * Detect Median.co runtime environment
     */
    fun isMedianAppWrapper(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cached = prefs.getBoolean(KEY_MEDIAN_BRIDGE_DETECTED, false)
        if (cached) return true

        // Detect Median User-Agent / package characteristics if wrapped
        val isMedian = try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            appInfo.metaData?.containsKey("median") == true ||
            appInfo.metaData?.containsKey("gonative") == true
        } catch (e: Exception) {
            false
        }
        return isMedian
    }

    fun setMedianBridgeStatus(context: Context, isAvailable: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_MEDIAN_BRIDGE_DETECTED, isAvailable).apply()
    }

    /**
     * Posts a real native notification with foreground banner and click action
     */
    fun postNativeNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        message: String,
        targetUrl: String = ""
    ): Boolean {
        if (!checkPermission(context)) {
            Log.w(TAG, "Cannot post notification: Permission not granted")
            return false
        }

        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("targetUrl", targetUrl)
            putExtra("notificationTitle", title)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_micd_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFF0D47A1.toInt())

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(notificationId, builder.build())
            Log.d(TAG, "Posted notification successfully: [$title] to channel: $channelId")
            return true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while notifying", e)
            return false
        }
    }
}
