package edu.kit.tm.ps.embertalk.app

import android.app.Application
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking

class EmberTalkApplication : Application() {

    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        object : Thread() {
            override fun run() {
                runBlocking {
                    container.cryptoService.initialize()
                }
            }
        }.start()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        applicationScope.cancel("OnLowMemory() called by system")
    }

    companion object {
        val applicationScope = MainScope()
    }
}