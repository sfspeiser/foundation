package net.ntworld.foundation


import kotlin.ByteArray

interface MessageAttribute {
    val dataType: String

    val binaryValue: ByteArray?

    val stringValue: String?
}