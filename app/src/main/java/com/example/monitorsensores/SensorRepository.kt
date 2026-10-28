package com.example.monitorsensores

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.random.Random

class SensorRepository {

    fun temperaturaFlow(): Flow<Double> = flow {
        while (true) {
            if (Random.nextInt(100) < 5) {
                throw IllegalStateException("Falla crítica en el sensor de temperatura")
            }
            val lectura = 20.0 + Random.nextDouble() * 20.0
            emit(lectura)
            delay(1000)
        }
    }.flowOn(Dispatchers.Default)

    fun humedadFlow(): Flow<Double> = flow {
        while (true) {
            val humedad = 40.0 + Random.nextDouble() * 50.0
            emit(humedad)
            delay(1000)
        }
    }.flowOn(Dispatchers.Default)

    fun cuentaRegresiva(desde: Int): Flow<Int> = flow {
        val rango = desde downTo 0
        for (i in rango) {
            emit(i)
            delay(1000)
        }
    }
}