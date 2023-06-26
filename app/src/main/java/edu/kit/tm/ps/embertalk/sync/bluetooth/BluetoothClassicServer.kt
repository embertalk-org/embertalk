package edu.kit.tm.ps.embertalk.sync.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.util.Log
import edu.kit.tm.ps.embertalk.sync.Synchronizer
import java.io.IOException
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothClassicServer(
    uuid: UUID,
    private val synchronizer: Synchronizer,
    private val bluetoothAdapter: BluetoothAdapter,
    private val taskQueue: MutableSet<String>
    ) : Thread() {
    private var serverSocket: BluetoothServerSocket? = null
    private var running = false

    init {
        try {
            serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(TAG, uuid)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to set up Bluetooth Classic connection", e)
        }

    }

    override fun run() {
        running = true
        while (bluetoothAdapter.isEnabled && running) {
            try {
                // This will block until there is a connection
                Log.d(TAG, "Listening for a client")
                val socket = serverSocket!!.accept()
                taskQueue.add(socket.remoteDevice.address)
                Log.d(TAG, "Opened a socket successfully")
                synchronizer.bidirectionalSync(socket.remoteDevice.address, socket.inputStream, socket.outputStream)
                socket.close()
                taskQueue.remove(socket.remoteDevice.address)
                Log.d(TAG, "Closed socket")
            } catch (connectException: IOException) {
                Log.e(TAG, "Failed to start a Bluetooth Classic connection", connectException)
            }
        }
    }

    fun shutdown() {
        running = false
    }

    companion object {
        const val TAG = "BTCS"
    }
}