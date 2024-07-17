package edu.kit.tm.ps.embertalk.epoch

import java.time.Instant

internal class SysTimeEpochprovider : EpochProvider {

    override fun current(): Long {
        return System.currentTimeMillis() / EPOCH_LENGTH
    }

    override fun fromInstant(instant: Instant): Long {
        return instant.toEpochMilli() / EPOCH_LENGTH
    }

    companion object {
        private const val EPOCH_LENGTH = 300000
    }
}