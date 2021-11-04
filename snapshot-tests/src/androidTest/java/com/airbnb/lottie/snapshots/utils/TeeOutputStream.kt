package com.airbnb.lottie.snapshots.utils

import java.io.IOException
import java.io.OutputStream
import kotlin.Throws

/**
 * An output stream which copies anything written into it to another stream.
 */
class TeeOutputStream(private val output1: OutputStream, private val output2: OutputStream) : OutputStream() {
    @Throws(IOException::class)
    override fun write(buf: ByteArray) {
        output1.write(buf)
        output2.write(buf)
    }

    @Throws(IOException::class)
    override fun write(buf: ByteArray, off: Int, len: Int) {
        output1.write(buf, off, len)
        output2.write(buf, off, len)
    }

    @Throws(IOException::class)
    override fun write(b: Int) {
        output1.write(b)
        output2.write(b)
    }

    @Throws(IOException::class)
    override fun flush() {
        output1.flush()
        output2.flush()
    }

    @Throws(IOException::class)
    override fun close() {
        output1.close()
        output2.close()
    }
}