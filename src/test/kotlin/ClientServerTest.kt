import http.HttpServer
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertTimeoutPreemptively
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientServerTest {
  private lateinit var server: HttpServer
  private lateinit var okClient: OkTestClient

  @BeforeAll
  fun initTest() {
    server = HttpServer(8090)
    server.start()
    okClient = OkTestClient()
  }

  @Test
  fun testRequestHttp() {
    val response1 = okClient.requestHttp("http://localhost:8090/whatever")
    val response2 = okClient.requestHttp("http://localhost:8090/hello")
    assert(response1.code == 404)
    assert(response2.code == 200)
  }

  @Test
  fun testOnOpen() {
    okClient.newWebSocket("ws://localhost:8090/chat", object : WebSocketListener() {
      override fun onOpen(webSocket: WebSocket, response: Response) {
        println("client open")
        webSocket.send("客户端发送消息：hello")
      }

      override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        println("客户端接收消息：$text")

        assert("server 发送消息 hello" == text)

        webSocket.close(1001, "test end")
        endLoop()
      }

      override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        println("client close")
      }
    })

    assertTimeoutPreemptively(Duration.ofSeconds(3)) {
      loop()
    }
  }

  private val isLoop = AtomicBoolean(true)
  private fun loop() {
    isLoop.set(true)
    while (isLoop.get()) {
    }
  }

  private fun endLoop() = isLoop.getAndSet(false)
}