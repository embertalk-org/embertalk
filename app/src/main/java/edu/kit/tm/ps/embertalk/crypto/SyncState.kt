package edu.kit.tm.ps.embertalk.crypto

sealed class SyncState {
    class Synchronized(val currentEpoch: Long): SyncState()
    object Initializing: SyncState()
    class Synchronizing(val remainingEpochs: Long): SyncState()
}