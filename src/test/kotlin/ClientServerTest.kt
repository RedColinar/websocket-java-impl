import org.junit.Before
import org.junit.Test

class ClientServerTest {
  private val server = HttpServer(8090)

  @Before
  fun init() {
    server.start()
  }

  @Test
  fun testUpgrade() {
    val client = Client("127.0.0.1", 8090)
    client.connect()

  }
}