package com.osc.lib.net

import com.osc.lib.dispatch.OscDispatcher
import com.osc.lib.io.OscParser
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A simple UDP Server for receiving OSC Packets.
 */
class OscServer(private val port: Int) {

    private val dispatcher = OscDispatcher()
    private val parser = OscParser()
    private var socket: DatagramSocket? = null
    private val isRunning = AtomicBoolean(false)
    private var thread: Thread? = null

    /**
     * Starts the server in a new thread.
     */
    fun start() {
        if (isRunning.get()) return

        socket = DatagramSocket(port)
        isRunning.set(true)

        thread = Thread {
            val buffer = ByteArray(65535) // Max UDP size
            val packet = DatagramPacket(buffer, buffer.size)

            while (isRunning.get()) {
                try {
                    socket?.receive(packet)
                    
                    // Copy data to ensure thread safety
                    val data = buffer.copyOfRange(0, packet.length)
                    
                    try {
                        val oscPacket = parser.parse(data)
                        dispatcher.dispatch(oscPacket)
                    } catch (e: Exception) {
                        System.err.println("Error parsing/dispatching OSC packet: ${e.message}")
                    }
                    
                } catch (e: Exception) {
                    if (isRunning.get()) {
                        e.printStackTrace()
                    }
                }
            }
        }.apply { start() }
    }

    /**
     * Stops the server and closes the socket.
     */
    fun stop() {
        isRunning.set(false)
        socket?.close()
        socket = null
        try {
            thread?.join(1000)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    /**
     * Returns the OscDispatcher associated with this server.
     */
    fun getDispatcher(): OscDispatcher {
        return dispatcher
    }
}
