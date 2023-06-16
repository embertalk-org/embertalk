package edu.kit.tm.ps.embertalk.epoch

class SysTimeEpochprovider : EpochProvider {

    override fun current(): Long {
        return System.currentTimeMillis() / 60000
    }
}