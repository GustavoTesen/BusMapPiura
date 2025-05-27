package com.example.busmappiura

import android.os.Bundle
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

class Rutas : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var textInputLayout: TextInputLayout
    private lateinit var opciones: List<String>
    private var esPrimeraVez: Boolean = true
    private var mapa: GoogleMap? = null

    // Cambié rutasMap para guardar par de listas: ida y vuelta
    private val rutasMap: Map<String, Pair<List<LatLng>, List<LatLng>>> = mapOf(
        "Ruta A" to Pair(
            listOf( // Ida
                LatLng(-5.19449, -80.63282),
                LatLng(-5.19300, -80.63000),
                LatLng(-5.19000, -80.62800)
            ),
            listOf( // Vuelta (distinta)
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
        // Puedes agregar más rutas con el mismo formato
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

                if (puntosIda.isNotEmpty()) {
                    mapa?.addMarker(MarkerOptions().position(puntosIda.first()).title("$nombreRuta - Inicio Ida"))
                    mapa?.addMarker(MarkerOptions().position(puntosIda.last()).title("$nombreRuta - Fin Ida"))

                    mapa?.moveCamera(CameraUpdateFactory.newLatLngZoom(puntosIda.first(), 15f))

                    val polylineIda = PolylineOptions()
                        .addAll(puntosIda)
                        .color(android.graphics.Color.BLUE)  // Azul para ida
                        .width(8f)

                    mapa?.addPolyline(polylineIda)
                }

                if (puntosVuelta.isNotEmpty()) {
                    mapa?.addMarker(MarkerOptions().position(puntosVuelta.first()).title("$nombreRuta - Inicio Vuelta"))
                    mapa?.addMarker(MarkerOptions().position(puntosVuelta.last()).title("$nombreRuta - Fin Vuelta"))

                    val polylineVuelta = PolylineOptions()
                        .addAll(puntosVuelta)
                        .color(android.graphics.Color.RED)  // Rojo para vuelta
                        .width(8f)

                    mapa?.addPolyline(polylineVuelta)
                }
            } else {
                Toast.makeText(this, "No hay puntos para la ruta seleccionada", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al mostrar ruta: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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
