class HttpRequest(
  var method: String = "",
  var uri: String = "",
  var protocol: String = ""
) : HttpMessage() {
  override fun reset() {
    super.reset()
    method = ""
    uri = ""
    protocol = ""
  }

  override fun toString(): String = "$method $uri $protocol"
}