# Android OSC Library

A lightweight, Android-focused library for Open Sound Control (OSC) 1.0 communication. This library provides a simple and efficient way to send and receive OSC messages and bundles in your Android applications.

## Features

- **OSC 1.0 for Android**
- **Minimal dependencies**

## Installation

### Local Module

1.  Clone or download this repository.
2.  Copy the `osc-lib` directory into your project's root directory.
3.  In your project's `settings.gradle.kts` file, include the module:
    ```kotlin
    include(":osc-lib")
    ```
4.  In your app module's `build.gradle.kts`, add the dependency:
    ```kotlin
    dependencies {
        implementation(project(":osc-lib"))
    }
    ```

## Usage

### 1. Sending OSC Messages (Client)

To send messages, create an `OscClient` with the target IP address and port.

```kotlin
import com.osc.lib.net.OscClient
import com.osc.lib.core.OscMessage
import java.net.InetAddress

// Create a client (run this off the main thread if possible)
val ipAddress = InetAddress.getByName("192.168.1.50")
val port = 8000
val client = OscClient(ipAddress, port)

// Create a message
val message = OscMessage("/example/address", listOf(123, "Hello", 45.6f))

// Send the message
Thread {
    try {
        client.send(message)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}.start()

// Close the client when done
// client.close()
```

### 2. Receiving OSC Messages (Server)

To receive messages, create an `OscServer` listening on a specific port.

```kotlin
import com.osc.lib.net.OscServer

val port = 9000
val server = OscServer(port)

// Register a listener for a specific address pattern
server.getDispatcher().addListener("/fader/1") { message ->
    val value = message.arguments.firstOrNull() as? Float
    println("Received fader value: $value")
}

// Start the server
server.start()

// Stop the server when no longer needed (e.g., in onDestroy)
// server.stop()
```

### 3. Updating UI from OSC (Android)

Network operations run on background threads, but UI updates must happen on the Main Thread. Use `OscAndroid.mainThread` to safely update your UI.

```kotlin
import com.osc.lib.android.OscAndroid

server.getDispatcher().addListener("/label/text", OscAndroid.mainThread { message ->
    val newText = message.arguments.firstOrNull() as? String
    myTextView.text = newText
})
```

### 4. Advanced Usage

#### OSC Bundles

You can send multiple messages together as a bundle with a time tag.

```kotlin
import com.osc.lib.core.OscBundle
import com.osc.lib.core.OscTimeTag

val msg1 = OscMessage("/trigger/1", listOf(1))
val msg2 = OscMessage("/trigger/2", listOf(0.5f))

// Create a bundle to be executed immediately
val bundle = OscBundle(listOf(msg1, msg2), OscTimeTag.immediate())

client.send(bundle)
```

#### Pattern Matching

The server supports standard OSC pattern matching in address paths:
- `?`: Matches any single character
- `*`: Matches any sequence of zero or more characters
- `[chars]`: Matches any character in the set
- `{foo,bar}`: Matches either "foo" or "bar"

```kotlin
// Matches /fader/1, /fader/2, etc.
server.getDispatcher().addListener("/fader/*") { message ->
    println("Received message on: ${message.address}")
}
```

## License

MIT License (see License.md)
