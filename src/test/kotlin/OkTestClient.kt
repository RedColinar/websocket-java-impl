import okhttp3.*

class OkTestClient {
  private val client = OkHttpClient()

  fun requestHttp(path: String): Response {
    val request: Request = Request.Builder().url(path).build()
    return client.newCall(request).execute()
  }

  fun newWebSocket(path: String, listener: WebSocketListener): WebSocket {
    val request = Request.Builder().url(path).build()
    return client.newWebSocket(request, listener)
  }
}