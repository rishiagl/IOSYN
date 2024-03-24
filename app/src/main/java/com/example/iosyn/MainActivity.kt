package com.example.iosyn

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import com.example.iosyn.service.SensorService
import com.example.iosyn.ui.theme.IOSYNTheme


class MainActivity : ComponentActivity() {
    lateinit var sensorServiceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorServiceIntent = Intent(this, SensorService::class.java)
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
        if (::sensorServiceIntent.isInitialized) {
            stopService(sensorServiceIntent)
        }
        super.onDestroy()
    }

    private fun startService(type: ServiceType){
        if(type == ServiceType.SENSOR) startService(sensorServiceIntent)
    }

    private fun stopService(type: ServiceType){
        if(type == ServiceType.SENSOR) stopService(sensorServiceIntent)
    }
}


@Composable
fun MainPage(startService: (ServiceType) -> Unit, stopService: (ServiceType) -> Unit) {
    var checked by remember { mutableStateOf(false) }
    Column{
        Header()
        Column {
            Text(text = "Services")
            Row () {
                Text(text = "Sensor Service")
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        if(checked) startService(ServiceType.SENSOR)
                        else stopService(ServiceType.SENSOR)
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
    }
}

@Composable
fun Header() {
    Text(text = "IOSYN")
}

@Composable
fun Sensor() {
    var checked by remember { mutableStateOf(true) }
    Row (){
        Text(text = "Sensor Service")
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
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

enum class ServiceType {
    SENSOR, LOCATION, WIFI
}