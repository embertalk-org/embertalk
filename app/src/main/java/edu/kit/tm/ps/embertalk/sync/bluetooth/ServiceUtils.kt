package edu.kit.tm.ps.embertalk.sync.bluetooth

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import java.util.UUID


private fun makeCharacteristic(uuid: UUID): BluetoothGattCharacteristic {
    return BluetoothGattCharacteristic(
        uuid,
        BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
        BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
    )
}

enum class Requests(val uuid: UUID) {
    CLOCKS(UUID.fromString("32e9a691-e75f-4340-ba98-bf4e34a10b0c")),
    MESSAGE(UUID.fromString("a6fa6de0-35c7-466f-9495-5a8035ce5feb")),
    MESSAGES_FINISHED(UUID.fromString("a6fa6de0-473a-e75f-9495-587082312c76"))
}

object ServiceUtils {

    val SERVICE_UUID = UUID.fromString("c32ebc7e-0507-4506-9111-673f5811fbbb")!!

    fun matchesService(uuid: UUID): Boolean {
        return SERVICE_UUID == uuid
    }

    fun service(): BluetoothGattService {
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        for (request in Requests.entries) {
            service.addCharacteristic(makeCharacteristic(request.uuid))
        }
        return service
    }
}