package edu.kit.tm.ps.embertalk.crypto

import edu.kit.tm.ps.PrivateKey
import edu.kit.tm.ps.PublicKey

data class KeyPair(
    val publicKey: PublicKey,
    val privateKey: PrivateKey
) {
    fun epoch(): Long {
        return privateKey.currentEpoch()
    }
    fun clone(): KeyPair {
        return KeyPair(publicKey.clone(), privateKey.clone())
    }
    fun fastForward(epochs: Long): KeyPair {
        val private = privateKey.clone()
        val public = publicKey.clone()
        private.fastForward(epochs)
        public.fastForward(epochs)
        return KeyPair(public, private)
    }
}