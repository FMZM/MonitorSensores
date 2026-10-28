package com.example.monitorsensores

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    private val vm: SensorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Pantalla(vm)
                }
            }
        }
    }
}

@Composable
fun Pantalla(vm: SensorViewModel) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val contador by vm.contador.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.alertas.collect { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Temperatura actual: ${estado.actual} °C",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Humedad simulada: ${estado.humedadActual} %",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Promedio (últimas 10): ${"%.1f".format(estado.promedio)} °C"
            )

            Text(
                text = "Lecturas (>25°C): ${estado.lecturas.joinToString(", ")}"
            )

            Text(text = "Umbral de Alerta: ${estado.umbralAlerta.toInt()} °C")
            Slider(
                value = estado.umbralAlerta,
                onValueChange = { vm.actualizarUmbral(it) },
                valueRange = 25f..40f,
                steps = 15
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { vm.iniciar() },
                    enabled = !estado.monitoreando
                ) {
                    Text("Iniciar")
                }

                Button(
                    onClick = { vm.detener() },
                    enabled = estado.monitoreando
                ) {
                    Text("Detener")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Button(onClick = { vm.iniciarContador(10) }) {
                Text("Cuenta regresiva 10s")
            }

            contador?.let {
                Text(
                    text = "Faltan: $it s",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}