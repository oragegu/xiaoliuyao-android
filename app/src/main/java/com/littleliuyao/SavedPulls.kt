package com.littleliuyao

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SavedPull(
    val id: Long,
    val intention: String,
    val createdAt: Long,
    val cards: List<PulledCard>,
)

class SavedPullRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("saved_pulls", Context.MODE_PRIVATE)

    fun load(): List<SavedPull> {
        val raw = preferences.getString(KEY_RECORDS, "[]").orEmpty()
        return runCatching {
            val items = JSONArray(raw)
            List(items.length()) { index -> items.getJSONObject(index).toSavedPull() }
                .sortedByDescending { it.createdAt }
        }.getOrDefault(emptyList())
    }

    fun save(intention: String, cards: List<PulledCard>): SavedPull {
        val now = System.currentTimeMillis()
        val record = SavedPull(
            id = now,
            intention = intention.trim(),
            createdAt = now,
            cards = cards,
        )
        val updated = listOf(record) + load()
        persist(updated)
        return record
    }

    fun delete(id: Long) {
        persist(load().filterNot { it.id == id })
    }

    fun updateIntention(id: Long, intention: String): SavedPull? {
        var updatedRecord: SavedPull? = null
        val updated = load().map { record ->
            if (record.id == id) {
                record.copy(intention = intention.trim()).also { updatedRecord = it }
            } else {
                record
            }
        }
        persist(updated)
        return updatedRecord
    }

    private fun persist(records: List<SavedPull>) {
        val json = JSONArray()
        records.forEach { json.put(it.toJson()) }
        preferences.edit().putString(KEY_RECORDS, json.toString()).apply()
    }

    private fun SavedPull.toJson(): JSONObject {
        val cardItems = JSONArray()
        cards.forEach { pulled ->
            cardItems.put(
                JSONObject()
                    .put("deck", pulled.deck.id)
                    .put("cardId", pulled.card.id)
                    .put("cardName", pulled.card.name)
                    .put("position", pulled.position),
            )
        }

        return JSONObject()
            .put("id", id)
            .put("intention", intention)
            .put("createdAt", createdAt)
            .put("cards", cardItems)
    }

    private fun JSONObject.toSavedPull(): SavedPull {
        val cardItems = getJSONArray("cards")
        return SavedPull(
            id = getLong("id"),
            intention = optString("intention"),
            createdAt = getLong("createdAt"),
            cards = List(cardItems.length()) { index -> cardItems.getJSONObject(index).toPulledCard() },
        )
    }

    private fun JSONObject.toPulledCard(): PulledCard {
        val deck = LiuYaoDecks.deckById(getString("deck")) ?: LiuYaoDecks.all.first()
        val cardId = getInt("cardId")
        val card = deck.cards.getOrNull(cardId) ?: LiuYaoCard(cardId, optString("cardName", "未知"))
        return PulledCard(
            deck = deck,
            card = card,
            position = optInt("position", 1),
        )
    }

    private companion object {
        const val KEY_RECORDS = "records"
    }
}
