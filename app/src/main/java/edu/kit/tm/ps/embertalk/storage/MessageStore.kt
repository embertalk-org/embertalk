package edu.kit.tm.ps.embertalk.storage

import java.util.concurrent.ConcurrentHashMap

class MessageStore {

    private val messages = ConcurrentHashMap<Int, Message>()

    fun messages(): Set<Message> {
        return messages.values.toSet()
    }

    fun hashes(): Set<Int> {
        return messages.keys.toSet()
    }

    fun save(message: Message) {
        messages[message.hashCode()] = message
    }

    fun size(): Int {
        return messages.size
    }

    fun messagesExcept(theirHashes: Set<Int>): Set<Message> {
        val messagesTheyDontHave = messages.filter { !theirHashes.contains(it.key) }
        return messagesTheyDontHave.values.toSet()
    }
}