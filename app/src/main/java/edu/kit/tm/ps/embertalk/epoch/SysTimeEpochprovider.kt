package edu.kit.tm.ps.embertalk.epoch

internal class SysTimeEpochprovider : EpochProvider {

    override fun current(): Long {
        return (System.currentTimeMillis() - START_EPOCH) / EPOCH_LENGTH
    }

    companion object {
        private const val START_EPOCH = 1686946000000
        private const val EPOCH_LENGTH = 60 * 1000
    }
}