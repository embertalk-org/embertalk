package edu.kit.tm.ps.embertalk.sync.bluetooth

import android.bluetooth.BluetoothDevice
import android.util.Log
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class ClientExecutorService {

    private val taskQueue: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
    private val service = Executors.newCachedThreadPool()

    fun enqueue(remoteDevice: BluetoothDevice, uuid: UUID, onCompletionAction: () -> Unit) {
        if (!taskQueue.contains(uuid)) {
            taskQueue.add(uuid)
            val client = BluetoothClassicClient(remoteDevice, uuid) {
                onCompletionAction.invoke()
                taskQueue.remove(uuid)
            }
            service.submit(client)
            Log.d(TAG, "Submitted sync task for device %s".format(uuid))
        } else {
            Log.d(TAG, "There is already a running sync for device %s, skipping".format(uuid))
        }
    }

    fun shutdownNow() {
        service.shutdownNow()
    }

    companion object {
        private const val TAG = "ClientExecutorService"
    }
}