package com.osc.lib.dispatch

import com.osc.lib.core.OscBundle
import com.osc.lib.core.OscMessage
import com.osc.lib.core.OscPacket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Dispatches OSC messages to registered listeners.
 */
class OscDispatcher {

    private val listeners = ConcurrentHashMap<String, (OscMessage) -> Unit>()
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    /**
     * Registers a listener for a specific OSC Address Pattern.
     *
     * @param address The OSC Address Pattern to listen for.
     * @param listener The callback function to be invoked when a matching message is received.
     */
    fun addListener(address: String, listener: (OscMessage) -> Unit) {
        listeners[address] = listener
    }

    fun removeListener(address: String) {
        listeners.remove(address)
    }

    /**
     * Dispatches an OSC Packet to the appropriate listeners.
     *
     * If the packet is a Bundle, its elements are scheduled for dispatch based on the time tag.
     * If the packet is a Message, it is dispatched immediately to matching listeners.
     *
     * @param packet The OSC Packet to dispatch.
     */
    fun dispatch(packet: OscPacket) {
        when (packet) {
            is OscMessage -> dispatchMessage(packet)
            is OscBundle -> dispatchBundle(packet)
        }
    }

    private fun dispatchMessage(message: OscMessage) {
        // Find all listeners that match the message's address pattern
        for ((methodAddress, listener) in listeners) {
            if (OscPatternMatcher.matches(message.address, methodAddress)) {
                try {
                    listener(message)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun dispatchBundle(bundle: OscBundle) {
        val now = System.currentTimeMillis()
        val tagTime = bundle.timeTag.toDate().time
        
        if (tagTime > now) {
             val delay = tagTime - now
             scheduler.schedule({
                 for (element in bundle.elements) {
                     dispatch(element)
                 }
             }, delay, TimeUnit.MILLISECONDS)
        } else {
            // Immediate dispatch
            for (element in bundle.elements) {
                dispatch(element)
            }
        }
    }
    
    fun shutdown() {
        scheduler.shutdown()
    }
}
