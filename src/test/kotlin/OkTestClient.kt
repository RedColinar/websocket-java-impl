import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class OkTestClient {
  val client = OkHttpClient()

  fun requestHttp(path: String) {
    val request: Request = Request.Builder().url(path).build()
  }

  fun requestWs(path: String, listener: WebSocketListener): WebSocket {
    val request = Request.Builder().url(path).build()
    return client.newWebSocket(request, listener)
  }
}