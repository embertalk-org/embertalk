package edu.kit.tm.ps.embertalk.sync.bluetooth

import android.os.ParcelUuid
import java.util.UUID

object ServiceUtils {

    val SERVICE_UUID = UUID.fromString("7ba323d4-7021-23c4-0000-000000000000")!!
    val SERVICE_MASK = UUID.fromString("FFFFFFFF-FFFF-FFFF-0000-000000000000")!!

    fun matchesService(uuid: UUID): Boolean {
        return SERVICE_UUID.mostSignificantBits == uuid.mostSignificantBits
    }

    fun toUuid(macAddress: String): UUID {
        return UUID(SERVICE_UUID.mostSignificantBits, toLong(macAddress))
    }

    fun fromParcelUuid(serviceUuid: ParcelUuid): String {
        return fromLong(serviceUuid.uuid.leastSignificantBits)
    }

    private fun toLong(macAddress: String): Long {
        return java.lang.Long.parseLong(macAddress.replace(":".toRegex(), ""), 16)
    }

    private fun fromLong(macAddressLong: Long): String {
        return String.format("%02x:%02x:%02x:%02x:%02x:%02x",
            (macAddressLong shr 40).toByte(),
            (macAddressLong shr 32).toByte(),
            (macAddressLong shr 24).toByte(),
            (macAddressLong shr 16).toByte(),
            (macAddressLong shr 8).toByte(),
            macAddressLong.toByte()).uppercase()
    }
}