package com.oop.blackjack.model

class Deck {
    private val cards = mutableListOf<Card>()

    init { reset() }

    fun reset() {
        cards.clear()
        for (suit in Suit.values())
            for (rank in Rank.values())
                cards.add(Card(suit, rank))
        cards.shuffle()
    }

    val size: Int get() = cards.size
    fun isEmpty(): Boolean = cards.isEmpty()

    fun deal(): Card {
        check(!isEmpty()) { "Deck is empty" }
        return cards.removeAt(cards.lastIndex)
    }
}
