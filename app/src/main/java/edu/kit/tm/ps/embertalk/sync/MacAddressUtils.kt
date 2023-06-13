package edu.kit.tm.ps.embertalk.sync

object MacAddressUtils {
    private val regex = "^([0-9A-Fa-f]{2}:){5}([0-9A-Fa-f]{2})$".toRegex()

    fun isValidMacAddress(macAddress: String): Boolean {
        return regex.matches(macAddress)
    }
}