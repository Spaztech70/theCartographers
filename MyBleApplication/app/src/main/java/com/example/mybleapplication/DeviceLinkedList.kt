package com.example.mybleapplication

import android.bluetooth.le.ScanResult
import android.os.Build
import androidx.annotation.RequiresApi
import org.jetbrains.anko.db.NULL
import java.util.*
import kotlin.math.pow


/*
    DoubleLinkedSeq manages the nodes in a time sequence list

     * @author Sanford Johnston
     * @date   March 1, 2017
     * CS 272 Data Structures
     * Lab 4 (HW3) Linked Lists - Sequences of Doubles
     * The purpose of this class is to practice working with linked lists and
     * 		maximize on using other methods to improve efficiency
     * Seven methods have been added
     * 	reverse Method: returns a new sequence that contains the same elements as the original sequence but in reverse order
     * 	everyOther Method: returns a new sequence that contains every other element of the original sequence
     * 	removeSmaller Method: removes from the sequence all elements that are less than a given value
     * 	addAtFront Method: adds a new element to the front of the sequence
     * 	removeAtFront Method: removes an element from the front of the sequence
     * 	toIth Method: makes the ith element the current element
     * 	toString Method: represents the sequence as a string
 */
// File: DoubleLinkedSeq.java from the package edu.colorado.collections

// This is an assignment for students to complete after reading Chapter 4 of
// "Data Structures and Other Objects Using Java" by Michael Main.

/******************************************************************************
 * This class is a homework assignment;
 * A DoubleLinkedSeq is a collection of double numbers.
 * The sequence can have a special "current element," which is specified and
 * accessed through four methods that are not available in the sequence class
 * (start, getCurrent, advance and isCurrent).
 *
 * <dl><dt><b>Limitations:</b>
 *   Beyond Int.MAX_VALUE elements, the size method
 *   does not work.
 *
 * <dt><b>Note:</b><dd>
 *   This file contains only blank implementations ("stubs")
 *   because this is a Programming Project for my students.
 *
 * <dt><b>Outline of Java Source Code for this class:</b><dd>
 *   <A HREF="../../../../edu/colorado/collections/DoubleLinkedSeq.java">
 *   http://www.cs.colorado.edu/~main/edu/colorado/collections/DoubleLinkedSeq.java
 *   </A>
 *   </dl>
 *
 * @version
 *   Nov 19, 2020
 ******************************************************************************/


/**
 * Initialize an empty sequence.
 * @param - none
 * <dt><b>Postcondition: the sequence is empty. </b><dd>
 *
 *   This sequence is empty.
 **/

private const val N = 10.0
private const val TEN = 10.0
private const val METERS_TO_FEET = 3.28083333
private const val MEASURE_POWER_DEFAULT = 7.0
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
        MEASURE_POWER_DEFAULT
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