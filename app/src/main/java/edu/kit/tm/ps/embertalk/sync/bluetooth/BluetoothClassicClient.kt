package edu.kit.tm.ps.embertalk.sync.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import edu.kit.tm.ps.embertalk.sync.Synchronizer
import java.io.IOException
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothClassicClient(device: BluetoothDevice, uuid: UUID, private val onSyncFinish: () -> Unit) : Runnable {
    var socket: BluetoothSocket? = null
    var macAddress: String? = null

    init {
        macAddress = device.address
        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid)
            Log.d(TAG, "Opened a socket successfully")
        } catch (connectException: IOException) {
            Log.e(TAG, "Failed to create socket", connectException)
        }

    }

    override fun run() {
        try {
            // This will block until there is a connection
            socket!!.connect()
            Log.d(TAG, "Connected to a server successfully.")
        } catch (connectException: IOException) {
            Log.e(TAG, "Failed to connect", connectException)
        }
        try {
            Synchronizer.bidirectionalSync(socket!!.inputStream, socket!!.outputStream)
            onSyncFinish.invoke()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to sync", e)
        }
        socket!!.close()
        Log.d(TAG, "Closed socket")
    }

    companion object {
        const val TAG = "BTCC"
    }
}