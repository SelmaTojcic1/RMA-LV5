package com.example.whereisselma

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val locationPermission = ACCESS_FINE_LOCATION
    private val locationRequestCode = 10
    private lateinit var locationManager: LocationManager
    private lateinit var map: GoogleMap
    private lateinit var lastLocation: Location

    private val locationListener = object: LocationListener {
        override fun onProviderEnabled(provider: String) { }

        override fun onProviderDisabled(provider: String) { }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }

        override fun onLocationChanged(location: Location) {
            lastLocation = location
            updateLocationDisplay(location)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        trackLocation()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        try{
            if (provider != null) {
                locationManager.requestLocationUpdates(provider, minTime, minDistance, locationListener)
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

        val myLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
        map.addMarker(MarkerOptions().position(myLocation).title("I'm here!"))
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        map.uiSettings.isZoomControlsEnabled = true
        map.moveCamera(CameraUpdateFactory.newLatLng(myLocation))
    }

}