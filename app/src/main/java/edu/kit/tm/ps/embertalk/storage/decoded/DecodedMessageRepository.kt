package edu.kit.tm.ps.embertalk.storage.decoded

import edu.kit.tm.ps.embertalk.storage.EmberObservable
import kotlinx.coroutines.flow.Flow

interface DecodedMessageRepository : EmberObservable {
    fun all(): Flow<List<DecodedMessage>>

    suspend fun insert(message: DecodedMessage)
}