package com.example.mybleapplication

/******************************************************************************
 * A DoubleNode provides a node for a linked list with
 * double data in each node.
 *
 * @note
 *   Lists of nodes can be made of any length, limited only by the amount of
 *   free memory in the heap. But beyond Integer.MAX_VALUE (2,147,483,647),
 *   the answer from listLength is incorrect because of arithmetic
 *   overflow.
 *
 * @see
 *   <A HREF="../../../../edu/colorado/nodes/DoubleNode.java">
 *   Java Source Code for this class
 *   (www.cs.colorado.edu/~main/edu/colorado/nodes/DoubleNode.java) </A>
 *
 * @author Michael Main
 *   <A HREF="mailto:main@colorado.edu"> (main@colorado.edu) </A>
 *
 * @version
 *   March 6, 2002
 *
 * @see Node
 * @see BooleanNode
 * @see ByteNode
 * @see CharNode
 * @see FloatNode
 * @see IntNode
 * @see LongNode
 * @see ShortNode
 ******************************************************************************/
/*
    DoubleNode manages its relationship to other nodes in a time sequence list.
 */
class DeviceLinkedNode(){
    private var startTime: Long = System.currentTimeMillis()
    private var endTime: Long = startTime
    private var totalTime: Long = endTime - startTime
    private var future: DeviceLinkedNode = this
    private var past: DeviceLinkedNode = this
    // Invariant of the DoubleNode class:
    //   1. The node's double data is in the instance variable data.
    //   2. For the final node of a list, the link part is null.
    //      Otherwise, the link part is a reference to the
    //      next node of the list.

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
     * Accessor method to get the data from this node.
     * @param - none
     * @return
     *   the data from this node
     **/
    open fun getTotalTime(): Long {
        return totalTime
    }

    open fun getEndTime(): Long {
        return endTime
    }

    /**
     * Accessor method to get a reference to the next node after this node.
     * @param - none
     * @return
     *   a reference to the node after this node (or the null reference if there
     *   is nothing after this node)
     **/
    open fun updateTime(){
        endTime = System.currentTimeMillis()
        totalTime = endTime - startTime
    }


    open fun getFuture(): DeviceLinkedNode{
        return future
    }

    open fun setNewTime(sTime: Long) {
        startTime = sTime
        updateTime()
    }

}