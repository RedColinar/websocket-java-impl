import http.HttpServer
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeout
import java.time.Duration

import java.util.concurrent.atomic.AtomicBoolean

class ClientServerTest {
  private lateinit var server: HttpServer
  private lateinit var client: TestClient
  private lateinit var okClient: OkTestClient

  @BeforeAll
  fun init() {
    server = HttpServer(8090)
    server.start()
    client = TestClient()
    okClient = OkTestClient()
  }

  @Test
  fun testRequestHttp() {
    client.requestHttp("http://localhost:8090/whatever")
    client.requestHttp("http://localhost:8090/hello")
  }

  private var isLoop = AtomicBoolean(true)
  private fun loop() {
    isLoop.set(true)
    while (isLoop.get()) {
    }
  }

  private fun endLoop() = isLoop.getAndSet(false)

  @Test()
  fun testOnOpen() {
    val ws = okClient.requestWs("ws://localhost:8090/chat", object : WebSocketListener() {
      override fun onOpen(webSocket: WebSocket, response: Response) {
        println("open")
        webSocket.send("hello")
      }

      override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        println(text)
        endLoop()
      }
    })

    assertTimeout(Duration.ofSeconds(3)) {
      loop()
    }
  }

  @Test
  fun testOnText() {

  }
}