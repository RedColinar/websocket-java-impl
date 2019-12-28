package ws

import http.HttpRequest

class RealWebSocketFactory : WebSocket.Factory {
  override fun create(endPoint: EndPoint): WebSocket {
    return object : WebSocket {
      override fun request(): HttpRequest? {
        return null
      }

      override fun queueSize(): Long {
        return 0
      }

      override fun send(text: String): Boolean {
        return false
      }

      override fun send(bytes: ByteArray): Boolean {
        return false
      }

      override fun close(code: Int, reason: String): Boolean {
        return false
      }

      override fun cancel() {

      }
    }
  }
}