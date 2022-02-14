package com.azzam.smartalarm

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val type = intent?.getIntExtra(EXTRA_TYPE, -1)
        val message = intent?.getStringExtra(EXTRA_MESSAGE)
        val title = if (type == TYPE_ONE_TIME) "One Time Alarm" else "Repeating"

        val notificationId = if (type == TYPE_ONE_TIME) ID_ONE_TIME else ID_REPEATING

        if (message != null) showAlarmNotification(context, title, message, notificationId)
    }

    fun setOneTimeAlarm(context: Context, type: Int, date: String, time: String, message: String){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(EXTRA_MESSAGE,message)
        intent.putExtra(EXTRA_TYPE, type)

        // date diterima -> 2-2-2022
        // menghilangkan tanda "-"
        Log.e("ErrorSetOneTimeAlarm", "setOneTimeAlarm: $date $time")

        val dateArray = date.split("-").toTypedArray()
        val convertedDateArray = convertArraytoArrayInt(dateArray)

        val timeArray = time.split(":").toTypedArray()
        val convertedTimeArray = convertArraytoArrayInt(timeArray)

        val calendar = Calendar.getInstance()
        // date
        calendar.set(Calendar.DAY_OF_MONTH, convertedDateArray[0]) // buat hari
        calendar.set(Calendar.MONTH, convertedDateArray[1] - 1) // buat bulan
        calendar.set(Calendar.YEAR, convertedDateArray[2]) // buat tahun
        // time
        calendar.set(Calendar.HOUR, convertedTimeArray[0])
        calendar.set(Calendar.MINUTE, convertedTimeArray[1])
        calendar.set(Calendar.SECOND, 0)

        val pendingIntent = PendingIntent.getBroadcast(context, 101, intent, 0)
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        Toast.makeText(context, "Success set one time alarm", Toast.LENGTH_SHORT).show()
        Log.i("SetAlarmNotifications", "setOneTimeAlarm: Alarm will rings on ${calendar.time}")
    }

    fun setRepeatingAlarm(context: Context, type: Int, time: String, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(EXTRA_MESSAGE, message)
        intent.putExtra(EXTRA_TYPE, type)

        val timeArray = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray[0]))
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArray[1]))
        calendar.set(Calendar.SECOND, 0)

        val pendingIntent = PendingIntent.getBroadcast(context, ID_REPEATING, intent, 0)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        Toast.makeText(context, "Success set up Repeating Alarm", Toast.LENGTH_SHORT).show()
    }

    private fun showAlarmNotification(
        context: Context?,
        title: String,
        message: String,
        notificationId: Int
    ) {
        val channelId = "Channel_1"
        val channelName = "AlarmManager channel"

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_one_time)
            .setContentTitle(title)
            .setContentText(message)
            .setSound(alarmSound)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            builder.setChannelId(channelId)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = builder.build()
        notificationManager.notify(notificationId, notification)
    }

    fun cancelAlarm(context: Context, type: Int) {
        // alarm manager
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // intent ke alarm receiver
        val intent = Intent(context, AlarmReceiver::class.java)
        // ambil request code
        val requestCode = if (type == TYPE_ONE_TIME) ID_ONE_TIME else ID_REPEATING
        // cancel pending intent
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0)
        pendingIntent.cancel()
        // cancel alarm manager
        alarmManager.cancel(pendingIntent)
        if (type == TYPE_ONE_TIME) {
            Toast.makeText(context, "Successfully cancel ONE TIME ALARM", Toast.LENGTH_SHORT)
                .show()
        }else {
            Toast.makeText(context, "Successfully cancel REPEATING ALARM", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun convertArraytoArrayInt(data: Array<String>) : List<Int> {
        return data.map {
            it.toInt()
        }
    }

    companion object{
        const val EXTRA_TYPE = "type"
        const val EXTRA_MESSAGE = "message"

        const val TYPE_ONE_TIME = 0
        const val TYPE_REPEATING = 1

        const val ID_ONE_TIME = 101
        const val ID_REPEATING = 102
    }

}