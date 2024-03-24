package com.example.iosyn.utils

import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.util.Log
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

@OptIn(DelicateCoroutinesApi::class)
public class SensorListenerClass(var sensor: Sensor) {
    lateinit var values: FloatArray
    lateinit var channel: Channel
    var sel: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        override fun onSensorChanged(event: android.hardware.SensorEvent?) {
            values = event!!.values
            GlobalScope.launch {
                try {
                    var i = 0
                    while (i < values.size) {
                        channel.basicPublish(
                            "",
                            "hello",
                            null,
                            values[i].toString().toByteArray(StandardCharsets.UTF_8)
                        )
                        i++
                    }
                } catch (e: Exception) {
                    Log.e("Message Send Exception", e.toString());
                }
            }

        }
    }

    init {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                var factory = ConnectionFactory()
                factory.host = "52.66.250.239"
                factory.username = "rishiagl"
                factory.password = "1234"
                factory.virtualHost = "vh1"
                channel = factory.newConnection().createChannel()
                channel.exchangeDeclare(sensor.name, "topic");
                channel.queueDeclare("hello", false, false, false, null)
            } catch (e: Exception) {
                Log.e("Rabbitmq Connection Exception", e.toString());
            }
        }
    }
}