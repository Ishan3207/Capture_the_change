package com.capturethechange.engine

class FrameBuffer {
    private var buffer = ArrayDeque<ByteArray>()
    private var capacity = 15
    private val lock = Any()

    fun updateCapacity(newCapacity: Int) {
        synchronized(lock) {
            capacity = newCapacity
            while (buffer.size > capacity) {
                buffer.removeFirst()
            }
        }
    }

    fun push(frame: ByteArray) {
        synchronized(lock) {
            if (capacity <= 0) return
            while (buffer.size >= capacity) {
                buffer.removeFirst()
            }
            buffer.addLast(frame)
        }
    }

    fun oldest(): ByteArray? = synchronized(lock) { buffer.firstOrNull() }
    fun latest(): ByteArray? = synchronized(lock) { buffer.lastOrNull() }
    fun clear() = synchronized(lock) { buffer.clear() }
    fun isFull() = synchronized(lock) { buffer.size >= capacity }
    val size: Int get() = synchronized(lock) { buffer.size }
}
