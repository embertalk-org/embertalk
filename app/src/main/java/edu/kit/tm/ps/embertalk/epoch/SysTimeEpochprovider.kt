package edu.kit.tm.ps.embertalk.epoch

internal class SysTimeEpochprovider : EpochProvider {

    override fun current(): Long {
        return System.currentTimeMillis() / EPOCH_LENGTH
    }

    companion object {
        private const val EPOCH_LENGTH = 300000
    }
}