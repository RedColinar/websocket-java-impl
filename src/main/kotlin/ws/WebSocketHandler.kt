package ws

import http.*
import java.net.Socket
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class WebSocketHandler(val endPoint: EndPoint) : HttpHandler {

  override fun handleRequest(socket: Socket, request: HttpRequest, response: HttpResponse): Boolean {
    if (!isSupportUpgradeRequest(request)) {
      response.code = HTTP_NOT_IMPLEMENTED
      response.reasonPhrase = "Not Implemented"
      response.body = createBody("Not a supported WebSocket upgrade request\n", "text/plain")
      return true
    }
    doUpgrade(socket, request, response)
    return false
  }

  private fun isSupportUpgradeRequest(request: HttpRequest): Boolean {
    return "Upgrade" == getFirstHeaderValue(request, "Connection")
            && "websocket" == getFirstHeaderValue(request, "Upgrade")
            && "13" == getFirstHeaderValue(request, "Sec-WebSocket-Version")
  }

  private fun getFirstHeaderValue(message: HttpMessage, headerName: String): String? {
    return message.getFirstHeaderValue(headerName)
  }

  private fun doUpgrade(socket: Socket, request: HttpRequest, response: HttpResponse) {
    response.code = HTTP_SWITCHING_PROTOCOLS
    response.reasonPhrase = "Switching Protocols"
    response.addHeader("Upgrade", "websocket")
    response.addHeader("Connection", "Upgrade")
    response.body = null
    val clientKey = getFirstHeaderValue(request, "Sec-WebSocket-Key")
    if (clientKey != null) {
      response.addHeader("Sec-WebSocket-Accept", generateServerKey(clientKey))
    }
  }

  private fun generateServerKey(clientKey: String): String {
    return try {
      val serverKey = clientKey + Companion.SERVER_KEY_GUID
      val sha1 = MessageDigest.getInstance("SHA-1")
      sha1.update(serverKey.toByteArray())
      Base64.getEncoder().encodeToString(sha1.digest())
    } catch (e: NoSuchAlgorithmException) {
      throw RuntimeException(e)
    }
  }

  companion object {
    private const val SERVER_KEY_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
  }
}