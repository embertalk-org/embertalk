package edu.kit.tm.ps.embertalk.sync.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import edu.kit.tm.ps.embertalk.sync.Synchronizer
import java.io.IOException


@SuppressLint("MissingPermission")
class BluetoothClassicClient(
    device: BluetoothDevice,
    private val synchronizer: Synchronizer,
    private val onSyncEnd: () -> Unit,
    private val onSyncSuccessful: () -> Unit
) : Runnable {
    private var socket: BluetoothSocket? = null
    private var macAddress: String? = null

    init {
        macAddress = device.address
        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(ServiceUtils.SDP_UUID)
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
            onSyncEnd.invoke()
            socket!!.close()
            Log.d(TAG, "Closed socket")
            return
        }
        try {
            val successful = synchronizer.bidirectionalSync(socket!!.remoteDevice.address, socket!!.inputStream, socket!!.outputStream)
            if (successful) {
                onSyncSuccessful.invoke()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to sync", e)
        }
        onSyncEnd.invoke()
        socket!!.close()
        Log.d(TAG, "Closed socket")
    }

    companion object {
        const val TAG = "BTCC"
    }
}