package edu.kit.tm.ps.embertalk.model



interface EmberObserver {
    fun notifyOfChange()
}

interface EmberObservable {
    fun register(observer: EmberObserver)
    fun notifyObservers()
}