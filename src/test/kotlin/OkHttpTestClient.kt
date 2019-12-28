import okhttp3.OkHttpClient
import okhttp3.Request

class OkHttpTestClient {
  val client = OkHttpClient()

  fun requestHttp(path: String) {
    val request: Request = Request.Builder().url(path).build()
  }
}