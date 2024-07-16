package edu.kit.tm.ps.embertalk.sync.bluetooth

import android.bluetooth.BluetoothDevice
import android.util.Log
import edu.kit.tm.ps.embertalk.sync.Synchronizer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class ClientExecutorService {

    private val taskQueue: MutableSet<String> = ConcurrentHashMap.newKeySet()
    private val service = Executors.newCachedThreadPool()

    fun enqueue(remoteDevice: BluetoothDevice, synchronizer: Synchronizer) {
        if (!taskQueue.contains(remoteDevice.address)) {
            taskQueue.add(remoteDevice.address)
            val client = BluetoothClassicClient(
                remoteDevice,
                synchronizer,
            ) { taskQueue.remove(remoteDevice.address) }
            service.submit(client)
            Log.d(TAG, "Submitted sync task for device %s".format(remoteDevice.address))
        } else {
            Log.d(TAG, "There is already a running sync for device %s, skipping".format(remoteDevice.address))
        }
    }

    fun shutdown() {
        taskQueue.clear()
        Log.d(TAG, "Cleared task queue.")
    }

    companion object {
        private const val TAG = "ClientExecutorService"
    }
}