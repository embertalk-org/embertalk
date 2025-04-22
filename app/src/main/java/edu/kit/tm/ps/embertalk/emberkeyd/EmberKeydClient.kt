package edu.kit.tm.ps.embertalk.emberkeyd

import android.util.Base64
import android.util.Log
import edu.kit.tm.ps.embertalk.crypto.KeyPair
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

const val AUTH_HEADER = "X-Ember-Secret"
const val AUTH_VAL = "eithu4ae7uzaer5dahfeiwi5Mohy2sah1IBeinguu5afahng8u"


class EmberKeydClient(
    private val keyServerUrl: String,
    private val keys: () -> KeyPair
) {

    private val mediaType = "application/json".toMediaType()
    private val client = OkHttpClient()

    suspend fun putKey(userId: UUID): Int {
        val pubKeyJson = JSONObject()
        pubKeyJson.put("pubkey", encodeAsJson(keys.invoke().publicKey.serialize()))
        val chalResp = post("$keyServerUrl/challenge", pubKeyJson.toString())
        if (chalResp.code != 200) {
            Log.d(TAG, "Got status code ${chalResp.code}")
            chalResp.body?.let { Log.d(TAG, it.string()) }
            return chalResp.code
        }
        val chalBody = JSONObject(chalResp.body!!.string())
        val challengeJson = copyStateAndNonceFrom(chalBody)
        challengeJson.put("response", encodeAsJson(keys.invoke().privateKey.decrypt(decodeFromJson(chalBody.getJSONArray("challenge")))))
        challengeJson.put("user_id", userId.toString())
        val finalResp = post("$keyServerUrl/response", challengeJson.toString())
        if (finalResp.code != 201) {
            Log.d(TAG, "Got status code ${finalResp.code}")
            finalResp.body?.let { Log.d(TAG, it.string()) }
        }
        return finalResp.code
    }

    private fun copyStateAndNonceFrom(challenge: JSONObject): JSONObject {
        val state = challenge.getJSONArray("state")
        val nonce = challenge.getJSONArray("nonce")
        return JSONObject().apply {
            this.put("state", state)
            this.put("nonce", nonce)
        }
    }

    suspend fun downloadKey(userId: UUID): String? {
        val resp = get("$keyServerUrl/key/$userId")
        Log.d(TAG, "Got status code ${resp.code}")
        return if (resp.code != 200) {
            null
        } else {
            val json = JSONObject(resp.body!!.string()).getJSONArray("pubkey")
            Base64.encodeToString(decodeFromJson(json), Base64.URL_SAFE)
        }
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun post(url: String, json: String): Response {
        val body: RequestBody = json.toRequestBody(mediaType)
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .header(AUTH_HEADER, AUTH_VAL)
            .build()
        return client.newCall(request).execute()

    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun get(url: String): Response {
        val request: Request = Request.Builder()
            .url(url)
            .get()
            .header(AUTH_HEADER, AUTH_VAL)
            .build()
        return client.newCall(request).execute()

    }

    companion object {
        private const val TAG = "EmberKeydClient"

        private fun encodeAsJson(bytes: ByteArray): JSONArray {
            val json = JSONArray()
            for (byte in bytes) {
                json.put(byte.toUByte().toInt())
            }
            return json
        }

        private fun decodeFromJson(array: JSONArray): ByteArray {
            val bytes = ByteArray(array.length())
            for (i in 0 until array.length()) {
                bytes[i] = array.getInt(i).toUByte().toByte()
            }
            return bytes
        }
    }
}