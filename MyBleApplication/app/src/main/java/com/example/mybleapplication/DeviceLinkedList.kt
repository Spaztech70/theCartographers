package com.example.mybleapplication

import android.bluetooth.le.ScanResult
import android.os.Build
import androidx.annotation.RequiresApi
import org.jetbrains.anko.db.NULL
import java.util.*
import kotlin.math.pow
/*
 * @author Sanford Johnston
 * @date November 25, 2020
 * CS 488 Senior Project
 * Aggie oCT COVID-19
 */

/**
 * Initialize a single node sequence.
 * @param - scanResult: a ScanResult object
 * <dt><b>Postcondition: the sequence has a single node. </b><dd>.
 **/

private const val N = 10.0
private const val TEN = 10.0
private const val METERS_TO_FEET = 3.28083333
private const val MEASURE_POWER_DEFAULT = 5.0
private const val ELAPSED_TIME_TO_NEW = 12000 // five minutes set to 300000

class DeviceLinkedList(scanResult: ScanResult){
    // Invariant of the DoubleLinkedSeq class:
    //   1. The linked sequence's number of nodes is in the instance variable manyNodes.
    //   2. The head node of the linked sequence is referenced by the instance variable head.
    //   3. The tail node of the linked sequence is referenced by the instance variable tail.
    //	4. The current node of the linked sequence is in the instance variable cursor.
    //	5. The node before the current node of the linked sequence is referenced in the instance variable precursor
    // any variables that I need for adding, removing nodes.
    @RequiresApi(Build.VERSION_CODES.O)
    private var dist = (if (scanResult.txPower > 0) {
        METERS_TO_FEET * TEN.pow(((scanResult.txPower) + (scanResult.rssi)).toDouble()/(TEN*N))
    }
    else {
        METERS_TO_FEET * TEN.pow((MEASURE_POWER_DEFAULT + (scanResult.rssi)).toDouble()/(TEN*N))
    })
    open var uniqueId = scanResult.device.address
    private var accumulatedTime = 0L
    open var head: DeviceLinkedNode = DeviceLinkedNode()
    open var tail = head
    private var cursor = head
    private var count = 0

    open fun getId(): String{
         return uniqueId
    }

    open fun updateTime(){
        // check if head endTime is > 5:01 minutes:seconds old
        // if greater, add new node to head.
        if ((System.currentTimeMillis() - head.getEndTime()) > ELAPSED_TIME_TO_NEW) {
            head.addFutureNode()
            head = head.getFuture()
            count++
        }
        // recalculate accumulated time
        cursor = tail
        accumulatedTime = 0
        do {
            accumulatedTime += cursor.getTotalTime()
            cursor = cursor.getFuture()
        } while (cursor != head)
    }

    // add node to chain
    open fun addHead() {
        head.addFutureNode()
        head = head.getFuture()
        updateTime()
        count++
    }

    open fun removeTail(){
        // if more than 1 node in chain
        if (tail != head) {
            tail = tail.getFuture()
            count--
        }
        // else only one node in chain
        else {
            tail.addFutureNode()
            tail = tail.getFuture()
            count = 1
        }
        setCurrentTime()
        updateTime()
    }

    open fun getAccTime(): Long{
        return accumulatedTime
    }

    open fun size(): Int {
        return count
    }

    @RequiresApi(Build.VERSION_CODES.O)
    open fun getDistance(): Double {
        return dist
    }

    @RequiresApi(Build.VERSION_CODES.O)
    open fun setDistance(distance: Double) {
        dist = distance
        setCurrentTime()
        updateTime()
    }

    open fun setCurrentTime(){
        head.updateTime()
    }

    open fun getCurrentTime(): Long{
        return head.getEndTime()
    }
}