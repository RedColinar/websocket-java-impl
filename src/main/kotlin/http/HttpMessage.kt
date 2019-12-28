package http

import java.util.ArrayList

const val CONTENT_TYPE = "Content-Type"
const val CONTENT_LENGTH = "Content-Length"

const val CONTENT_TYPE_PLAIN = "text/plain"

open class HttpMessage {
  val headerNames = ArrayList<String>()
  val headerValues = ArrayList<String>()

  fun addHeader(name: String, value: String) {
    headerNames.add(name)
    headerValues.add(value)
  }

  fun getFirstHeaderValue(name: String): String? {
    for (i in 0 until headerNames.size) {
      if (name == headerNames[i]) {
        return headerValues[i]
      }
    }
    return null
  }

  open fun reset() {
    headerNames.clear()
    headerValues.clear()
  }
}