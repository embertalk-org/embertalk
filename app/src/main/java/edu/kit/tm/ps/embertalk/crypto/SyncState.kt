package edu.kit.tm.ps.embertalk.crypto

sealed class SyncState {
    object Synchronized: SyncState()
    object Initializing: SyncState()
    class Synchronizing(val remainingEpochs: Long): SyncState()
}