package com.oop.game
enum class Suit(val symbol: String, val displayName: String) {
    HEARTS  ("♥", "Hearts"),
    DIAMONDS("♦", "Diamonds"),
    CLUBS   ("♣", "Clubs"),
    SPADES  ("♠", "Spades")
}

enum class Rank(val displayName: String, val value: Int) {
    TWO  ("2", 2),
    THREE("3", 3),
    FOUR ("4", 4),
    FIVE ("5", 5),
    SIX  ("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE ("9", 9),
    TEN  ("10", 10),
    JACK ("Jack", 10),
    QUEEN("Queen", 10),
    KING ("King", 10),
    ACE  ("Ace", 11);   // Ace 1/11 처리는 Hand 단계에서

    override fun toString(): String = displayName
}

class Deck {
    private val cards = mutableListOf<String>()

    init {
        for (s in Suit.values()) {
            for (r in Rank.values()) {
                cards.add("$r of ${s.displayName}")
            }
        }
        cards.shuffle()
    }

    val size: Int get() = cards.size

    fun dealCard(): String {
        return cards.removeAt(cards.size - 1)
    }

    fun isEmpty(): Boolean = cards.isEmpty()
}
