package edu.kit.tm.ps.embertalk.sync.bluetooth

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.app.EmberTalkApplication
import edu.kit.tm.ps.embertalk.notification.PersistentNotification
import edu.kit.tm.ps.embertalk.sync.Synchronizer
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap


@SuppressLint("MissingPermission")
class BluetoothSyncService : Service() {

    private lateinit var synchronizer: Synchronizer
    private lateinit var serviceUuidAndAddress: UUID
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeAdvertiser: BluetoothLeAdvertiser
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var bluetoothClassicServer: Thread
    private var started = false
    private val devicesLastSynced: ConcurrentHashMap<UUID, Instant> = ConcurrentHashMap()
    private val clientExecutorService: ClientExecutorService = ClientExecutorService()

    init {
        Log.i("SyncService", "Started Sync Service")
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(1, PersistentNotification
            .build(this, "EmberTalk Bluetooth Sync", "Service is running in the background..."))
    }

    override fun onBind(intent: Intent): IBinder? {
        TODO("What do we actually need to do here?")
    }

    enum class CanStartResult {
        CAN_START,
        BLUETOOTH_OR_BLE_UNSUPPORTED,
        BLUETOOTH_OFF,
        BLUETOOTH_ADDRESS_UNAVAILABLE
    }

    private fun startBluetoothLeDiscovery(startId: Int) {
        Log.i(TAG, "Starting advertise with service uuid %s".format(serviceUuidAndAddress))
        bluetoothLeAdvertiser.startAdvertising(
            BleSettings.ADVERTISE_SETTINGS,
            BleSettings.buildAdvertiseData(serviceUuidAndAddress),
            object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                    super.onStartSuccess(settingsInEffect)
                    Log.d(TAG, "BLE advertise started")
                }

                override fun onStartFailure(errorCode: Int) {
                    super.onStartFailure(errorCode)
                    Log.e(TAG, "BLE advertise failed to start: error $errorCode")
                    stopSelf(startId)
                    //TODO can we just restart here again?
                }
            })

        bluetoothLeScanner.startScan(
            BleSettings.SCAN_FILTERS,
            BleSettings.SCAN_SETTINGS,
            object : ScanCallback() {
                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    Log.e(TAG, "BLE scan failed to start: error $errorCode")
                    stopSelf(startId)
                }

                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    super.onScanResult(callbackType, result)

                    if (result.scanRecord == null || result.scanRecord!!.serviceUuids == null) {
                        return
                    }

                    for (uuid in result.scanRecord!!.serviceUuids) {
                        if (!ServiceUtils.matchesService(uuid.uuid)) {
                            continue
                        }
                        if (devicesLastSynced[serviceUuidAndAddress] != null &&
                            Instant.now().isBefore(devicesLastSynced[serviceUuidAndAddress]!!.plusSeconds(60))) {
                            continue
                        }

                        val remoteDeviceMacAddress = ServiceUtils.fromParcelUuid(uuid)
                        val remoteDevice = bluetoothAdapter.getRemoteDevice(remoteDeviceMacAddress)
                        clientExecutorService.enqueue(remoteDevice, uuid.uuid, synchronizer) {
                            devicesLastSynced[serviceUuidAndAddress] = Instant.now()
                        }
                    }
                }
            })
    }

    private fun stopBluetoothLeDiscovery() {
        if (!bluetoothAdapter.isEnabled) {
            return
        }

        bluetoothLeAdvertiser.stopAdvertising(object : AdvertiseCallback() {
            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)
                Log.e(TAG, "BLE advertise failed to stop: error $errorCode")
            }
        })

        bluetoothLeScanner.stopScan(object : ScanCallback() {
            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.e(TAG, "BLE scan failed to stop: error $errorCode")
            }
        })
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (canStart(this) != CanStartResult.CAN_START) {
            Log.e(TAG, "Trying to start the service even though Bluetooth is off or BLE is unsupported")
            stopSelf(startId)
            return START_NOT_STICKY
        }

        if (started) {
            Log.d(TAG, "Started again")
            return START_REDELIVER_INTENT
        }
        val app = this.application as EmberTalkApplication
        synchronizer = Synchronizer(app.container.messageManager)

        bluetoothAdapter = getBluetoothAdapter(this)

        // First half identifies that the advertisement is for us.
        // Second half is the MAC address of this device's Bluetooth adapter so that clients know how to connect to it.
        // These are not listed separately in the advertisement because a UUID is 16 bytes and ads are limited to 31 bytes.
        val macAddress = getBluetoothAdapterAddress(this)
        if (macAddress == null) {
            Log.e(TAG, "Unable to get this device's Bluetooth MAC address")
            stopSelf(startId)
            return START_NOT_STICKY
        }
        val uuid = ServiceUtils.toUuid(macAddress)
        serviceUuidAndAddress = uuid

        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        startBluetoothLeDiscovery(startId)

        started = true
        bluetoothClassicServer = BluetoothClassicServer(uuid, synchronizer, bluetoothAdapter)
        bluetoothClassicServer.start()

        Log.d(TAG, "Started")
        Toast.makeText(this, R.string.bluetooth_sync_started, Toast.LENGTH_LONG).show()
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        started = false

        stopBluetoothLeDiscovery()
        bluetoothClassicServer.interrupt()

        Toast.makeText(this, R.string.bluetooth_sync_stopped, Toast.LENGTH_LONG).show()
        Log.d(TAG, "Stopped")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "BluetoothSyncService"

        fun canStart(context: Context): CanStartResult {
            val packageManager = context.packageManager
            val bluetoothAdapter = getBluetoothAdapter(context)
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) || !packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                return CanStartResult.BLUETOOTH_OR_BLE_UNSUPPORTED
            } else if (!bluetoothAdapter.isEnabled) {
                return CanStartResult.BLUETOOTH_OFF
            } else if (!bluetoothAdapter.isMultipleAdvertisementSupported) {
                return CanStartResult.BLUETOOTH_OR_BLE_UNSUPPORTED
            } else if (getBluetoothAdapterAddress(context) == null) {
                return CanStartResult.BLUETOOTH_ADDRESS_UNAVAILABLE
            }

            return CanStartResult.CAN_START
        }

        fun startOrPromptBluetooth(context: Context) {
            when (canStart(context)) {
                CanStartResult.CAN_START -> {
                    Log.d(TAG, "Starting BLE sync service")
                    context.startForegroundService(Intent(context, BluetoothSyncService::class.java))
                }
                CanStartResult.BLUETOOTH_OR_BLE_UNSUPPORTED -> {
                    Log.d(TAG, "BLE not supported, not starting BLE sync service")
                    Toast.makeText(context, R.string.bluetooth_not_supported, Toast.LENGTH_LONG).show()
                }
                CanStartResult.BLUETOOTH_OFF -> {
                    Log.d(TAG, "BLE supported but Bluetooth is off; will prompt for Bluetooth and start once it's on")
                    Toast.makeText(context, R.string.bluetooth_ask_enable, Toast.LENGTH_LONG).show()
                    context.startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                }
                CanStartResult.BLUETOOTH_ADDRESS_UNAVAILABLE -> {
                    Log.d(TAG, "BLE supported but MAC address is unavailable; will prompt for address and start once it's available")
                    Toast.makeText(context, R.string.bluetooth_ask_address, Toast.LENGTH_LONG).show()
                }
            }
        }

        private fun getBluetoothAdapter(context: Context): BluetoothAdapter {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            return bluetoothManager.adapter
        }

        private fun getBluetoothAdapterAddress(context: Context): String? {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getString("mac", null)?.uppercase()
        }
    }
}
