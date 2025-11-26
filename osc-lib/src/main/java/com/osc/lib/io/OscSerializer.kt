package com.osc.lib.io

import com.osc.lib.core.OscBundle
import com.osc.lib.core.OscMessage
import com.osc.lib.core.OscPacket
import com.osc.lib.core.OscTimeTag
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.charset.StandardCharsets

/**
 * Serializes OSC Packets into ByteArrays.
 */
class OscSerializer {

    /**
     * Serializes an OSC Packet into a byte array.
     *
     * @param packet The OSC Packet to serialize.
     * @return A byte array containing the serialized OSC data.
     */
    fun serialize(packet: OscPacket): ByteArray {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos) // DataOutputStream is Big-Endian by default

        writePacket(dos, packet)

        return baos.toByteArray()
    }

    private fun writePacket(dos: DataOutputStream, packet: OscPacket) {
        when (packet) {
            is OscMessage -> writeMessage(dos, packet)
            is OscBundle -> writeBundle(dos, packet)
        }
    }

    private fun writeMessage(dos: DataOutputStream, message: OscMessage) {
        writeString(dos, message.address)
        writeString(dos, message.getTypeTagString())

        for (arg in message.arguments) {
            when (arg) {
                is Int -> dos.writeInt(arg)
                is Float -> dos.writeFloat(arg)
                is String -> writeString(dos, arg)
                is ByteArray -> writeBlob(dos, arg)
                is Long -> dos.writeLong(arg)
                is Double -> dos.writeDouble(arg)
                is OscTimeTag -> dos.writeLong(arg.rawValue)
                is Char -> dos.writeInt(arg.code) // Char sent as 32-bit int
                is Boolean -> {} // No bytes for T/F
                null -> {} // No bytes for Nil
                else -> throw IllegalArgumentException("Unsupported argument type: ${arg.javaClass}")
            }
        }
    }

    private fun writeBundle(dos: DataOutputStream, bundle: OscBundle) {
        writeString(dos, "#bundle")
        dos.writeLong(bundle.timeTag.rawValue)

        for (element in bundle.elements) {
            // Recursively serialize the element to get its size
            val elementBytes = serialize(element)
            dos.writeInt(elementBytes.size)
            dos.write(elementBytes)
        }
    }

    private fun writeString(dos: DataOutputStream, str: String) {
        val bytes = str.toByteArray(StandardCharsets.UTF_8)
        dos.write(bytes)
        dos.write(0) // Null terminator
        align(dos, bytes.size + 1)
    }

    private fun writeBlob(dos: DataOutputStream, blob: ByteArray) {
        dos.writeInt(blob.size)
        dos.write(blob)
        align(dos, blob.size)
    }

    private fun align(dos: DataOutputStream, length: Int) {
        val pad = (4 - (length % 4)) % 4
        for (i in 0 until pad) {
            dos.write(0)
        }
    }
}
