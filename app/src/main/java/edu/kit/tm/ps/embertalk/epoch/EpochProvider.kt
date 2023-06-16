package edu.kit.tm.ps.embertalk.epoch

interface EpochProvider {
    fun current(): Long
}