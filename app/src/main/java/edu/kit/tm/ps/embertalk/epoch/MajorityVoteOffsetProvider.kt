package edu.kit.tm.ps.embertalk.epoch

import java.util.concurrent.ConcurrentHashMap

class MajorityVoteOffsetProvider(
    private var baseEpochProvider: EpochProvider,
) : EpochProvider, ClockManager {

    private val knownClocks: MutableMap<String, Long> = ConcurrentHashMap()

    override fun current(): Long {
        return baseEpochProvider.current() + majorityVote()
    }

    override fun rememberClock(device: String, epoch: Long) {
        knownClocks[device] = epoch - baseEpochProvider.current()
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
}