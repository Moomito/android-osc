package com.osc.lib.core

/**
 * Represents an OSC Message.
 */
data class OscMessage(
    val address: String,
    val arguments: List<Any> = emptyList()
) : OscPacket {
    
    init {
        require(address.startsWith("/")) { "OSC Address must start with '/'" }
    }

    /**
     * Generates the OSC Type Tag String for this message
     *
     * @return The type tag string starting with a comma.
     * @throws IllegalArgumentException if an argument type is not supported.
     */
    fun getTypeTagString(): String {
        val sb = StringBuilder(",")
        for (arg in arguments) {
            when (arg) {
                is Int -> sb.append('i')
                is Float -> sb.append('f')
                is String -> sb.append('s')
                is ByteArray -> sb.append('b')
                is Long -> sb.append('h') // 64-bit int
                is Double -> sb.append('d') // 64-bit float
                is OscTimeTag -> sb.append('t')
                is Char -> sb.append('c')
                is Boolean -> sb.append(if (arg) 'T' else 'F')
                null -> sb.append('N')
                else -> throw IllegalArgumentException("Unsupported OSC argument type: ${arg.javaClass.simpleName}")
            }
        }
        return sb.toString()
    }
}
