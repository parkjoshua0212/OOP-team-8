package com.oop.game

class Balance {
    var amount = 0
        private set

    fun deposit(amount: Int){
        this.amount += amount
    }
    fun withdraw(withdraw: Int): Boolean {
        if (withdraw <= amount){
            this.amount -= withdraw
            return true
        } else {
            println("Not enough funds")
            return false
        }
    }
    fun canAfford(cost: Int): Boolean {
        return cost <= amount
    }
    fun displayBalance(){
        println("Balance: amount $amount")
    }
}

class Wallet {
    private val balance = Balance()

    fun initialize(startingAmount: Int) {
        balance.deposit(startingAmount)
    }

    fun placeBet(bet: Int): Boolean {
        if (bet <= 0) {
            println("Betting amount need to be above 0")
            return false
        }
        if (balance.canAfford(bet)) {
            balance.withdraw(bet)
            return true
        }
        return false
    }

    fun playerWin(bet: Int) {
        balance.deposit(bet * 2)
    }

    fun blackjackWin(bet: Int) {
        val blackjackPayout = bet * 5 / 2
        balance.deposit(blackjackPayout)
    }

    fun pushReturn(bet: Int) {
        balance.deposit(bet)
    }

    fun canAfford(amount: Int): Boolean {
        return balance.canAfford(amount)
    }

    fun getCurrentBalance(): Int {
        return balance.amount
    }

    fun displayBalance() {
        balance.displayBalance()
    }
}