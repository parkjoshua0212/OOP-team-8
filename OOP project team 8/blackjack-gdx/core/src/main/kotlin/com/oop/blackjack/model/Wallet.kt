package com.oop.blackjack.model

class Wallet(startingAmount: Int = 1000) {
    var balance: Int = startingAmount
        private set

    fun canAfford(amount: Int): Boolean = amount in 1..balance

    /** Deducts bet from balance. Returns false if insufficient funds. */
    fun placeBet(amount: Int): Boolean {
        if (!canAfford(amount)) return false
        balance -= amount
        return true
    }

    /** Called on a standard win: returns the bet + 1:1 profit. */
    fun onWin(bet: Int) { balance += bet * 2 }

    /** Called on blackjack: returns the bet + 3:2 profit. */
    fun onBlackjack(bet: Int) { balance += bet + (bet * 3 / 2) }

    /** Returns the original bet (push/tie, insurance save). */
    fun onPush(bet: Int) { balance += bet }

    fun deposit(amount: Int) { balance += amount }
}
