package com.osc.lib.android

import android.os.Handler
import android.os.Looper
import com.osc.lib.core.OscMessage

/**
 * Helper to run OSC listeners on the Android Main (UI) Thread.
 */
object OscAndroid {

    private val handler = Handler(Looper.getMainLooper())

    /**
     * Wraps a listener lambda so that it executes on the Main Thread.
     * Use this when you need to update UI elements from an OSC message.
     */
    fun mainThread(listener: (OscMessage) -> Unit): (OscMessage) -> Unit {
        return { message ->
            handler.post {
                listener(message)
            }
        }
    }
}
