package edu.kit.tm.ps.embertalk.epoch

import java.time.Instant

interface EpochProvider {
    fun current(): Long
    fun fromInstant(instant: Instant): Long

    companion object {
        public const val EPOCH_LENGTH = 300000
    }
}