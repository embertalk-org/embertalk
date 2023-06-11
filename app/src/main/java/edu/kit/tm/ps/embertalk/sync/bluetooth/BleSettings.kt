package edu.kit.tm.ps.embertalk.sync.bluetooth

import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import java.util.UUID

object BleSettings {

    fun buildAdvertiseData(serviceUuidAndAddress: UUID?): AdvertiseData {
        val builder = AdvertiseData.Builder()
        builder.addServiceUuid(ParcelUuid(serviceUuidAndAddress))
        builder.setIncludeDeviceName(false)
        return builder.build()
    }

    fun buildAdvertiseSettings(): AdvertiseSettings {
        val builder = AdvertiseSettings.Builder()
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
        builder.setTimeout(0) // Advertise as long as Bluetooth is on, blatantly ignoring Google's advice.
        builder.setConnectable(false)
        return builder.build()
    }

    fun buildScanSettings(): ScanSettings {
        val builder = ScanSettings.Builder()
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)

        builder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        builder.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
        builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)

        return builder.build()
    }

}