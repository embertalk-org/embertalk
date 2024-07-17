package edu.kit.tm.ps.embertalk.epoch

import java.time.Instant

internal class SysTimeEpochprovider : EpochProvider {

    override fun current(): Long {
        return System.currentTimeMillis() / EpochProvider.EPOCH_LENGTH
    }

    override fun fromInstant(instant: Instant): Long {
        return instant.toEpochMilli() / EpochProvider.EPOCH_LENGTH
    }
}