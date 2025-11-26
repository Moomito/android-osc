package com.osc.lib.io

import com.osc.lib.core.OscBundle
import com.osc.lib.core.OscMessage
import com.osc.lib.core.OscPacket
import com.osc.lib.core.OscTimeTag
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

/**
 * Parses ByteArrays into OSC Packets.
 */
class OscParser {

    /**
     * Parses a byte array into an OSC Packet (Message or Bundle).
     *
     * @param bytes The byte array containing the OSC data.
     * @return The parsed OSC Packet.
     * @throws IllegalArgumentException If the data is malformed or contains unsupported types.
     */
    fun parse(bytes: ByteArray): OscPacket {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        return parsePacket(buffer)
    }

    private fun parsePacket(buffer: ByteBuffer): OscPacket {
        // Peek at the first string to see if it's "#bundle" or an address
        buffer.mark()
        val firstString = readString(buffer)
        buffer.reset()

        return if (firstString == "#bundle") {
            parseBundle(buffer)
        } else {
            parseMessage(buffer)
        }
    }

    private fun parseBundle(buffer: ByteBuffer): OscBundle {
        readString(buffer) // Consume "#bundle"
        val timeTag = OscTimeTag(buffer.long)
        val elements = mutableListOf<OscPacket>()

        while (buffer.hasRemaining()) {
            val size = buffer.int
            // Create a slice for the element to ensure isolation
            val elementBuffer = buffer.slice().order(ByteOrder.BIG_ENDIAN)
            elementBuffer.limit(size)
            
            elements.add(parsePacket(elementBuffer))
            
            // Advance original buffer
            buffer.position(buffer.position() + size)
        }

        return OscBundle(timeTag, elements)
    }

    private fun parseMessage(buffer: ByteBuffer): OscMessage {
        val address = readString(buffer)
        val arguments = mutableListOf<Any>()

        // Check for type tags
        if (buffer.hasRemaining()) {
            buffer.mark()
            val possibleTypeTags = readString(buffer)
            if (possibleTypeTags.startsWith(",")) {
                // Parse arguments based on type tags
                for (i in 1 until possibleTypeTags.length) {
                    when (val typeChar = possibleTypeTags[i]) {
                        'i' -> arguments.add(buffer.int)
                        'f' -> arguments.add(buffer.float)
                        's' -> arguments.add(readString(buffer))
                        'b' -> arguments.add(readBlob(buffer))
                        'h' -> arguments.add(buffer.long)
                        'd' -> arguments.add(buffer.double)
                        't' -> arguments.add(OscTimeTag(buffer.long))
                        'c' -> arguments.add(buffer.int.toChar())
                        'T' -> arguments.add(true)
                        'F' -> arguments.add(false)
                        'N' -> arguments.add(Unit)
                        else -> {
                            throw IllegalArgumentException("Unsupported OSC type tag: $typeChar")
                        }
                    }
                }
            } else {
                // Reset buffer position if no type tags found
                buffer.reset()
            }
        }

        // Filter out Unit/Nulls if added for N tag
        val cleanArgs = arguments.filter { it != Unit }
        
        return OscMessage(address, cleanArgs)
    }

    private fun readString(buffer: ByteBuffer): String {
        val start = buffer.position()
        while (buffer.get() != 0.toByte()) {
            // Scan for null terminator
        }
        val end = buffer.position() - 1
        val length = end - start
        
        val bytes = ByteArray(length)
        buffer.position(start)
        buffer.get(bytes)
        buffer.position(end + 1) // Skip the null

        align(buffer)
        
        return String(bytes, StandardCharsets.UTF_8)
    }

    private fun readBlob(buffer: ByteBuffer): ByteArray {
        val size = buffer.int
        val bytes = ByteArray(size)
        buffer.get(bytes)
        align(buffer)
        return bytes
    }

    private fun align(buffer: ByteBuffer) {
        val pos = buffer.position()
        val pad = (4 - (pos % 4)) % 4
        buffer.position(pos + pad)
    }
}
