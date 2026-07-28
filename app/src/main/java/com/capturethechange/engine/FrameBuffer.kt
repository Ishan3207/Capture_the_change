package com.capturethechange.engine

class FrameBuffer {
    private var buffer = ArrayDeque<ByteArray>()
    private var capacity = 15

    fun updateCapacity(newCapacity: Int) {
        capacity = newCapacity
        while (buffer.size > capacity) {
            buffer.removeFirst()
        }
    }

    fun push(frame: ByteArray) {
        if (capacity <= 0) return
        while (buffer.size >= capacity) {
            buffer.removeFirst()
        }
        buffer.addLast(frame)
    }

    fun oldest(): ByteArray? = buffer.firstOrNull()
    fun latest(): ByteArray? = buffer.lastOrNull()
    fun clear() = buffer.clear()
    fun isFull() = buffer.size >= capacity
    val size: Int get() = buffer.size
}
