package ws

import http.HttpRequest

interface WebSocket {

  fun request(): HttpRequest?

  fun queueSize(): Long

  fun send(text: String): Boolean

  fun send(bytes: ByteArray): Boolean

  fun close(code: Int, reason: String): Boolean

  fun cancel()

  interface Factory {
    fun create(endPoint: EndPoint): WebSocket
  }
}
