package edu.kit.tm.ps.embertalk.sync

object MacAddressUtils {
    private val regex = "^([0-9A-F]{2}:){5}([0-9A-F]{2})$".toRegex()

    fun isValidMacAddress(macAddress: String): Boolean {
        return regex.matches(macAddress)
    }
}