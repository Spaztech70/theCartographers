package com.example.mybleapplication

import android.bluetooth.BluetoothClass
import android.bluetooth.le.ScanResult
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.scan_results.view.*
import org.jetbrains.anko.layoutInflater
import java.lang.Math.pow
import java.util.*
import kotlin.math.pow

class ScanResultAdapter (private val items: List<ScanResult>,
                         private val deviceClass: BluetoothClass.Device,
                         private val onClickListener: ((device: ScanResult) -> Unit)):
                        RecyclerView.Adapter<ScanResultAdapter.ViewHolder>(){
     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
         val view = parent.context.layoutInflater.inflate(
             R.layout.scan_results,
             parent,
             false
         )
         return ViewHolder(view, onClickListener)
     }

    override fun getItemCount() = items.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

     class ViewHolder(private val view: View,
                      private val onClickListener: ((device: ScanResult) -> Unit)) :
         RecyclerView.ViewHolder(view) {
         private lateinit var date: Date
         private val N = 10.0
         private val ten = 10.0
         private val metersToFeet = 3.28083333

         @RequiresApi(Build.VERSION_CODES.O)
         fun bind(result: ScanResult) {
             view.device_class.text = filterClassDevice(result.device.bluetoothClass.deviceClass)
             view.device_name.text = result.device.name ?: "Unnamed"
             view.device_mac_address.text = result.device.address
             view.device_signal.text = result.rssi.toString() + " dBm"
             val distance = metersToFeet * ten.pow(((result.txPower) + (result.rssi)).toDouble()/(ten*N))
             view.device_distance.text = (result.txPower).toString()
             view.device_distance.text = distance.toInt().toString() + " ft"
             view.setOnClickListener { onClickListener.invoke(result) }

         }

         // Takes the Device_Major identifier and converts it to a string for that device type
         private fun filterClassDevice(i: Int): CharSequence? {
             var name = "$i"
             when (i) {
                 0 -> name = "MISC"
                 256 -> name = "COMPUTER"
                 512 -> name = "PHONE"
                 768 -> name = "NETWORKING"
                 1024 -> name = "AUDIO VIDEO"
                 1280 -> name = "PERIPHERAL"
                 1536 -> name = "IMAGING"
                 1792 -> name = "WEARABLE"
                 2048 -> name = "TOY GAME"
                 2304 -> name = "HEALTH"
                 7936 -> name = "UNCATEGORIZED"
                 else -> name = "UNRECOGNIZED DEVICE"
             }
             return name
         }
     }
}

