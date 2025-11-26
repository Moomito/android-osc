package com.osc.lib.core

/**
 * Represents an OSC Bundle.
 */
data class OscBundle(
    val timeTag: OscTimeTag = OscTimeTag.IMMEDIATE,
    val elements: List<OscPacket> = emptyList()
) : OscPacket
