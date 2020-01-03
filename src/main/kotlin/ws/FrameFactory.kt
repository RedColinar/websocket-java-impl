package ws

import kotlin.experimental.xor

/** 解掩码 */
fun mask(maskKey: ByteArray, data: ByteArray, _offset: Int, _count: Int) {
  var offset = _offset
  var count = _count
  var index = 0
  while (count-- > 0) {
    // maskKey.size = 4
    data[offset] = data[offset] xor maskKey[index % maskKey.size]
    offset++
    index++
  }
}

fun createTextFrame(payload: String): Frame {
  return createSimpleFrame(Frame.OPCODE_TEXT_FRAME, payload.toByteArray())
}

fun createBinaryFrame(payload: ByteArray): Frame {
  return createSimpleFrame(Frame.OPCODE_BINARY_FRAME, payload)
}

fun createCloseFrame(closeCode: Int, closeReason: String): Frame {
  val reasonPhraseEncoded: ByteArray?
  var payloadLen = 2
  reasonPhraseEncoded = closeReason.toByteArray()
  payloadLen += reasonPhraseEncoded.size
  val payload = ByteArray(payloadLen)
  payload[0] = (closeCode shr 8 and 0xff).toByte()
  payload[1] = (closeCode and 0xff).toByte()
  System.arraycopy(reasonPhraseEncoded, 0, payload, 2, reasonPhraseEncoded.size)
  return createSimpleFrame(Frame.OPCODE_CONNECTION_CLOSE, payload, payloadLen)
}

fun createPingFrame(payload: ByteArray, payloadLen: Int): Frame {
  return createSimpleFrame(Frame.OPCODE_CONNECTION_PING, payload, payloadLen)
}

fun createPongFrame(payload: ByteArray, payloadLen: Int): Frame {
  return createSimpleFrame(Frame.OPCODE_CONNECTION_PONG, payload, payloadLen)
}

private fun createSimpleFrame(opCode: Byte, payload: ByteArray, payloadLen: Int): Frame {
  val frame = Frame()
  frame.fin = true
  frame.hasMask = false
  frame.opcode = opCode
  frame.payloadData = payload
  frame.payloadLen = payloadLen.toLong()
  return frame
}

private fun createSimpleFrame(opCode: Byte, payload: ByteArray): Frame {
  return createSimpleFrame(opCode, payload, payload.size)
}