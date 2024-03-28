package com.example.iosyn

import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
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


class MainActivity : ComponentActivity() {
    lateinit var AccelerometerIntent: Intent
    lateinit var GyroscopeIntent: Intent
    lateinit var MagneticFieldIntent: Intent
    lateinit var LightIntent: Intent
    lateinit var ProximityIntent: Intent
    private lateinit var sm: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sm = getSystemService(SENSOR_SERVICE) as SensorManager
        AccelerometerIntent = Intent(this, AccelerometerService::class.java)
        GyroscopeIntent = Intent(this, GyroscopeService::class.java)
        MagneticFieldIntent = Intent(this, MagneticFieldService::class.java)
        LightIntent = Intent(this, LightService::class.java)
        ProximityIntent = Intent(this, ProximityService::class.java)
        setContent {
            IOSYNTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainPage(::startService, ::stopService)
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
fun MainPage(startService: (ServiceType) -> Unit, stopService: (ServiceType) -> Unit) {
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
            Text(text = "Services", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 10.dp))
            ServiceRow(serviceType = ServiceType.ACCELEROMETER, startService = startService, stopService = stopService)
            ServiceRow(serviceType = ServiceType.GYROSCOPE, startService = startService, stopService = stopService)
            ServiceRow(serviceType = ServiceType.LIGHT, startService = startService, stopService = stopService)
            ServiceRow(serviceType = ServiceType.MAGNETIC_FIELD, startService = startService, stopService = stopService)
            ServiceRow(serviceType = ServiceType.PROXIMITY, startService = startService, stopService = stopService)
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