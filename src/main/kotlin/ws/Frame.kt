package ws

class Frame {
  companion object {
    const val OPCODE_TEXT_FRAME: Byte = 0x1
    const val OPCODE_BINARY_FRAME: Byte = 0x2
    const val OPCODE_CONNECTION_CLOSE: Byte = 0x8
    const val OPCODE_CONNECTION_PING: Byte = 0x9
    const val OPCODE_CONNECTION_PONG: Byte = 0xA
  }
}