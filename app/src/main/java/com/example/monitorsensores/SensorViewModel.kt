package com.example.monitorsensores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SensorUiState(
    val actual: Double = 0.0,
    val promedio: Double = 0.0,
    val humedadActual: Double = 0.0,
    val lecturas: List<Double> = emptyList(),
    val monitoreando: Boolean = false,
    val umbralAlerta: Float = 35.0f
)

class SensorViewModel(
    private val repo: SensorRepository = SensorRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SensorUiState())
    val uiState: StateFlow<SensorUiState> = _uiState.asStateFlow()

    private val _alertas = MutableSharedFlow<String>()
    val alertas: SharedFlow<String> = _alertas.asSharedFlow()

    private val _contador = MutableStateFlow<Int?>(null)
    val contador: StateFlow<Int?> = _contador.asStateFlow()

    private var jobSensor: Job? = null
    private var jobContador: Job? = null

    fun actualizarUmbral(nuevoUmbral: Float) {
        _uiState.update { it.copy(umbralAlerta = nuevoUmbral) }
    }

    fun iniciar() {
        if (jobSensor?.isActive == true) return
        _uiState.update { it.copy(monitoreando = true) }

        jobSensor = viewModelScope.launch {
            repo.temperaturaFlow()
                .map { "%.1f".format(it).replace(',', '.').toDouble() }
                .filter { it > 25.0 }
                .distinctUntilChanged()
                .retry(3)
                .catch { e ->
                    _alertas.emit("Error en sensor tras reintentos: ${e.message}")
                }
                .combine(repo.humedadFlow()) { temp, humedad ->
                    Pair(temp, humedad)
                }
                .onEach { (temp, _) ->
                    val umbralActual = _uiState.value.umbralAlerta.toDouble()
                    if (temp > umbralActual) {
                        _alertas.emit("¡Alerta! Temperatura alta: $temp °C (Umbral: $umbralActual)")
                    }
                }
                .collect { (temp, humedad) ->
                    _uiState.update { estado ->
                        val nuevas = (estado.lecturas + temp).takeLast(10)
                        estado.copy(
                            actual = temp,
                            humedadActual = "%.1f".format(humedad).replace(',', '.').toDouble(),
                            lecturas = nuevas,
                            promedio = if (nuevas.isNotEmpty()) nuevas.average() else 0.0
                        )
                    }
                }
        }
    }

    fun detener() {
        jobSensor?.cancel()
        _uiState.update { it.copy(monitoreando = false) }
    }

    fun iniciarContador(segundos: Int = 10) {
        jobContador?.cancel()
        jobContador = viewModelScope.launch {
            repo.cuentaRegresiva(segundos)
                .onCompletion { _contador.value = null }
                .collect { _contador.value = it }
        }
    }
}