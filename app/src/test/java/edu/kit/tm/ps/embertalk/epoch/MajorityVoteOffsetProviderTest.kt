package edu.kit.tm.ps.embertalk.epoch

import org.junit.Assert
import org.junit.Before
import org.junit.Test

private class IncreasingProvider : EpochProvider {
    private var cnt = 1000L
    override fun current(): Long = cnt
    fun bump() {
        cnt++
    }
}

internal class MajorityVoteOffsetProviderTest {

    private lateinit var baseProvider: IncreasingProvider
    private lateinit var offsetProvider: MajorityVoteOffsetProvider

    @Before
    fun prepare() {
        baseProvider = IncreasingProvider()
        offsetProvider = MajorityVoteOffsetProvider(baseProvider)
    }

    @Test
    fun testLocalEpochOnly() {
        Assert.assertEquals(1000, offsetProvider.current())
        baseProvider.bump()
        Assert.assertEquals(1001, offsetProvider.current())
        baseProvider.bump()
        Assert.assertEquals(1002, offsetProvider.current())
        baseProvider.bump()
        Assert.assertEquals(1003, offsetProvider.current())
    }

    @Test
    fun testForeignClocks() {
        offsetProvider.rememberClock("test1", 100)
        offsetProvider.rememberClock("test2", 100)
        offsetProvider.rememberClock("test3", 100)
        Assert.assertEquals(100, offsetProvider.current())
    }

    @Test
    fun oneForeignClock() {
        offsetProvider.rememberClock("test1", 99)
        Assert.assertEquals(1000, offsetProvider.current())
    }

    @Test
    fun differentForeignClocks() {
        offsetProvider.rememberClock("test1", 100)
        offsetProvider.rememberClock("test2", 101)
        offsetProvider.rememberClock("test3", 102)
        Assert.assertTrue(listOf(100L, 101L, 102L).contains(offsetProvider.current()))
    }
}