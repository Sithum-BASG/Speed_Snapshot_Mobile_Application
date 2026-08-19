package com.group4.speedsnapshot

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.group4.speedsnapshot.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            onLocationPermissionGranted()
        } else {
            onLocationPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLocationUpdates()
        checkAndRequestLocationPermission()
    }

    private fun setupLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateIntervalMillis(1000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return

                val speedText = "Speed: %.2f m/s".format(location.speed)
                val accuracyText = "Accuracy: %.1f m".format(location.accuracy)

                // Note: Using findViewById as placeholders as requested. 
                // These will need R.id.tvSpeed and R.id.tvAccuracy to exist in activity_main.xml
                findViewById<TextView>(R.id.tvSpeed)?.text = speedText
                findViewById<TextView>(R.id.tvAccuracy)?.text = accuracyText
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        // Missing permission check is intentional as per requirements (handled by teammate)
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun checkAndRequestLocationPermission() {
        if (hasLocationPermission()) {
            onLocationPermissionGranted()
            return
        }

        binding.permissionStatusText.text = getString(R.string.permission_status_rationale)
        permissionLauncher.launch(locationPermissions)
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    private fun onLocationPermissionGranted() {
        binding.permissionStatusText.text = getString(R.string.permission_status_granted)
    }

    private fun onLocationPermissionDenied() {
        binding.permissionStatusText.text = getString(R.string.permission_status_denied)
    }

    fun isLocationPermissionGranted(): Boolean = hasLocationPermission()
}
