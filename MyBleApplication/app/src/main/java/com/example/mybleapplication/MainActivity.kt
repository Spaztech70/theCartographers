package com.example.mybleapplication

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

import org.jetbrains.anko.alert
import java.lang.Math.*
import kotlin.math.pow

// import timber.log.Timber

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2

class MainActivity : AppCompatActivity() {

    /*******************************************
     * Properties: variables and values
     *******************************************/
    //Sets the label for the button between Start/Stop Scan
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }
    private val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    //Sets the label for the button between Start/Stop Scan
    private var isScanning = false
        set(value) {
            field = value
            runOnUiThread { scan_button.text = if (value) "Stop Scan" else "Start Scan" }
        }

    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    private val scanResults = mutableListOf<ScanResult>()
    private val deviceClass = BluetoothClass.Device()
    // private val device: BluetoothDevice()


    private val deviceGattMap = ConcurrentHashMap<BluetoothDevice, BluetoothGatt>()
    private fun BluetoothDevice.isConnected() = deviceGattMap.containsKey(this)

    private val scanSafeResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults, deviceClass) { result ->
            if (isScanning) stopBleScan()
        }
    }
    private val scanCautionResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults, deviceClass) { result ->
            if (isScanning) stopBleScan()
        }
    }
    private val scanThreatResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults, deviceClass) { result ->
            if (isScanning) stopBleScan()
        }
    }


    /*******************************************
     * Activity function overrides
     *******************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scan_button.setOnClickListener {
            if (isScanning) stopBleScan()
            else startBleScan()
        }
        setupSafeRecyclerView()
        setupCautionRecyclerView()
        setupThreatRecyclerView()
    }
/*
    override fun onResume() {
        super.onResume()
        // ConnectionManager.registerListener(connectionEventListener)
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }
    }
*/
/*
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    startBleScan()
                }
            }
        }
    }
*/
    /*******************************************
     * Private functions
     *******************************************/
    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    // TODO implement an interval scan in this function
    // TODO 1) Scan for 1 second, record list of found devices
    // TODO 2) Pause scan for 10 seconds, update database of found devices
    // TODO 3) go to step 1

    private fun startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            scanResults.clear()
            scanSafeResultAdapter.notifyDataSetChanged()
            scanCautionResultAdapter.notifyDataSetChanged()
            scanThreatResultAdapter.notifyDataSetChanged()
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
        }
        // DEBUG LINE -> REMOVE
        isScanning = true
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }



    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }

        runOnUiThread {
            alert {
                title = "Location permission required"
                message = "Starting from Android M (6.0), the system requires apps to be granted " +
                        "location access in order to scan for BLE devices."
                isCancelable = false
                positiveButton(android.R.string.ok) {
                    requestPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            }.show()
        }

    }

    private fun setupSafeRecyclerView() {
        scan_results_safe_view.apply {
            adapter = scanSafeResultAdapter
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = scan_results_safe_view.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    private fun setupCautionRecyclerView() {
        scan_results_caution_view.apply {
            adapter = scanCautionResultAdapter
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = scan_results_caution_view.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    private fun setupThreatRecyclerView() {
        scan_results_threat_view.apply {
            adapter = scanThreatResultAdapter
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = scan_results_threat_view.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    /*******************************************
     * Callback bodies
     *******************************************/

    private val scanCallback = object : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            // I wonder if I create an if-else chain here such that
            val N = 10.0
            val ten = 10.0
            val metersToFeet = 3.28083333
            /*
            Formula:
            Distance
            Measured Power
            RSSI
            N (Constant depends on the Environmental factor. Range 2-4)
            Distance = 10 ^ ((Measured Power – RSSI)/(10 * N))
             */
            var distance = metersToFeet * ten.pow((result.txPower + result.rssi).toDouble() / (ten * N))

            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                if (distance < 6.5) {
                    scanSafeResultAdapter.notifyItemChanged(indexQuery)
                } else if (distance < 15.0) {
                    scanCautionResultAdapter.notifyItemChanged(indexQuery)
                }
                else {
                    scanThreatResultAdapter.notifyItemChanged(indexQuery)
                }
            } else {
                with(result.device) {
                    // Timber.i("Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                }
                scanResults.add(result)
                if (distance < 6.5) {
                    scanSafeResultAdapter.notifyItemInserted(scanResults.size - 1)
                } else if (distance < 15.0) {
                    scanCautionResultAdapter.notifyItemInserted(scanResults.size - 1)
                }
                else {
                    scanThreatResultAdapter.notifyItemInserted(scanResults.size - 1)
                }
            }
            // In ScanResultAdapter, a list of nearby devices should have been updated in a database object
            // after 1 second, stop BLE scan
            // Record devices to database
            // in database, CHECK: for each device to be updated, remove entries for that device > 24 hours old
            // in ROOM database, add new contact entry for each device (each entry = 10 seconds of contact)
            // in ROOM database, CHECK: if a device has been in cumulative contact for > 15 minutes, popup a warning to the user
            // after 10 seconds, resume BLE scan
            // TODO Timber records logs. A log of each device
        }

        override fun onScanFailed(errorCode: Int) {
            // Timber.e("onScanFailed: code $errorCode")
        }
    }

    /*******************************************
     * Extension functions
     *******************************************/
    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }
}