package edu.kit.tm.ps.embertalk.ui.qr_code

import androidx.lifecycle.ViewModel
import edu.kit.tm.ps.embertalk.crypto.CryptoService
import java.io.IOException
import java.util.UUID

class QrCodeViewModel(
    private val cryptoService: CryptoService,
) : ViewModel() {

    suspend fun putKey(userId: UUID): Int {
        return try {
            cryptoService.emberKeydClient().putKey(userId)
        } catch (e: IOException) {
            -1
        }
    }

    fun isMyKey(pubKey: String): Boolean {
        return cryptoService.isMyKey(pubKey)
    }
}