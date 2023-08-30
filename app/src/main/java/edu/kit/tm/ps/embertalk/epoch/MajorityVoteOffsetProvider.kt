package edu.kit.tm.ps.embertalk.epoch

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class MajorityVoteOffsetProvider(
    private var baseEpochProvider: EpochProvider,
) : EpochProvider, ClockManager {

    private val knownClocks: MutableMap<String, Long> = ConcurrentHashMap()
    private val clockTimestamps: MutableMap<String, Instant> = ConcurrentHashMap()

    private var vote: Long = majorityVote()
    private var voteTimestamp: Instant = Instant.now()

    override fun current(): Long {
        val now = Instant.now()
        if (voteTimestamp.isAfter(now.minusSeconds(EVICTION_INTERVAL))) {
            clockTimestamps
                .filter { it.value.isBefore(now.minusSeconds(EVICTION_INTERVAL)) }
                .forEach { knownClocks.remove(it.key) }
            vote = majorityVote()
            voteTimestamp = now
        }
        return baseEpochProvider.current() + vote
    }

    override fun rememberClock(device: String, epoch: Long) {
        val offset = epoch - baseEpochProvider.current()
        knownClocks[device] = offset
        clockTimestamps[device] = Instant.now()
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
        private const val EVICTION_INTERVAL = 120L
    }
}