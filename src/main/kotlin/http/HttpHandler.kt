package http

import java.net.Socket

interface HttpHandler {
  fun handleRequest(socket: Socket, request: HttpRequest, response: HttpResponse): Boolean
}