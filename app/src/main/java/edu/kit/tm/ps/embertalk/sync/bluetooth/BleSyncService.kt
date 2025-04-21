package edu.kit.tm.ps.embertalk.sync.bluetooth

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import edu.kit.tm.ps.embertalk.Preferences
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.model.messages.MessageManager
import edu.kit.tm.ps.embertalk.notification.Notification
import jakarta.inject.Inject
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap


@SuppressLint("MissingPermission")
class BleSyncService @Inject constructor(
    private val messageManager: MessageManager,
) : Service() {
    private val devicesLastSynced: ConcurrentHashMap<String, Instant> = ConcurrentHashMap()

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothLeAdvertiser: BluetoothLeAdvertiser
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var bleServer: BluetoothGattServer
    private lateinit var scanCallback: ScanCallback
    private var started = false
    private lateinit var preferences: SharedPreferences

    init {
        Log.i("SyncService", "Started Sync Service")
    }

    override fun onCreate() {
        super.onCreate()
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        startForeground(1, Notification.build(
            this,
            "EmberTalk Bluetooth Sync",
            "Service is running in the background...",
            NotificationManager.IMPORTANCE_NONE)
        )
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

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "BLE advertise started")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.e(TAG, "BLE advertise failed to start: error $errorCode")
        }
    }

    private fun scanCallback(): ScanCallback {
        return object : ScanCallback() {

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.e(TAG, "BLE scan failed to start: error $errorCode")
            }

            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)

                if (result.scanRecord == null || result.scanRecord!!.serviceUuids == null) {
                    return
                }

                for (uuid in result.scanRecord!!.serviceUuids) {
                    if (!ServiceUtils.matchesService(uuid.uuid)) {
                        continue
                    }
                    val remoteDevice = result.device
                    if (devicesLastSynced[remoteDevice.address] != null
                        && Instant.now()
                            .isBefore(devicesLastSynced[remoteDevice.address]!!
                                .plusSeconds(preferences.getLong(Preferences.SYNC_INTERVAL, 5)))
                        ) {
                        continue
                    } else {
                        Log.i(TAG, "Found device running EmberTalk with address ${remoteDevice.address}. Syncing.")
                        devicesLastSynced[remoteDevice.address] = Instant.now()
                    }
                    val clientCallback = ClientCallback(remoteDevice.address, messageManager)
                    val gatt = remoteDevice.connectGatt(this@BleSyncService, false, clientCallback, BluetoothDevice.TRANSPORT_LE)
                }
            }
        }
    }

    private fun startBluetoothLeDiscovery() {
        Log.i(TAG, "Starting advertise with service uuid %s".format(ServiceUtils.SERVICE_UUID))
        bluetoothLeAdvertiser.startAdvertising(
            BleSettings.ADVERTISE_SETTINGS,
            BleSettings.buildAdvertiseData(ServiceUtils.SERVICE_UUID),
            advertiseCallback)

        scanCallback = scanCallback()
        bluetoothLeScanner.startScan(
            BleSettings.SCAN_FILTERS,
            BleSettings.SCAN_SETTINGS,
            scanCallback)
    }

    private fun stopBluetoothLeDiscovery() {
        if (!bluetoothManager.adapter.isEnabled) {
            return
        }
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
        bluetoothLeScanner.stopScan(scanCallback)
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

        bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        bluetoothLeAdvertiser = bluetoothManager.adapter.bluetoothLeAdvertiser
        bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner
        startBluetoothLeDiscovery()

        started = true

        var gattServer: BluetoothGattServer? = null
        gattServer = bluetoothManager.openGattServer(this, ServerCallback(messageManager) { gattServer!! })
        bleServer = gattServer

        gattServer.addService(ServiceUtils.service())

        Log.d(TAG, "Started")
        Toast.makeText(this, R.string.bluetooth_sync_started, Toast.LENGTH_LONG).show()
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        started = false

        stopBluetoothLeDiscovery()
        bleServer.close()

        Toast.makeText(this, R.string.bluetooth_sync_stopped, Toast.LENGTH_LONG).show()
        Log.d(TAG, "Stopped")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "BleSyncService"

        fun canStart(context: Context): CanStartResult {
            val packageManager = context.packageManager
            val bluetoothAdapter = getBluetoothAdapter(context)
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) || !packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                return CanStartResult.BLUETOOTH_OR_BLE_UNSUPPORTED
            } else if (!bluetoothAdapter.isEnabled) {
                return CanStartResult.BLUETOOTH_OFF
            } else if (!bluetoothAdapter.isMultipleAdvertisementSupported) {
                return CanStartResult.BLUETOOTH_OR_BLE_UNSUPPORTED
            }

            return CanStartResult.CAN_START
        }

        fun startOrPromptBluetooth(context: Context) {
            when (canStart(context)) {
                CanStartResult.CAN_START -> {
                    Log.d(TAG, "Starting BLE sync service")
                    context.startForegroundService(Intent(context, BleSyncService::class.java))
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

        fun stop(context: Context) {
            context.stopService(Intent(context, BleSyncService::class.java))
        }

        private fun getBluetoothAdapter(context: Context): BluetoothAdapter {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            return bluetoothManager.adapter
        }
    }
}
