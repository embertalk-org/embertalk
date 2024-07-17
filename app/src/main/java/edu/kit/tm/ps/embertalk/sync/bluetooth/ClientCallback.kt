package edu.kit.tm.ps.embertalk.sync.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import edu.kit.tm.ps.embertalk.model.messages.MessageManager
import edu.kit.tm.ps.embertalk.sync.Protocol
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.nio.ByteBuffer

class ClientCallback(
    private val remoteAddress: String,
    private val messageManager: MessageManager
) : BluetoothGattCallback() {

    private lateinit var sendBuffer: ByteBuffer

    private lateinit var hashBuffer: ByteBuffer

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
                //Coded PHY provides the longest range, but less data rate
                gatt.setPreferredPhy(
                    BluetoothDevice.PHY_LE_CODED,
                    BluetoothDevice.PHY_LE_CODED,
                    BluetoothDevice.PHY_OPTION_S8
                )
                gatt.discoverServices()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        Log.d(TAG, "Services Discovered. Reading HASHES_SIZE.")
        gatt.readCharacteristic(gatt.characteristic(Requests.HASHES_SIZE.uuid))
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
            Requests.HASHES_SIZE.uuid -> {
                hashBuffer = ByteBuffer.allocate(ByteBuffer.wrap(value).getInt())
                if (hashBuffer.hasRemaining()) {
                    gatt.readCharacteristic(gatt.characteristic(Requests.HASH.uuid))
                }
            }
            Requests.HASH.uuid -> {
                if (hashBuffer.hasRemaining()) {
                    hashBuffer.put(value)
                    gatt.readCharacteristic(gatt.characteristic(Requests.HASH.uuid))
                } else {
                    val hashes = Protocol.toHashes(hashBuffer.array())
                    sendBuffer = ByteBuffer.wrap(Protocol.fromMessages(
                        runBlocking { messageManager.allEncryptedExcept(hashes).first() }
                    ))
                    Log.d(TAG, "Received all hashes. Sending ${sendBuffer.remaining()} bytes.")
                    if (sendBuffer.hasRemaining()) {
                        gatt.writeCharacteristic(gatt.characteristic(Requests.MESSAGE.uuid), sendBuffer.nextChunk(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                    }
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
                    gatt.writeCharacteristic(gatt.characteristic(Requests.MESSAGE.uuid), sendBuffer.nextChunk(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
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
}