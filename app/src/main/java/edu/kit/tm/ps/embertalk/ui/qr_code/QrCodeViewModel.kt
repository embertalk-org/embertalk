package edu.kit.tm.ps.embertalk.ui.qr_code

import androidx.lifecycle.ViewModel
import edu.kit.tm.ps.embertalk.crypto.CryptoService
import java.io.IOException

class QrCodeViewModel(
    private val cryptoService: CryptoService,
) : ViewModel() {

    suspend fun putKey(pubKey: String): Int {
        return try {
            cryptoService.emberKeydClient().putKey(pubKey)
        } catch (e: IOException) {
            -1
        }
    }

    fun isMyKey(pubKey: String): Boolean {
        return cryptoService.isMyKey(pubKey)
    }
}