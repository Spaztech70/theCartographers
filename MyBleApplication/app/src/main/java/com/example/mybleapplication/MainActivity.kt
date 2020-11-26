package com.example.mybleapplication
/*
 * @author Sanford Johnston
 * @date November 25, 2020
 * CS 488 Senior Project
 * Aggie oCT COVID-19
 */

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock.sleep
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.delay
import org.jetbrains.anko.alert
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.pow

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2
private const val CAUTION_DISTANCE = 15.0
private const val THREAT_DISTANCE = 6.0 // TODO change to 6
private const val N = 10.0
private const val TEN = 10.0
private const val METERS_TO_FEET = 3.28083333
private const val MEASURED_POWER_DEFAULT = 5.0
private const val MILLISECONDS = 10000
private const val CROWD_MIN = 6         // TODO change to 6
private const val BUBBLE_MAX = 1        // TODO set at 1
private const val ONE_DAY = 86400000 // milliseconds in one day
private const val CONTINUOUS_CONTACT = 300 // seconds TODO change to 300
private const val ACCUMULATED_CONTACT = 900 // seconds TODO change to 900
private const val CROWD_MESSAGE = "!!ALERT!!\nYou are in a crowd.\nGet clear."
private const val BUBBLE_MESSAGE = "!!ALERT!!\nSomeone is too close.\nMove away."
private const val CONTINUOUS_CONTACT_MESSAGE = "!!ALERT!! Continuous contact with same person. Maintain distance."
private const val ACCUMULATED_CONTACT_MESSAGE = "!!ALERT!! Repeated contact with same person. Stay safe."

var listDevice = mutableListOf<DeviceLinkedList>()
var storeDevice = mutableListOf<DeviceLinkedList>()

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
    private val scanSettings =
        ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    //Sets the label for the button between Start/Stop Scan
    private var isScanning = false
        set(value) {
            field = value
            runOnUiThread { scan_button.text = if (value) "Stop Scan" else "Start Scan" }
        }

    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    private val scanResults = mutableListOf<ScanResult>()
    private val threatScanResults = mutableListOf<DeviceLinkedList>()
    private val cautionScanResults = mutableListOf<DeviceLinkedList>()
    private val safeScanResults = mutableListOf<DeviceLinkedList>()
    private val deviceClass = BluetoothClass.Device()


    private val deviceGattMap = ConcurrentHashMap<BluetoothDevice, BluetoothGatt>()
    private fun BluetoothDevice.isConnected() = deviceGattMap.containsKey(this)

    private val scanSafeResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(safeScanResults, deviceClass) { result ->
            if (isScanning) stopBleScan()
        }
    }
    private val scanCautionResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(cautionScanResults, deviceClass) { result ->
            if (isScanning) stopBleScan()
        }
    }
    private val scanThreatResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(threatScanResults, deviceClass) { result ->
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

    /*******************************************
     * Private functions
     *******************************************/
    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    private fun startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            // scanResults.clear()
            threatScanResults.clear()
            cautionScanResults.clear()
            safeScanResults.clear()
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
            isNestedScrollingEnabled = true
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
            /*
            Formula:
            Distance
            Measured Power
            RSSI
            N (Constant depends on the Environmental factor. Range 2-4)
            Distance = 10 ^ ((Measured Power – RSSI)/(10 * N))
             */
            var distance = if(result.txPower > 0) {
                METERS_TO_FEET * TEN.pow((result.txPower + result.rssi).toDouble() / (TEN * N))
            } else {
                MEASURED_POWER_DEFAULT
            }

            /*
            The contents of each display are coming from here.
             */
            /* we can store the results of the scan in the DeviceLinkedList here
                * device ID (mac or UUID)
                * distance
             * check deviceList for existence of device
             * update list
                * update distance OR
                * add device to list
             * sort list
             * store markers for begin/end indexes for the 3 safety zones
             * call the different scan Results Adapters and assign it a new begin/end portion of the deviceList to display
             */
            var found = false
            var stored = false
            var i = 0
            var index = 0

            // search stored list for device
            while (index < storeDevice.size && !stored) {
                if (storeDevice[index].getId() == result.device.address){
                    stored = true
                    i = index
                    storeDevice[index].setDistance(distance)
                }
                index++
            }
            if (stored){
                listDevice.add(storeDevice[i])
                storeDevice.removeAt(i)
                listDevice[listDevice.size-1].addHead()
            }

            // search active list for device
            index = 0
            while (index < listDevice.size && !found) {
                if (listDevice[index].getId() == result.device.address) {
                    found = true
                    i = index
                    listDevice[index].setDistance(distance)
                    if (listDevice[index].getDistance() <= CAUTION_DISTANCE) {
                        listDevice[index].updateTime()
                    }
                }
                index++
            }

            if (found && listDevice[i].getAccTime() > ACCUMULATED_CONTACT*1000){
                // TODO SHOW SNACKBAR
                Snackbar.make(findViewById(R.id.threat_view), ACCUMULATED_CONTACT_MESSAGE, Snackbar.LENGTH_LONG).show()
            }
            if (found && listDevice[i].head.getTotalTime() > CONTINUOUS_CONTACT*1000){
                // TODO SHOW SNACKBAR
                Snackbar.make(findViewById(R.id.threat_view), CONTINUOUS_CONTACT_MESSAGE, Snackbar.LENGTH_LONG).show()
            }

            // New device. Add to active list
            if (!found && !stored){
                // add device to deviceList
                listDevice.add(DeviceLinkedList(result))
            }

            // check times for active devices. Move inactive devices to stored list
            index = 0
            while (index < listDevice.size) {
                if ((System.currentTimeMillis() - listDevice[index].tail.getEndTime() > ONE_DAY)) {
                        listDevice[index].removeTail()
                    }
                if ((System.currentTimeMillis() - listDevice[index].getCurrentTime()) > MILLISECONDS) {
                    storeDevice.add(listDevice[index])
                    listDevice.removeAt(index)
                }
                index++
            }

            // sort active device list
            if (listDevice.size > 1) {
                listDevice.sortBy { it.getDistance() }
            }

            // clear each of the recycler view ScanResult lists
            // dataList.clear()
            threatScanResults.clear()
            cautionScanResults.clear()
            safeScanResults.clear()
            // notify each of the recycler views that the set has changed
            // recyclerView?.adapter?.notifyDataSetChanged()
            scanThreatResultAdapter.notifyDataSetChanged()
            scanCautionResultAdapter.notifyDataSetChanged()
            scanSafeResultAdapter.notifyDataSetChanged()
            // re-populate the recycler view lists
            index = 0
            while (index < listDevice.size) {
                if (listDevice[index].getDistance() <= THREAT_DISTANCE)
                    threatScanResults.add(listDevice[index])
                else if (listDevice[index].getDistance() <= CAUTION_DISTANCE)
                    cautionScanResults.add(listDevice[index])
                else safeScanResults.add(listDevice[index])
                index++
            }
            // call each of the recycler view ScanResult lists
            // recyclerView?.adapter?.notifyDataSetChanged()
            scanThreatResultAdapter.notifyDataSetChanged()
            scanCautionResultAdapter.notifyDataSetChanged()
            scanSafeResultAdapter.notifyDataSetChanged()
            if (cautionScanResults.size + threatScanResults.size >= CROWD_MIN) {
                // TODO SHOW SNACKBAR
                Snackbar.make(findViewById(R.id.threat_view), CROWD_MESSAGE, Snackbar.LENGTH_LONG).show()
            }
            if (threatScanResults.size >= BUBBLE_MAX) {
                // TODO SHOW SNACKBAR
                Snackbar.make(findViewById(R.id.threat_view), BUBBLE_MESSAGE, Snackbar.LENGTH_LONG).show()
            }
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