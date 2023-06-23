package edu.kit.tm.ps.embertalk.epoch

import android.content.SharedPreferences
import edu.kit.tm.ps.embertalk.Preferences
import java.util.concurrent.ConcurrentHashMap

class MajorityVoteOffsetProvider(
    private val prefs: SharedPreferences,
) : EpochProvider, ClockManager {

    private val knownClocks: MutableMap<String, Long> = ConcurrentHashMap()
    private val systimeEpochProvider = SysTimeEpochprovider()
    private var lastLocalEpoch = prefs.getLong(Preferences.STORED_EPOCH, systimeEpochProvider.current())

    override fun current(): Long {
        val currentLocalEpoch = systimeEpochProvider.current()
        val epochsSinceLast = currentLocalEpoch - lastLocalEpoch
        lastLocalEpoch = currentLocalEpoch
        prefs.edit().putLong(Preferences.STORED_EPOCH, lastLocalEpoch).apply()
        return majorityVote() + epochsSinceLast
    }

    override fun rememberClock(device: String, epoch: Long) {
        knownClocks[device] = epoch
    }

    private fun majorityVote(): Long {
        val chosenClocks = knownClocks.getRandomElements(3)
        return if (chosenClocks[0] == chosenClocks[1] || chosenClocks[0] == chosenClocks[2]) {
            chosenClocks[0]
        } else if (chosenClocks[1] == chosenClocks[2]) {
            chosenClocks[1]
        } else {
            chosenClocks.shuffled()[0]
        }
    }

    private fun <K, V> Map<K, V>.getRandomElements(count: Int): List<V> {
        val entries = this.entries.toList()
        return entries
            .shuffled()
            .take(count)
            .map { it.value }
    }
}