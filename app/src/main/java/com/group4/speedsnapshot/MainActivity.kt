package com.group4.speedsnapshot

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.group4.speedsnapshot.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var isTracking = false

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
        updateButtonState()

        binding.btnStart.setOnClickListener {
            if (isLocationPermissionGranted()) {
                startLocationUpdates()
                isTracking = true
                updateButtonState()
            } else {
                Toast.makeText(this, "Location permission is required to track speed.", Toast.LENGTH_SHORT).show()
                checkAndRequestLocationPermission()
            }
        }

        binding.btnStop.setOnClickListener {
            stopLocationUpdates()
            isTracking = false
            updateButtonState()
        }

        checkAndRequestLocationPermission()
    }

    override fun onPause() {
        super.onPause()
        if (isTracking) {
            Log.d("MainActivity", "onPause: Stopping tracking")
            stopLocationUpdates()
            isTracking = false
            updateButtonState()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isTracking) {
            Log.d("MainActivity", "onStop: Stopping tracking")
            stopLocationUpdates()
            isTracking = false
            updateButtonState()
        }
    }

    private fun setupLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateIntervalMillis(1000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return

                val speedKmH = location.speed * 3.6f
                val speedText = getString(R.string.speed_label, speedKmH)
                val accuracyText = getString(R.string.accuracy_label, location.accuracy)

                binding.tvSpeed.text = speedText
                binding.tvAccuracy.text = accuracyText
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
        updateButtonState()
    }

    private fun onLocationPermissionDenied() {
        isTracking = false
        updateButtonState()
        Toast.makeText(this, "Location permission is required to track speed.", Toast.LENGTH_SHORT).show()
    }

    private fun updateButtonState() {
        val granted = hasLocationPermission()
        binding.btnStart.isEnabled = granted && !isTracking
        binding.btnStop.isEnabled = granted && isTracking
    }

    fun isLocationPermissionGranted(): Boolean = hasLocationPermission()
}
