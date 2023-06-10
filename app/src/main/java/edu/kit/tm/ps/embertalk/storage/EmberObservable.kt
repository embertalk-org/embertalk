package edu.kit.tm.ps.embertalk.storage



interface EmberObserver {
    fun notifyOfChange()
}

interface EmberObservable {
    fun register(observer: EmberObserver)
    fun notifyObservers()
}