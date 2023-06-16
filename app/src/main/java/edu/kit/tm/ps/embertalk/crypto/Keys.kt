package edu.kit.tm.ps.embertalk.crypto

import android.content.SharedPreferences
import android.util.Base64
import edu.kit.tm.ps.KeyGen
import edu.kit.tm.ps.PrivateKey
import edu.kit.tm.ps.PublicKey
import edu.kit.tm.ps.embertalk.Preferences

class Keys(
    private val prefs: SharedPreferences,
) {

    fun regenerate() {
        val keyPair = KeyGen.generateKeypair()
        prefs.edit()
            .putString(Preferences.PRIVATE_KEY, privKeyString(keyPair.privateKey()))
            .putString(Preferences.PUBLIC_KEY, pubKeyString(keyPair.publicKey()))
            .apply()
    }

    fun private(): PrivateKey {
        return PrivateKey.deserialize(Base64.decode(prefs.getString(Preferences.PRIVATE_KEY, ""), Base64.URL_SAFE))
    }

    fun public(): PublicKey {
        return PublicKey.deserialize(Base64.decode(prefs.getString(Preferences.PRIVATE_KEY, ""), Base64.URL_SAFE))
    }

    fun decode(pubKey: String): PublicKey {
        return PublicKey.deserialize(Base64.decode(pubKey, Base64.URL_SAFE))
    }

    private fun privKeyString(priv: PrivateKey): String? {
        return Base64.encodeToString(priv.serialize(), Base64.URL_SAFE)
    }

    private fun pubKeyString(pub: PublicKey): String? {
        return Base64.encodeToString(pub.serialize(), Base64.URL_SAFE)
    }
}