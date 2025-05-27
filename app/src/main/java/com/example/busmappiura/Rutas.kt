package com.example.busmappiura

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class Rutas : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var textInputLayout: TextInputLayout
    private lateinit var opciones: List<String>
    private var esPrimeraVez: Boolean = true
    private var mapa: GoogleMap? = null

    // TU API KEY DE GOOGLE MAPS - REEMPLAZA CON LA TUYA
    private val API_KEY = "AIzaSyAjEMOPRIS1dkx3HzT3mQ-DYgtMXAynARs"

    // Ahora solo guardamos puntos de parada, la ruta se calculará con Directions API
    private val rutasMap: Map<String, Pair<List<LatLng>, List<LatLng>>> = mapOf(
        "Ruta A" to Pair(
            listOf( // Puntos de parada ida
                LatLng(-5.19449, -80.63282), // Plaza de Armas
                LatLng(-5.19300, -80.63000), // Punto intermedio
                LatLng(-5.19000, -80.62800)  // Destino
            ),
            listOf( // Puntos de parada vuelta
                LatLng(-5.19000, -80.62800),
                LatLng(-5.19150, -80.62950),
                LatLng(-5.19449, -80.63282)
            )
        ),
        "Ruta B" to Pair(
            listOf(
                LatLng(-5.19449, -80.63282),
                LatLng(-5.19550, -80.63500),
                LatLng(-5.19800, -80.63700)
            ),
            listOf(
                LatLng(-5.19800, -80.63700),
                LatLng(-5.19600, -80.63400),
                LatLng(-5.19449, -80.63282)
            )
        ),
        "Ruta C" to Pair(
            listOf(
                LatLng(-5.1916959, -80.6647480),
                LatLng(-5.19100, -80.63400),
                LatLng(-5.18800, -80.63600)
            ),
            listOf(
                LatLng(-5.18800, -80.63600),
                LatLng(-5.18950, -80.65000),
                LatLng(-5.1916959, -80.6647480)
            )
        ),
        "Ruta D" to Pair(
            listOf(
                LatLng(-5.19449, -80.63282),
                LatLng(-5.1916959, -80.6647480)
            ),
            listOf(
                LatLng(-5.1916959, -80.6647480),
                LatLng(-5.19300, -80.64000),
                LatLng(-5.19449, -80.63282)
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rutas)

        try {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
            mapFragment?.getMapAsync(this)

            autoCompleteTextView = findViewById(R.id.auto)
            textInputLayout = findViewById(R.id.text_input_layout)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        if (userId == null) {
            Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userDocRef = db.collection("usuarios").document(userId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                try {
                    if (!document.exists()) {
                        esPrimeraVez = true
                        userDocRef.set(hashMapOf("primeraVez" to true))
                    } else {
                        esPrimeraVez = document.getBoolean("primeraVez") ?: true
                    }

                    opciones = if (esPrimeraVez) {
                        listOf(
                            "Seleccione una ruta",
                            "Ruta A",
                            "Ruta B",
                            "Ruta C",
                            "Ruta D",
                            "Ruta E",
                            "Ruta F",
                            "Ruta G",
                            "Ruta H"
                        )
                    } else {
                        listOf(
                            "Ruta A",
                            "Ruta B",
                            "Ruta C",
                            "Ruta D",
                            "Ruta E",
                            "Ruta F",
                            "Ruta G",
                            "Ruta H"
                        )
                    }

                    val adapter = ArrayAdapter(this,R.layout.list_item, opciones)
                    autoCompleteTextView.setAdapter(adapter)

                    autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
                        val rutaSeleccionada = parent.getItemAtPosition(position) as String

                        if (esPrimeraVez && rutaSeleccionada == "Seleccione una ruta") {
                            Toast.makeText(
                                this@Rutas,
                                "Por favor, seleccione una ruta válida",
                                Toast.LENGTH_SHORT
                            ).show()
                            autoCompleteTextView.setText("", false)
                        } else {
                            Toast.makeText(this@Rutas, "Seleccionaste: $rutaSeleccionada", Toast.LENGTH_SHORT).show()

                            if (esPrimeraVez) {
                                userDocRef.update("primeraVez", false)
                                esPrimeraVez = false
                            }

                            mostrarRutaEnMapa(rutaSeleccionada)
                        }
                    }

                    autoCompleteTextView.threshold = 0
                    autoCompleteTextView.setOnClickListener {
                        autoCompleteTextView.showDropDown()
                    }

                    if (esPrimeraVez) {
                        autoCompleteTextView.setText("Seleccione una ruta", false)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@Rutas, "Error al configurar opciones: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al obtener datos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarRutaEnMapa(nombreRuta: String) {
        try {
            mapa?.clear()

            val ruta = rutasMap[nombreRuta]
            if (ruta != null) {
                val puntosIda = ruta.first
                val puntosVuelta = ruta.second

                // Para ida
                if (puntosIda.isNotEmpty()) {
                    // Agregar marcadores de paradas
                    for (i in puntosIda.indices) {
                        val titulo = when (i) {
                            0 -> "$nombreRuta - Inicio Ida"
                            puntosIda.size - 1 -> "$nombreRuta - Fin Ida"
                            else -> "$nombreRuta - Parada ${i}"
                        }
                        mapa?.addMarker(MarkerOptions().position(puntosIda[i]).title(titulo))
                    }

                    // Dibujar ruta ida usando Directions API
                    dibujarRutaConDirections(puntosIda, android.graphics.Color.BLUE)

                    mapa?.moveCamera(CameraUpdateFactory.newLatLngZoom(puntosIda.first(), 15f))
                }

                // Para vuelta
                if (puntosVuelta.isNotEmpty()) {
                    // Agregar marcadores de paradas
                    for (i in puntosVuelta.indices) {
                        val titulo = when (i) {
                            0 -> "$nombreRuta - Inicio Vuelta"
                            puntosVuelta.size - 1 -> "$nombreRuta - Fin Vuelta"
                            else -> "$nombreRuta - Parada Vuelta ${i}"
                        }
                        mapa?.addMarker(MarkerOptions().position(puntosVuelta[i]).title(titulo))
                    }

                    // Dibujar ruta vuelta usando Directions API
                    dibujarRutaConDirections(puntosVuelta, android.graphics.Color.RED)
                }
            } else {
                Toast.makeText(this, "No hay puntos para la ruta seleccionada", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al mostrar ruta: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dibujarRutaConDirections(puntos: List<LatLng>, color: Int) {
        if (puntos.size < 2) return

        // Usar corrutina para la llamada de red
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val rutaCompleta = obtenerRutaDirections(puntos)

                withContext(Dispatchers.Main) {
                    if (rutaCompleta.isNotEmpty()) {
                        val polylineOptions = PolylineOptions()
                            .addAll(rutaCompleta)
                            .color(color)
                            .width(8f)

                        mapa?.addPolyline(polylineOptions)
                    } else {
                        // Fallback: línea recta si falla la API
                        val polylineOptions = PolylineOptions()
                            .addAll(puntos)
                            .color(color)
                            .width(8f)

                        mapa?.addPolyline(polylineOptions)
                        Toast.makeText(this@Rutas, "Usando ruta directa (sin calles)", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Rutas, "Error al obtener ruta: ${e.message}", Toast.LENGTH_SHORT).show()

                    // Fallback: línea recta
                    val polylineOptions = PolylineOptions()
                        .addAll(puntos)
                        .color(color)
                        .width(8f)

                    mapa?.addPolyline(polylineOptions)
                }
            }
        }
    }

    private suspend fun obtenerRutaDirections(puntos: List<LatLng>): List<LatLng> {
        return withContext(Dispatchers.IO) {
            val rutaCompleta = mutableListOf<LatLng>()

            // Para cada par de puntos consecutivos, obtener la ruta
            for (i in 0 until puntos.size - 1) {
                val origen = puntos[i]
                val destino = puntos[i + 1]

                val segmentoRuta = obtenerSegmentoRuta(origen, destino)

                if (i == 0) {
                    rutaCompleta.addAll(segmentoRuta)
                } else {
                    // Evitar duplicar el punto de conexión
                    rutaCompleta.addAll(segmentoRuta.drop(1))
                }
            }

            rutaCompleta
        }
    }

    private fun obtenerSegmentoRuta(origen: LatLng, destino: LatLng): List<LatLng> {
        val puntosRuta = mutableListOf<LatLng>()

        try {
            val origenStr = "${origen.latitude},${origen.longitude}"
            val destinoStr = "${destino.latitude},${destino.longitude}"

            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${URLEncoder.encode(origenStr, "UTF-8")}&" +
                    "destination=${URLEncoder.encode(destinoStr, "UTF-8")}&" +
                    "key=$API_KEY"

            Log.d("DirectionsAPI", "URL: $url")

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            Log.d("DirectionsAPI", "Response Code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                reader.close()
                connection.disconnect()

                Log.d("DirectionsAPI", "Response: ${response.toString()}")

                val jsonResponse = JSONObject(response.toString())

                // Verificar si hay errores en la respuesta
                val status = jsonResponse.getString("status")
                Log.d("DirectionsAPI", "Status: $status")

                if (status == "OK") {
                    val routes = jsonResponse.getJSONArray("routes")

                    if (routes.length() > 0) {
                        val route = routes.getJSONObject(0)
                        val legs = route.getJSONArray("legs")

                        for (i in 0 until legs.length()) {
                            val leg = legs.getJSONObject(i)
                            val steps = leg.getJSONArray("steps")

                            for (j in 0 until steps.length()) {
                                val step = steps.getJSONObject(j)
                                val polyline = step.getJSONObject("polyline")
                                val encodedPoints = polyline.getString("points")

                                // Decodificar polyline
                                puntosRuta.addAll(decodificarPolyline(encodedPoints))
                            }
                        }
                        Log.d("DirectionsAPI", "Puntos obtenidos: ${puntosRuta.size}")
                    }
                } else {
                    Log.e("DirectionsAPI", "Error en API: $status")
                    if (jsonResponse.has("error_message")) {
                        Log.e("DirectionsAPI", "Error message: ${jsonResponse.getString("error_message")}")
                    }
                }
            } else {
                Log.e("DirectionsAPI", "HTTP Error: $responseCode")
                // Leer el error stream
                val errorStream = connection.errorStream
                if (errorStream != null) {
                    val errorReader = BufferedReader(InputStreamReader(errorStream))
                    val errorResponse = StringBuilder()
                    var errorLine: String?
                    while (errorReader.readLine().also { errorLine = it } != null) {
                        errorResponse.append(errorLine)
                    }
                    Log.e("DirectionsAPI", "Error Response: ${errorResponse.toString()}")
                    errorReader.close()
                }
            }

        } catch (e: Exception) {
            Log.e("DirectionsAPI", "Exception: ${e.message}", e)
            // En caso de error, devolver línea recta
            puntosRuta.add(origen)
            puntosRuta.add(destino)
        }

        return puntosRuta
    }

    private fun decodificarPolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }

    override fun onMapReady(googleMap: GoogleMap) {
        try {
            mapa = googleMap

            val ubicacionInicial = LatLng(-5.19449, -80.63282)
            mapa?.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionInicial, 13f))
            mapa?.addMarker(MarkerOptions().position(ubicacionInicial).title("Ubicación inicial"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar mapa: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}