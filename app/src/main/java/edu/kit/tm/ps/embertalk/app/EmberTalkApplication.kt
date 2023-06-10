package edu.kit.tm.ps.embertalk.app

import android.app.Application

class EmberTalkApplication : Application() {

    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}