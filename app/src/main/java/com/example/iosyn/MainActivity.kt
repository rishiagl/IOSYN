package com.example.iosyn

import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iosyn.service.AccelerometerService
import com.example.iosyn.service.GyroscopeService
import com.example.iosyn.service.LightService
import com.example.iosyn.service.MagneticFieldService
import com.example.iosyn.service.ProximityService
import com.example.iosyn.ui.theme.IOSYNTheme
import com.example.iosyn.utils.ServiceType
import com.rabbitmq.client.ConnectionFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random


class MainActivity : ComponentActivity() {
    lateinit var AccelerometerIntent: Intent
    lateinit var GyroscopeIntent: Intent
    lateinit var MagneticFieldIntent: Intent
    lateinit var LightIntent: Intent
    lateinit var ProximityIntent: Intent
    private lateinit var sm: SensorManager
    private val exchangeName = Random(System.currentTimeMillis()).nextInt(99999999).toString()
    private var context_label = "unlabelled"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sm = getSystemService(SENSOR_SERVICE) as SensorManager
        AccelerometerIntent = Intent(this, AccelerometerService::class.java).putExtra("EXCHANGE_NAME", exchangeName)
        GyroscopeIntent = Intent(this, GyroscopeService::class.java).putExtra("EXCHANGE_NAME", exchangeName)
        MagneticFieldIntent = Intent(this, MagneticFieldService::class.java).putExtra("EXCHANGE_NAME", exchangeName)
        LightIntent = Intent(this, LightService::class.java).putExtra("EXCHANGE_NAME", exchangeName)
        ProximityIntent = Intent(this, ProximityService::class.java).putExtra("EXCHANGE_NAME", exchangeName)
        setContent {
            IOSYNTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainPage(exchangeName, ::startService, ::stopService, ::switchContext)
                }
            }
        }
    }

    override fun onDestroy() {
        if (::AccelerometerIntent.isInitialized) {
            stopService(AccelerometerIntent)
        }
        super.onDestroy()
    }

    private fun switchContext(context: String){
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val factory = ConnectionFactory()
                factory.host = "52.66.250.239"
                factory.username = "rishiagl"
                factory.password = "1234"
                factory.virtualHost = "vh1"
                val channel = factory.newConnection().createChannel()
                channel.exchangeDeclare(exchangeName, "topic")
                channel.basicPublish(
                    exchangeName,
                    "CONTEXT",
                    null,
                    context.toByteArray()
                )
                channel.connection.close()
            } catch (e: Exception) {
                Log.e("Rabbitmq Connection Exception", e.toString())
            }
        }
    }

    private fun startService(type: ServiceType){
        if(type == ServiceType.ACCELEROMETER) startService(AccelerometerIntent)
        if(type == ServiceType.GYROSCOPE) startService(GyroscopeIntent)
        if(type == ServiceType.MAGNETIC_FIELD) startService(MagneticFieldIntent)
        if(type == ServiceType.LIGHT) startService(LightIntent)
        if(type == ServiceType.PROXIMITY) startService(ProximityIntent)
    }

    private fun stopService(type: ServiceType){
        if(type == ServiceType.ACCELEROMETER) stopService(AccelerometerIntent)
        if(type == ServiceType.GYROSCOPE) stopService(GyroscopeIntent)
        if(type == ServiceType.MAGNETIC_FIELD) stopService(MagneticFieldIntent)
        if(type == ServiceType.LIGHT) stopService(LightIntent)
        if(type == ServiceType.PROXIMITY) stopService(ProximityIntent)
    }
}


@Composable
fun MainPage(exchangeName: String, startService: (ServiceType) -> Unit, stopService: (ServiceType) -> Unit, switchContext:(String) -> Unit) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(2.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally){
        Header()
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,) {
            Text(text = exchangeName.toString(), fontWeight = FontWeight.Bold, fontSize = 25.sp, modifier = Modifier.padding(bottom = 10.dp))
            Text(text = "Services", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 10.dp))
            ServiceRow(serviceType = ServiceType.ACCELEROMETER, startService = startService, stopService = stopService)
            ServiceRow(serviceType = ServiceType.GYROSCOPE, startService = startService, stopService = stopService)
            ServiceRow(serviceType = ServiceType.LIGHT, startService = startService, stopService = stopService)
            ServiceRow(serviceType = ServiceType.MAGNETIC_FIELD, startService = startService, stopService = stopService)
            ServiceRow(serviceType = ServiceType.PROXIMITY, startService = startService, stopService = stopService)
            Column (modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .padding(start = 15.dp, end = 15.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    Text(text = "Context Switch", fontWeight = FontWeight.Bold)
                }
                Column {
                    Row {
                        Button(onClick = { switchContext("INDOOR")}, modifier = Modifier.padding(2.dp)) {
                            Text(text = "Indoor")
                        }
                        Button(onClick = { switchContext("OUTDOOR")}, modifier = Modifier.padding(2.dp)) {
                            Text(text = "OUTDOOR")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Header() {
    Text(text = "IOSYN", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))
}

@Composable
fun ServiceRow(serviceType: ServiceType, startService: (ServiceType) -> Unit, stopService: (ServiceType) -> Unit) {
    var checked by remember { mutableStateOf(false) }
    Row (modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp)
        .padding(start = 15.dp, end = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = serviceType.name)
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                if(checked) startService(serviceType)
                else stopService(serviceType)
            },
            thumbContent = if (checked) {
                {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            } else {
                null
            }
        )
    }
}