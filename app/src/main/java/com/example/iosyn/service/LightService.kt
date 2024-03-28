package com.example.iosyn.service

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.iosyn.utils.ServiceType
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

class LightService: Service() {
    private lateinit var sm: SensorManager
    lateinit var values: FloatArray
    var channel: Channel? = null
    private lateinit var sensor: Sensor
    lateinit var routingKey: String
    val EXCHANGE_NAME = "CLIENT_SAMSUNG_S10"

    @OptIn(DelicateCoroutinesApi::class)
    var sel: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        override fun onSensorChanged(event: android.hardware.SensorEvent?) {
            values = event!!.values
            GlobalScope.launch {
                try {
                    var i = 0
                    while (i < values.size) {
                        channel?.basicPublish(
                            EXCHANGE_NAME,
                            "$routingKey.$i",
                            null,
                            values[i].toString().toByteArray(StandardCharsets.UTF_8)
                        )
                        i++
                    }
                } catch (e: Exception) {
                    Log.e("Message Send Exception", e.toString())
                }
            }

        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) createNotificationChanel() else startForeground(
//            1,
//            Notification()
//        )
        sm = getSystemService(SENSOR_SERVICE) as SensorManager
        if (sm.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            sensor  = sm.getDefaultSensor(Sensor.TYPE_LIGHT)!!
        }
        else{
            Toast.makeText(this, "LIGHT Not Present", Toast.LENGTH_SHORT).show()
        }
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val factory = ConnectionFactory()
                factory.host = "52.66.250.239"
                factory.username = "rishiagl"
                factory.password = "1234"
                factory.virtualHost = "vh1"
                channel = factory.newConnection().createChannel()
                channel?.exchangeDeclare("mobile", "topic")
                routingKey = ServiceType.LIGHT.name
            } catch (e: Exception) {
                Log.e("Rabbitmq Connection Exception", e.toString())
            }
        }
        Log.e("routing key", sensor.name)
        sm.registerListener(
            sel,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
        return START_STICKY
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onDestroy() {

        GlobalScope.launch {
            sm.unregisterListener(sel)
            channel?.connection?.close()
        }
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun createNotificationChanel() {
//        val NOTIFICATION_CHANNEL_ID = "com.getlocationbackground"
//        val channelName = "Background Service"
//        val chan = NotificationChannel(
//            NOTIFICATION_CHANNEL_ID,
//            channelName,
//            NotificationManager.IMPORTANCE_NONE
//        )
//        chan.lightColor = Color.BLUE
//        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
//        val manager =
//            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
//        manager.createNotificationChannel(chan)
//        val notificationBuilder =
//            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//        val notification: Notification = notificationBuilder.setOngoing(true)
//            .setContentTitle("App is running count::")
//            .setPriority(NotificationManager.IMPORTANCE_MIN)
//            .setCategory(Notification.CATEGORY_SERVICE)
//            .build()
//        startForeground(2, notification)
//    }
}