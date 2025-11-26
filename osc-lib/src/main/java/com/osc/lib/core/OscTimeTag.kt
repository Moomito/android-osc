package com.osc.lib.core

import java.util.Date

/**
 * Represents an OSC Time Tag.
 */
data class OscTimeTag(val rawValue: Long) {

    companion object {
        val IMMEDIATE = OscTimeTag(1)
        private const val SECONDS_FROM_1900_TO_1970 = 2208988800L

        fun now(): OscTimeTag {
            val millis = System.currentTimeMillis()
            val secondsSince1970 = millis / 1000
            val secondsSince1900 = secondsSince1970 + SECONDS_FROM_1900_TO_1970
            
            val fraction = ((millis % 1000) * 4294967.296).toLong()
            
            // Combine: high 32 bits = seconds, low 32 bits = fraction
            val raw = (secondsSince1900 shl 32) or (fraction and 0xFFFFFFFFL)
            return OscTimeTag(raw)
        }
    }

    fun toDate(): Date {
        if (this == IMMEDIATE) return Date()
        
        val secondsSince1900 = (rawValue ushr 32)
        val fraction = (rawValue and 0xFFFFFFFFL)
        
        val secondsSince1970 = secondsSince1900 - SECONDS_FROM_1900_TO_1970
        val millisFromFraction = (fraction / 4294967.296).toLong()
        
        return Date((secondsSince1970 * 1000) + millisFromFraction)
    }
}
