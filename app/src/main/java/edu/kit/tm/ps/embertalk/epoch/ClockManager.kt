package edu.kit.tm.ps.embertalk.epoch

interface ClockManager {
    fun rememberClock(device: String, epoch: Long)
}