package com.littleliuyao

import java.security.SecureRandom

class LocalCardPuller(
    private val random: SecureRandom = SecureRandom(),
) {
    fun pull(): List<PulledCard> = LiuYaoDecks.all.mapIndexed { index, deck ->
        PulledCard(
            deck = deck,
            card = deck.cards[random.nextInt(deck.cards.size)],
            position = index + 1,
        )
    }
}

sealed interface PullResult {
    data class Success(val cards: List<PulledCard>, val message: String? = null) : PullResult
    data class Failure(val message: String, val fallbackCards: List<PulledCard>) : PullResult
}

class PullRepository(
    private val localPuller: LocalCardPuller = LocalCardPuller(),
    private val connectedPuller: ConnectedCardPuller = ConnectedCardPuller(BuildConfig.PULL_ENDPOINT),
) {
    suspend fun pullCards(intention: String, privatePull: Boolean): PullResult {
        if (privatePull) {
            return PullResult.Success(localPuller.pull())
        }

        return connectedPuller.pull(intention = intention, persist = true).fold(
            onSuccess = { PullResult.Success(it) },
            onFailure = {
                PullResult.Failure(
                    message = it.message ?: "Connected pull is not available. A private local pull was made instead.",
                    fallbackCards = localPuller.pull(),
                )
            },
        )
    }
}
