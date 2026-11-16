package com.khanscreations.ruhani.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.khanscreations.ruhani.data.model.GeoMath
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class CompassReading(
    val azimuthDegrees: Float?,
    val hasSensor: Boolean
)

class CompassRepository(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    fun observeDeviceAzimuth(): Flow<CompassReading> = callbackFlow {
        val manager = sensorManager
        if (manager == null) {
            trySend(CompassReading(azimuthDegrees = null, hasSensor = false))
            close()
            return@callbackFlow
        }

        val rotationSensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetSensor = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val useRotationVector = rotationSensor != null
        val useAccelMagnet = !useRotationVector && accelSensor != null && magnetSensor != null

        if (!useRotationVector && !useAccelMagnet) {
            trySend(CompassReading(azimuthDegrees = null, hasSensor = false))
            close()
            return@callbackFlow
        }

        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        val lastAccel = FloatArray(3)
        val lastMagnet = FloatArray(3)
        var hasAccel = false
        var hasMagnet = false
        var smoothedAzimuth: Float? = null

        fun emitSmoothed(rawDegrees: Float) {
            val normalized = GeoMath.normalizeDegrees(rawDegrees)
            val current = smoothedAzimuth
            val next = if (current == null) {
                normalized
            } else {
                val delta = GeoMath.shortestSignedAngle(current, normalized)
                GeoMath.normalizeDegrees(current + delta * 0.22f)
            }
            smoothedAzimuth = next
            trySend(CompassReading(azimuthDegrees = next, hasSensor = true))
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientationAngles)
                        val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                        emitSmoothed(azimuth)
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, lastAccel, 0, 3)
                        hasAccel = true
                        if (hasMagnet &&
                            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccel, lastMagnet)
                        ) {
                            SensorManager.getOrientation(rotationMatrix, orientationAngles)
                            val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                            emitSmoothed(azimuth)
                        }
                    }

                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, lastMagnet, 0, 3)
                        hasMagnet = true
                        if (hasAccel &&
                            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccel, lastMagnet)
                        ) {
                            SensorManager.getOrientation(rotationMatrix, orientationAngles)
                            val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                            emitSmoothed(azimuth)
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        if (useRotationVector) {
            manager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            manager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            manager.registerListener(listener, magnetSensor, SensorManager.SENSOR_DELAY_UI)
        }

        awaitClose {
            manager.unregisterListener(listener)
        }
    }
}
