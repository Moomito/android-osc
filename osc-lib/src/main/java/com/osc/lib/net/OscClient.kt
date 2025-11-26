package com.osc.lib.net

import com.osc.lib.core.OscPacket
import com.osc.lib.io.OscSerializer
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * A simple UDP Client for sending OSC Packets.
 */
class OscClient(private val address: InetAddress, private val port: Int) {

    private val socket = DatagramSocket()
    private val serializer = OscSerializer()

    /**
     * Sends an OSC Packet to the configured address and port.
     *
     * @param packet The OSC Packet (Message or Bundle) to send.
     * @throws java.io.IOException If an I/O error occurs.
     */
    fun send(packet: OscPacket) {
        val bytes = serializer.serialize(packet)
        val datagramPacket = DatagramPacket(bytes, bytes.size, address, port)
        socket.send(datagramPacket)
    }

    /**
     * Closes the underlying DatagramSocket.
     */
    fun close() {
        socket.close()
    }
}
