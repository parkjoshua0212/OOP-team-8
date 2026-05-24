package com.oop.game

class Balance {
    var amount = 0
        private set

    fun deposit(deposit: Int) {
        this.amount += deposit
    }

    fun withdraw(withdraw: Int): Boolean {
        if (withdraw <= amount) {
            this.amount -= withdraw
            return true
        } else {
            println("not enough money to continue")
            return false
        }
    }

    fun canAfford(bet: Int): Boolean {
        return bet <= amount
    }

    fun displayBalance() {
        println("Current Balance: $amount")
    }

}

class Wallet {
    private val balance = Balance()

    // 초기 자금 설정
    fun initialize(startingAmount: Int) {  // 추가
        balance.deposit(startingAmount)
    }

    // 베팅
    fun placeBet(bet: Int): Boolean {
        if (bet <= 0) {
            println("bet is negative")
            return false
        }
        if (balance.canAfford(bet)) {
            balance.withdraw(bet)
            return true
        }
        else {
            println("You cannot place that bet. Not enough funds.")
            return false
        }
    }

    // 게임 승리 시 베팅금액의 2배 입금
    fun playerWin(bet: Int) {
        balance.deposit(bet * 2)
    }
    // 잔액 확인
    fun getCurrentBalance(): Int {
        return balance.amount
    }
    // 3판마다 특정 금액(amount)에 도달하는지 여부 확인
    fun getBalanceAbove(amount: Int): Boolean {
        return getCurrentBalance() >= amount
    }
    // 현재 잔액 조회
    fun displayBalance() {
        balance.displayBalance()
    }
}


fun main(){
    val wallet = Wallet()

    wallet.initialize(100)
    wallet.displayBalance()

    println("Enter your bet:")
    val bet = readLine()?.toIntOrNull() ?: 0

    if (wallet.placeBet(bet)) {
        println("Bet of $bet placed successfully.")
        wallet.displayBalance()
    }
    else {
        println("Bet of $bet could not be placed.")
        wallet.displayBalance()
    }
}