package com.example.mybleapplication

/* @author Sanford Johnston
 * @date November 25, 2020
 * CS 488 Senior Project
 * Aggie oCT COVID-19
 */

import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.scan_results.view.*
import org.jetbrains.anko.layoutInflater

/* ScanResultAdapter displays the scan results from a list of DeviceLinkedList objects
 * @param items is a list of DeviceLinkedList(s)
 * @param onClickListener: triggers the display of devices when a scan is started.
 * Scan results are displayed in a RecyclerView
 */
class ScanResultAdapter(
    private val items: List<DeviceLinkedList>,
    private val device: BluetoothClass.Device,
    private val onClickListener: ((device: DeviceLinkedList) -> Unit)) :
    RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {
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


    /* ViewHolder populates the MAC address, time of contact and distance of a device in
     * a scan_result window.
     * @param view is the View that holds a scan_result object.
     * @onClickListener triggers the display of devices when a scan is started.
     */
    class ViewHolder(
        private val view: View,
        private val onClickListener: ((device: DeviceLinkedList) -> Unit)
    ) :
        RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(result: DeviceLinkedList) {
            // PULL DEVICE INFO FROM ARRAY OF LINKEDLIST
            view.device_mac_address.text = result.getId()
            view.device_distance.text = "%.2f".format(result.getDistance()) + "ft"
            view.time_delta.text = "%.2f".format((result.getAccTime() / 1000).toDouble()) + "sec"
            view.setOnClickListener {
                onClickListener.invoke(result)
            }

        }
    }
}

