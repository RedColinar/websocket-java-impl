import http.HttpServer
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class ClientServerTest {
  private lateinit var server: HttpServer
  private lateinit var client: TestClient
  private lateinit var okClient: OkTestClient

  @Before
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

  @Test(timeout = 3 * 1000)
  fun testUpgrade() {
    val ws = okClient.requestWs("ws://localhost:8090/chat", object : WebSocketListener() {
      override fun onOpen(webSocket: WebSocket, response: Response) {
        println("open")
        endLoop()
      }
    })
    loop()
  }

  var isLoop = AtomicBoolean(true)
  private fun loop() {
    while (isLoop.get()) {

    }
  }

  private fun endLoop() = isLoop.getAndSet(false)
}