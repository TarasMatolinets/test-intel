package com.android.aura.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.aura.MainActivity
import com.android.aura.R
import com.android.aura.models.BootLoadData
import com.android.aura.viewmodel.SHARED_PREF_NAME
import com.google.gson.Gson

private const val CHANNEL_ID = "notificaiton--channal-id"
class BootReceiver  : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if(action != null && action == Intent.ACTION_BOOT_COMPLETED) {
            createNotificationChannel(context)
           val sharedData = getDataShared(context)
            createNotificationEvent(context, getTitle(context, sharedData))
        }
    }

    private fun createNotificationEvent(context: Context, title: CharSequence) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(R.integer.not_id, builder.build())
        }
    }

    private fun getTitle(context: Context,sharedData: BootLoadData): CharSequence {
        return when (sharedData.count)
        {
            0 -> context.getString(R.string.no_value)
            1 -> context.getString(R.string.single_boot)
            else -> context.getString(R.string.multiple_boots)
        }
    }

    private fun getDataShared(context: Context): BootLoadData {
        val sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val result = sharedPref.getString(context.getString(R.string.pref_key), context.getString(R.string.no_boots_detected))
        val gson = Gson()
        val timeLoad = System.currentTimeMillis() / 1000L
       return if(result == context.getString(R.string.no_boots_detected)) {
            val bootLoadData =  BootLoadData(1,timeLoad)
            gson.toJson(bootLoadData)

            bootLoadData
        }else {
            val bootLoadData =  gson.fromJson(result, BootLoadData::class.java)
            bootLoadData.count += 1
            bootLoadData.time = timeLoad - bootLoadData.time

            gson.toJson(bootLoadData)

           bootLoadData
        }
    }

    private fun createNotificationChannel(context: Context) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}