// CameraPositionSharedPreferences.kt
package campus.tech.kakao.map

import android.content.Context

object CameraPositionSharedPreferences {
    private const val PREFS_NAME = "camera_position_prefs"
    private const val KEY_LATITUDE = "latitude"
    private const val KEY_LONGITUDE = "longitude"
    private const val KEY_ZOOM = "zoom"

    fun saveCameraPosition(context: Context, latitude: Double, longitude: Double, zoom: Int?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putFloat(KEY_LATITUDE, latitude.toFloat())
            putFloat(KEY_LONGITUDE, longitude.toFloat())
            if (zoom != null) {
                putFloat(KEY_ZOOM, zoom.toFloat())
            }
            apply()
        }
    }

    fun loadCameraPosition(context: Context): Triple<Double, Double, Double>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val latitude = prefs.getFloat(KEY_LATITUDE, Float.MIN_VALUE)
        val longitude = prefs.getFloat(KEY_LONGITUDE, Float.MIN_VALUE)
        val zoom = prefs.getFloat(KEY_ZOOM, Float.MIN_VALUE)

        return if (latitude != Float.MIN_VALUE && longitude != Float.MIN_VALUE && zoom != Float.MIN_VALUE) {
            Triple(latitude.toDouble(), longitude.toDouble(), zoom.toDouble())
        } else {
            null
        }
    }
}
