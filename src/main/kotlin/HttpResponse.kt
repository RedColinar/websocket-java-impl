class HttpResponse(
  var code: Int = 0,
  var reasonPhrase: String = "",
  var body: HttpBody? = null
) : HttpMessage() {

  fun prepare() {
    if (body != null) {
      addHeader(CONTENT_TYPE, body!!.contentType())
      addHeader(CONTENT_LENGTH, body!!.contentLength().toString())
    }
  }

  override fun reset() {
    super.reset()
    this.code = -1
    this.reasonPhrase = ""
    this.body = null
  }
}