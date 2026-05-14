package com.littleliuyao

data class LiuYaoCard(
    val id: Int,
    val name: String,
)

data class CardDeck(
    val id: String,
    val deckName: String,
    val englishLabel: String,
    val assetFolder: String,
    val cards: List<LiuYaoCard>,
)

data class PulledCard(
    val deck: CardDeck,
    val card: LiuYaoCard,
    val position: Int,
)

object LiuYaoDecks {
    val spirit = CardDeck(
        id = "A",
        deckName = "六神",
        englishLabel = "Spirit",
        assetFolder = "deckA",
        cards = listOf("青龍", "朱雀", "勾陳", "螣蛇", "白虎", "玄武").mapIndexed(::LiuYaoCard),
    )

    val relation = CardDeck(
        id = "B",
        deckName = "六親",
        englishLabel = "Relation",
        assetFolder = "deckB",
        cards = listOf("兄弟", "子孫", "妻財", "官鬼", "父母", "世應").mapIndexed(::LiuYaoCard),
    )

    val earth = CardDeck(
        id = "C",
        deckName = "地支",
        englishLabel = "Earth",
        assetFolder = "deckC",
        cards = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥").mapIndexed(::LiuYaoCard),
    )

    val all = listOf(spirit, relation, earth)

    fun deckById(id: String): CardDeck? = all.firstOrNull { it.id == id }
}
