package edu.kit.tm.ps.embertalk.app

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmberTalkApplication : Application() {

    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        applicationScope.launch {
            withContext(Dispatchers.Default) {
                while (true) {
                    container.cryptoService.initialize()
                }
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        applicationScope.cancel("OnLowMemory() called by system")
    }

    companion object {
        val applicationScope = MainScope()
    }
}