package com.littleliuyao

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ConnectedCardPuller(
    private val endpoint: String,
) {
    suspend fun pull(intention: String, persist: Boolean): Result<List<PulledCard>> = withContext(Dispatchers.IO) {
        if (endpoint.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Connected pulls are not configured yet. Your cards were pulled privately on this device."),
            )
        }

        runCatching {
            val payload = JSONObject()
                .put("intention", intention)
                .put("persist", persist)
                .toString()

            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 6_000
                readTimeout = 6_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }

            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }

            if (connection.responseCode !in 200..299) {
                error("The connected pull did not complete. Your cards were pulled privately instead.")
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            parseCards(response)
        }
    }

    private fun parseCards(response: String): List<PulledCard> {
        val cards = JSONObject(response).getJSONArray("cards")
        return List(cards.length()) { index ->
            val item = cards.getJSONObject(index)
            val deck = LiuYaoDecks.deckById(item.getString("deck"))
                ?: error("Unknown deck returned by connected pull.")
            val id = item.getInt("id")
            val card = deck.cards.getOrNull(id)
                ?: LiuYaoCard(id = id, name = item.getString("name"))
            PulledCard(deck = deck, card = card, position = index + 1)
        }
    }
}
