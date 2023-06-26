package edu.kit.tm.ps.embertalk.epoch

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

class MajorityVoteOffsetProvider(
    private var baseEpochProvider: EpochProvider,
) : EpochProvider, ClockManager {

    private val knownClocks: MutableMap<String, Long> = ConcurrentHashMap()

    override fun current(): Long {
        return baseEpochProvider.current() + majorityVote()
    }

    override fun rememberClock(device: String, epoch: Long) {
        val offset = epoch - baseEpochProvider.current()
        knownClocks[device] = offset
        Log.d(TAG, "Stored clock of device %s with offset %s".format(device, offset))
    }

    private fun majorityVote(): Long {
        val chosenClocks = knownClocks.getRandomElements(3)
        while (chosenClocks.size < 3) {
            chosenClocks.add(0)
        }
        return if (chosenClocks[0] == chosenClocks[1] || chosenClocks[0] == chosenClocks[2]) {
            chosenClocks[0]
        } else if (chosenClocks[1] == chosenClocks[2]) {
            chosenClocks[1]
        } else {
            Log.d(TAG, "Could not find matching clocks, choosing random clock.")
            chosenClocks.shuffled()[0]
        }
    }

    private fun <K, V> Map<K, V>.getRandomElements(count: Int): MutableList<V> {
        val entries = this.entries.toList()
        return entries
            .shuffled()
            .take(count)
            .map { it.value }
            .toMutableList()
    }

    companion object {
        private const val TAG = "MajorityVoteOffsetProvider"
    }
}