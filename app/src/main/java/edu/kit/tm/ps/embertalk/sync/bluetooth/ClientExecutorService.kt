package edu.kit.tm.ps.embertalk.sync.bluetooth

import android.bluetooth.BluetoothDevice
import android.util.Log
import edu.kit.tm.ps.embertalk.sync.Synchronizer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class ClientExecutorService {

    private val taskQueue: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
    private val service = Executors.newCachedThreadPool()

    fun enqueue(remoteDevice: BluetoothDevice, uuid: UUID, synchronizer: Synchronizer, onFinishAction: () -> Unit) {
        if (!taskQueue.contains(uuid)) {
            taskQueue.add(uuid)
            val client = BluetoothClassicClient(
                remoteDevice,
                synchronizer,
                uuid,
                { taskQueue.remove(uuid) },
                { onFinishAction.invoke() },
            )
            service.submit(client)
            Log.d(TAG, "Submitted sync task for device %s".format(uuid))
        } else {
            Log.d(TAG, "There is already a running sync for device %s, skipping".format(uuid))
        }
    }

    fun shutdown() {
        service.shutdown()
    }

    companion object {
        private const val TAG = "ClientExecutorService"
    }
}