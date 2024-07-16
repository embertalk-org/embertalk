package edu.kit.tm.ps.embertalk.sync.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import edu.kit.tm.ps.embertalk.epoch.ClockManager
import edu.kit.tm.ps.embertalk.model.messages.MessageManager
import edu.kit.tm.ps.embertalk.sync.Protocol
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.nio.ByteBuffer
import java.util.UUID

class ClientCallback(
    private val remoteAddress: String,
    private val messageManager: MessageManager,
    private val clockManager: ClockManager
) : BluetoothGattCallback() {

    private lateinit var sendBuffer: ByteBuffer

    lateinit var hashes: Set<Int>

    private fun nextBufferChunk(): ByteArray {
        val nextChunkSize = sendBuffer.remaining().coerceAtMost(20)
        val result = ByteArray(nextChunkSize)
        sendBuffer.get(result, 0, nextChunkSize)
        return result
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.i(TAG, "Connected to server $remoteAddress. Status: $status.")
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d(TAG, "Not connected to Server. Connecting.")
                gatt.connect()
            }
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d(TAG, "Connected to Server. Discovering Services.")
                gatt.discoverServices()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        Log.d(TAG, "Services Discovered. Reading Clock.")
        gatt.readCharacteristic(gatt.characteristic(Requests.CLOCKS.uuid))
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        when(characteristic.uuid) {
            Requests.CLOCKS.uuid -> {
                val clock = Protocol.toClock(value)
                clockManager.rememberClock(remoteAddress, clock)
                sendBuffer = ByteBuffer.wrap(Protocol.fromMessages(
                    runBlocking { messageManager.allEncryptedExcept(setOf()).first() }
                ))
                Log.d(TAG, "Received Clock. Sending ${sendBuffer.remaining()} bytes.")
                if (sendBuffer.hasRemaining()) {
                    gatt.writeCharacteristic(gatt.characteristic(Requests.MESSAGE.uuid), nextBufferChunk(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        when (characteristic.uuid) {
            Requests.MESSAGE.uuid -> {
                if (sendBuffer.hasRemaining()) {
                    gatt.writeCharacteristic(gatt.characteristic(Requests.MESSAGE.uuid), nextBufferChunk(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                } else {
                    Log.d(TAG, "No more chunks. Sending finish mark.")
                    gatt.writeCharacteristic(gatt.characteristic(Requests.MESSAGES_FINISHED.uuid), ByteArray(0), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                }
            }
            Requests.MESSAGES_FINISHED.uuid -> {
                Log.d(TAG, "Finishing.")
                gatt.disconnect()
                gatt.close()
            }
        }
    }

    companion object {
        const val TAG = "GattClientCallback"
    }

    private fun BluetoothGatt.characteristic(uuid: UUID): BluetoothGattCharacteristic {
        return this.getService(ServiceUtils.SERVICE_UUID).getCharacteristic(uuid)
    }
}