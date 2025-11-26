package com.osc.lib.dispatch

import java.util.regex.Pattern

/**
 * Utility for matching OSC Address Patterns.
 *
 * Supports the following wildcards:
 * - '?' matches any single character
 * - '*' matches any sequence of zero or more characters
 * - '[chars]' matches any character in the set
 * - '[!chars]' matches any character not in the set
 * - '{string,string}' matches any of the comma-separated strings
 */
object OscPatternMatcher {

/**
     * Checks if an OSC Address matches an OSC Address Pattern.
     *
     * @param pattern The OSC Address Pattern (e.g., "/foo/\*"). // <--- Backslash added
     * @param address The OSC Address to check (e.g., "/foo/bar").
     * @return True if the address matches the pattern, false otherwise.
     */
    fun matches(pattern: String, address: String): Boolean {
        // 1. The OSC Address and the OSC Address Pattern contain the same number of parts
        val patternParts = pattern.split("/").filter { it.isNotEmpty() }
        val addressParts = address.split("/").filter { it.isNotEmpty() }

        if (patternParts.size != addressParts.size) {
            return false
        }

        // 2. Each part of the OSC Address Pattern matches the corresponding part of the OSC Address
        for (i in patternParts.indices) {
            if (!matchPart(patternParts[i], addressParts[i])) {
                return false
            }
        }

        return true
    }

    private fun matchPart(patternPart: String, addressPart: String): Boolean {
        // Convert OSC pattern to Regex
        val regex = StringBuilder("^")
        var i = 0
        while (i < patternPart.length) {
            val c = patternPart[i]
            when (c) {
                '?' -> regex.append(".")
                '*' -> regex.append(".*")
                '[' -> {
                    regex.append("[")
                    i++
                    if (i < patternPart.length && patternPart[i] == '!') {
                        regex.append("^") // Negation in regex
                        i++
                    }
                    while (i < patternPart.length && patternPart[i] != ']') {
                        if (patternPart[i] == '-' && i + 1 < patternPart.length && patternPart[i+1] != ']') {
                             // Range
                             regex.append("-")
                        } else {
                            regex.append(Pattern.quote(patternPart[i].toString()))
                        }
                        i++
                    }
                    regex.append("]")
                }
                '{' -> {
                    regex.append("(")
                    i++
                    val sb = StringBuilder()
                    while (i < patternPart.length && patternPart[i] != '}') {
                        sb.append(patternPart[i])
                        i++
                    }
                    // Split by comma and OR them
                    val options = sb.toString().split(",")
                    regex.append(options.joinToString("|") { Pattern.quote(it) })
                    regex.append(")")
                }
                else -> regex.append(Pattern.quote(c.toString()))
            }
            i++
        }
        regex.append("$")

        return Pattern.compile(regex.toString()).matcher(addressPart).matches()
    }
}
