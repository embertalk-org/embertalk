package edu.kit.tm.ps.embertalk.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import edu.kit.tm.ps.embertalk.crypto.CryptoService
import edu.kit.tm.ps.embertalk.model.messages.MessageManager
import jakarta.inject.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class EmberTalkApplication : Application() {

    @Inject lateinit var messageManager: MessageManager
    @Inject lateinit var cryptoService: CryptoService

    override fun onCreate() {
        super.onCreate()
        object : Thread() {
            override fun run() {
                runBlocking {
                    cryptoService.initialize()
                }
            }
        }.start()
        applicationScope.launch {
            messageManager.deleteOld()
        }
    }

    companion object {
        val applicationScope = MainScope()
    }
}