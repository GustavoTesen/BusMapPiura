import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.busmappiura.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class Rutas : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private var mapa: GoogleMap? = null

    // Define todas las rutas de IDA
    private val rutasIda: Map<String, List<LatLng>> = mapOf(
        "Ruta A" to listOf(
            LatLng(-5.19449, -80.63282),
            LatLng(-5.19300, -80.63000),
            LatLng(-5.19000, -80.62800)
        ),
        "Ruta B" to listOf(
            LatLng(-5.19449, -80.63282),
            LatLng(-5.19550, -80.63500),
            LatLng(-5.19800, -80.63700)
        ),
        "Ruta C" to listOf(
            LatLng(-5.20000, -80.64000),
            LatLng(-5.20200, -80.64200),
            LatLng(-5.20400, -80.64400)
        )
        // Agrega más rutas aquí...
    )

    // Define todas las rutas de VUELTA
    private val rutasVuelta: Map<String, List<LatLng>> = mapOf(
        "Ruta A" to listOf(
            LatLng(-5.19000, -80.62800),
            LatLng(-5.19300, -80.63000),
            LatLng(-5.19449, -80.63282)
        ),
        "Ruta B" to listOf(
            LatLng(-5.19800, -80.63700),
            LatLng(-5.19550, -80.63500),
            LatLng(-5.19449, -80.63282)
        ),
        "Ruta C" to listOf(
            LatLng(-5.20400, -80.64400),
            LatLng(-5.20200, -80.64200),
            LatLng(-5.20000, -80.64000)
        )
        // Agrega más rutas aquí...
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rutas)

        autoCompleteTextView = findViewById(R.id.auto)

        // Adaptador con todas las claves de rutasIda (asumiendo que Ida y Vuelta tienen las mismas claves)
        val nombresRutas = rutasIda.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombresRutas)
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val rutaSeleccionada = nombresRutas[position]
            mostrarRutaEnMapa(rutaSeleccionada)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapa = googleMap
        mapa?.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

    private fun mostrarRutaEnMapa(nombreRuta: String) {
        try {
            mapa?.clear()

            val ida = rutasIda[nombreRuta]
            val vuelta = rutasVuelta[nombreRuta]

            if ((ida == null || ida.isEmpty()) && (vuelta == null || vuelta.isEmpty())) {
                Toast.makeText(this, "No hay datos para la ruta $nombreRuta", Toast.LENGTH_SHORT).show()
                return
            }

            ida?.let {
                mapa?.addMarker(MarkerOptions().position(it.first()).title("Inicio - Ida"))
                mapa?.addPolyline(
                    PolylineOptions()
                        .addAll(it)
                        .color(Color.BLUE)
                        .width(10f)
                )
            }

            vuelta?.let {
                mapa?.addMarker(MarkerOptions().position(it.last()).title("Fin - Vuelta"))
                mapa?.addPolyline(
                    PolylineOptions()
                        .addAll(it)
                        .color(Color.RED)
                        .width(10f)
                )
            }

            val centro = ida?.firstOrNull() ?: vuelta?.firstOrNull()
            centro?.let {
                mapa?.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error al mostrar la ruta: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
