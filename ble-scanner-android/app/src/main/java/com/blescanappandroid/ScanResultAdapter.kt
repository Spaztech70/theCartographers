/*
 * Copyright 2019 Punch Through Design LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blescanappandroid

import android.bluetooth.BluetoothClass
import android.bluetooth.le.ScanResult
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_scan_result.view.device_class
import kotlinx.android.synthetic.main.row_scan_result.view.device_name
import kotlinx.android.synthetic.main.row_scan_result.view.mac_address
import kotlinx.android.synthetic.main.row_scan_result.view.signal_strength
import org.jetbrains.anko.layoutInflater

class ScanResultAdapter(
    private val items: List<ScanResult>,
    private val deviceClass: BluetoothClass.Device,
    private val onClickListener: ((device: ScanResult) -> Unit)

) : RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.context.layoutInflater.inflate(
            R.layout.row_scan_result,
            parent,
            false
        )
        return ViewHolder(view, deviceClass, onClickListener)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    class ViewHolder(
        private val view: View,
        private val deviceClass: BluetoothClass.Device,
        private val onClickListener: ((device: ScanResult) -> Unit),
        private var fileData: String = ""

    ) : RecyclerView.ViewHolder(view) {

        fun bind(result: ScanResult) {
            view.device_class.text = filterClassDevice(result.device.bluetoothClass.deviceClass)
            view.device_name.text = result.device.name ?: "Unnamed"
            view.mac_address.text = result.device.address
            view.signal_strength.text = "${result.rssi} dBm"
            view.setOnClickListener { onClickListener.invoke(result) }
            fileData += "Device class: " + filterClassDevice(result.device.bluetoothClass.deviceClass).toString() + ", "
            fileData += "RSSI signal: " + "${result.rssi} dBm" + ", "
            fileData += "Device name: " + (result.device.name ?: "Unnamed") + ", "
            fileData += "Device address: " + result.device.address + "\n"
            Log.d("ble_log.txt", fileData)

        }

        private fun filterClassDevice(i: Int): CharSequence? {
            var name = i.toString()
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
