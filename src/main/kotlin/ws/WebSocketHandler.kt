package ws

import http.HttpHandler
import http.HttpRequest
import http.HttpResponse
import java.net.Socket

class WebSocketHandler(val endPoint: EndPoint) : HttpHandler {
  override fun handleRequest(socket: Socket, request: HttpRequest, response: HttpResponse): Boolean {

    return true
  }
}