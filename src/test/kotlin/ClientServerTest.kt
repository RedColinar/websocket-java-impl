import http.HttpServer
import org.junit.Before
import org.junit.Test
import ws.Session
import ws.SimpleEndPoint
import java.util.concurrent.atomic.AtomicBoolean

class ClientServerTest {
  lateinit var server: HttpServer
  lateinit var client: TestClient

  @Before
  fun init() {
    server = HttpServer(8090)
    server.start()
    client = TestClient()
  }

  @Test
  fun testRequestHttp() {
    client.requestHttp("http://localhost:8090/whatever")
    client.requestHttp("http://localhost:8090/hello")
  }

  @Test
  fun testUpgrade() {
    val ws = client.newWebSocket("ws://localhost:8090/chat", object : SimpleEndPoint() {
      override fun onOpen(session: Session) {
        super.onOpen(session)
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