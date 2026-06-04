package com.oop.blackjack.model

abstract class Participant {
    val hand = mutableListOf<Card>()

    val score: Int
        get() {
            var total = hand.sumOf { it.rank.baseValue }
            var aces = hand.count { it.rank == Rank.ACE }
            while (total > 21 && aces-- > 0) total -= 10
            return total
        }

    val isBust: Boolean get() = score > 21
    val isBlackjack: Boolean get() = hand.size == 2 && score == 21

    fun draw(deck: Deck) {
        if (!deck.isEmpty()) hand.add(deck.deal())
    }

    fun reset() = hand.clear()
}

class Player : Participant()

class Dealer : Participant() {
    /** Returns true when the dealer must draw another card (< 17). */
    fun shouldDraw(): Boolean = score < 17
}
