package com.example.iosyn.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.iosyn.utils.SensorListenerClass


class SensorService : Service() {

    private lateinit var sm: SensorManager
    private var sensorObjectList: ArrayList<SensorListenerClass> = ArrayList<SensorListenerClass>()

    override fun onCreate() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) createNotificationChanel() else startForeground(
            1,
            Notification()
        )
        registerSensors()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel() {
        val NOTIFICATION_CHANNEL_ID = "com.getlocationbackground"
        val channelName = "Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager =
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running count::")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }

    private fun registerSensors() {
        sm = getSystemService(SENSOR_SERVICE) as SensorManager
        if (sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            sensorObjectList.add(SensorListenerClass(sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!))
        }
        if (sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            sensorObjectList.add(SensorListenerClass(sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!))
        }
        if (sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            sensorObjectList.add(SensorListenerClass(sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!))
        }
        if (sm.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            sensorObjectList.add(SensorListenerClass(sm.getDefaultSensor(Sensor.TYPE_LIGHT)!!))
        }
        if (sm.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
            sensorObjectList.add(SensorListenerClass(sm.getDefaultSensor(Sensor.TYPE_PROXIMITY)!!))
        }
        for (i in sensorObjectList.indices) {
            sm.registerListener(
                sensorObjectList[i].sel,
                sensorObjectList[i].sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        unRegisterSensors()
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }

    private fun unRegisterSensors(){
        for (i in sensorObjectList.indices) {
            sm.unregisterListener(sensorObjectList[i].sel)
            sensorObjectList[i].channel.connection.close()
        }
        Toast.makeText(
            this,
            "Sensors Unregistered",
            Toast.LENGTH_LONG
        ).show()
    }
}
