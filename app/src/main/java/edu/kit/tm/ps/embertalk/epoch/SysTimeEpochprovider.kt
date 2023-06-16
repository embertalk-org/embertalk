package edu.kit.tm.ps.embertalk.epoch

class SysTimeEpochprovider : EpochProvider {

    override fun current(): Long {
        return (System.currentTimeMillis() - START_EPOCH) / 120000
    }

    companion object {
        private const val START_EPOCH = 1686946000000
    }
}