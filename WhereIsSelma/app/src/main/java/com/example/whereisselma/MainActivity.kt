package com.example.whereisselma

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private val locationPermission = ACCESS_FINE_LOCATION
    private val locationRequestCode = 10
    private lateinit var locationManager: LocationManager
    private lateinit var map: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var soundPool: SoundPool
    private var loaded: Boolean = false
    var soundMap: HashMap<Int, Int> = HashMap()

    val REQUEST_IMAGE_CAPTURE = 1

    private val locationListener = object: LocationListener {
        override fun onProviderEnabled(provider: String) { }

        override fun onProviderDisabled(provider: String) { }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }

        override fun onLocationChanged(location: Location) {
            updateLocationDisplay(location)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        trackLocation()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        this.loadSounds()
    }

    private fun loadSounds() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.soundPool = SoundPool.Builder().setMaxStreams(10).build()
        } else {
            this.soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        }
        this.soundPool.setOnLoadCompleteListener { _, _, _ -> loaded = true }
        this.soundMap[R.raw.marker_added] = this.soundPool.load(this, R.raw.marker_added, 1)
    }

    fun playSound(selectedSound: Int) {
        val soundID = this.soundMap[selectedSound] ?: 0
        this.soundPool.play(soundID, 1f, 1f, 1, 0, 1f)
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Permission to take photo not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocationDisplay(location: Location?) {
        val lat = location?.latitude ?: 0
        val lon = location?.longitude ?: 0
        locationDisplay.text = "Lat: $lat\nLon: $lon"

        if(location != null && Geocoder.isPresent()) {
            val geocoder = Geocoder(this, Locale.getDefault());
            try {
                val nearByAddresses = geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                );

                if (nearByAddresses.size > 0) {
                    val stringBuilder = StringBuilder();
                    val nearestAddress = nearByAddresses[0];
                    stringBuilder.append("\n")
                            .append(nearestAddress.getAddressLine(0))
                            .append("\n")
                            .append(nearestAddress.locality)
                            .append("\n")
                            .append(nearestAddress.countryName);
                    locationDisplay.append(stringBuilder.toString());
                }
            } catch (e: IOException) {
                e.printStackTrace(); }
        }
    }

    private fun trackLocation() {
        if(hasPermissionCompat(locationPermission)){
            startTrackingLocation()
        } else {
            requestPermisionCompat(arrayOf(locationPermission), locationRequestCode)
        }
    }

    private fun startTrackingLocation() {
        Log.d("TAG", "Tracking location")

        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE

        val provider = locationManager.getBestProvider(criteria, true)
        val minTime = 1000L
        val minDistance = 10.0F
        try {
            if (provider != null) {
                locationManager.requestLocationUpdates(provider, minTime, minDistance, locationListener)
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    lastLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    placeMarkerOnMap(currentLatLng)
                    playSound(R.raw.marker_added)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                }
            }
        } catch (e: SecurityException){
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray) {

        when(requestCode){
            locationRequestCode -> {
                if(grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    trackLocation()
                else
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(locationListener)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            map = googleMap
        }

        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        map.addMarker(markerOptions)
    }

    override fun onMarkerClick(p0: Marker?): Boolean = false

}