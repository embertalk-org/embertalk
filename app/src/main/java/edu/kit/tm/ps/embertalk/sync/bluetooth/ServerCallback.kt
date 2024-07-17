package edu.kit.tm.ps.embertalk.sync.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.util.Log
import edu.kit.tm.ps.embertalk.epoch.ClockManager
import edu.kit.tm.ps.embertalk.epoch.EpochProvider
import edu.kit.tm.ps.embertalk.model.messages.MessageManager
import edu.kit.tm.ps.embertalk.sync.Protocol
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

@SuppressLint("MissingPermission")
class ServerCallback(
    private val messageManager: MessageManager,
    private val epochProvider: EpochProvider,
    private val clockManager: ClockManager,
    private val bluetoothGattServer: () -> BluetoothGattServer
    ) : BluetoothGattServerCallback() {

    private lateinit var hashBuffer: ByteBuffer
    private var receiveBuffer = ByteArrayOutputStream()

    override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.i(ClientCallback.TAG, "Device ${device.address} connected. Status: $status.")
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.i(ClientCallback.TAG, "Device is disconnected.")
            }
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.i(ClientCallback.TAG, "Device is connected.")
            }
        }
        Log.d(TAG, "Device ${device.address} connected. Status: $status, newState: $newState")
    }

    override fun onCharacteristicReadRequest(
        device: BluetoothDevice,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic
    ) {
        val reply = when (characteristic.uuid) {
            Requests.CLOCKS.uuid -> {
                Log.d(TAG, "Handling CLOCKS read.")
                Protocol.fromClock(epochProvider.current())
            }
            Requests.HASHES_SIZE.uuid -> {
                Log.d(TAG, "Handling HASHES_SIZE read.")
                hashBuffer = ByteBuffer.wrap(Protocol.fromHashes(runBlocking { messageManager.hashes().first() }))
                ByteBuffer.allocate(4).putInt(hashBuffer.remaining()).array()
            }
            Requests.HASH.uuid -> {
                if (hashBuffer.hasRemaining()) {
                    hashBuffer.nextChunk()
                } else {
                    null
                }
            }
            else -> {
                Log.d(TAG, "Aborting INVALID read.")
                null
            }
        }
        if (reply != null) {
            bluetoothGattServer.invoke().sendResponse(
                device,
                requestId,
                0,
                0,
                reply
            )
        } else {
            bluetoothGattServer.invoke().sendResponse(
                device,
                requestId,
                404,
                0,
                ByteArray(0)
            )
        }
    }

    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        when (characteristic.uuid) {
            Requests.MESSAGE.uuid -> {
                receiveBuffer.write(value!!)
            }
            Requests.MESSAGES_FINISHED.uuid -> {
                val received = receiveBuffer.toByteArray()
                val messages = Protocol.toMessages(received)
                Log.d(TAG, "Received ${received.size} bytes and ${messages.size} messages. Enrolling.")
                messages.forEach {
                    runBlocking { messageManager.handle(it) }
                }
                receiveBuffer = ByteArrayOutputStream()
                Log.d(TAG, "Enrolled ${messages.size} messages.")
            }
        }
        bluetoothGattServer.invoke().sendResponse(
            device,
            requestId,
            200,
            0,
            ByteBuffer.allocate(0).array()
        )
    }

    companion object {
        const val TAG = "GattServerCallback"
    }
}