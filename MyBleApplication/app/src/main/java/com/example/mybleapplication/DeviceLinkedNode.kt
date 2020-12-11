package com.example.mybleapplication

/* @author Sanford Johnston
 * @date November 25, 2020
 * CS 488 Senior Project
 * Aggie oCT COVID-19
 */

/******************************************************************************
 * A LinkedNode provides a node for a linked list with
 * time data in each node.
 *
 * @note
 *   Lists of nodes can be made of any length, limited only by the amount of
 *   free memory in the heap. But beyond Integer.MAX_VALUE (2,147,483,647),
 *   the answer from listLength is incorrect because of arithmetic
 *   overflow.
 *
 *   LinkedNode manages its relationship to other nodes in a time sequence list.
 */
class DeviceLinkedNode(){
    private var startTime: Long = System.currentTimeMillis()
    private var endTime: Long = startTime
    private var totalTime: Long = endTime - startTime
    private var future: DeviceLinkedNode = this
    private var past: DeviceLinkedNode = this
    /* Invariant of the DoubleNode class:
    //   1. The node's double data is in the instance variable data.
    //   2. For the final node of a list, the link part is null.
    //      Otherwise, the link part is a reference to the
    //      next node of the list. */

    /**
     * Modification method to add a new node to this node.
     * @postcondition
     *   A new node has been created and linked with this node.
     *   The data for the new node is the current time in milliseconds. Any other nodes
     *   that used to be after this node are now after the new node.
     **/
    open fun addFutureNode() {
        future = DeviceLinkedNode()
    }

    /**
     * Accessor method to get the total time from this node.
     * @param - none
     * @return
     *   totalTime
     **/
    open fun getTotalTime(): Long {
        return totalTime
    }

    /**
     * Accessor method to get the end time for this node.
     * @param - none
     * @return
     *   endTime
     **/
    open fun getEndTime(): Long {
        return endTime
    }

    /**
     * General method to update the total time for this node.
     * @param - none
     * @return
     *   none
     **/
    open fun updateTime(){
        endTime = System.currentTimeMillis()
        totalTime = endTime - startTime
    }


    /**
     * Accessor method to get a reference to the node that is ahead of this node.
     * @param - none
     * @return
     *     future: a reference to the node before this node (or the null reference if there is nothing
     *     before this node).
     */
    open fun getFuture(): DeviceLinkedNode{
        return future
    }

    /**
     * Setter method to set the start time for this node.
     * @param - sTime: a long integer object.
     * @return
     *   none
     **/
    open fun setNewTime(sTime: Long) {
        startTime = sTime
        updateTime()
    }

}