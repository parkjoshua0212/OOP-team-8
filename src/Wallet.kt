package com.oop.game

// 실제 금액 데이터를 관리하는 클래스
class Balance {
    // private set: 외부에서 읽기는 가능하나 수정은 이 클래스 내부에서만
    var amount = 0
        private set

    // 금액 추가
    fun deposit(amount: Int) {
        this.amount += amount
    }

    // 금액 차감, 잔액 부족 시 false 반환
    fun withdraw(withdraw: Int): Boolean {
        if (withdraw <= amount) {
            this.amount -= withdraw
            return true
        } else {
            println("Not enough funds")
            return false
        }
    }

    // 특정 금액을 지불할 수 있는지 확인
    fun canAfford(cost: Int): Boolean {
        return cost <= amount
    }

    // 현재 잔액 출력
    fun displayBalance() {
        println("Balance: amount $amount")
    }
}

// Balance는 순수한 금액 처리, Wallet은 게임 규칙에 맞는 입출금 역할
class Wallet {
    private val balance = Balance()

    // 게임 시작 시 초기 잔액 설정
    fun initialize(startingAmount: Int) {
        balance.deposit(startingAmount)
    }

    // 배팅 금액 차감 (0 이하 입력 검사 포함)
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

    // 일반 승리: 배팅액의 2배 지급
    fun playerWin(bet: Int) {
        balance.deposit(bet * 2)
    }

    // 블랙잭 승리: 배팅액의 2.5배 지급
    fun blackjackWin(bet: Int) {
        val blackjackPayout = bet * 5 / 2
        balance.deposit(blackjackPayout)
    }

    // 타이 또는 insurance 발동 시 배팅액 그대로 반환
    fun pushReturn(bet: Int) {
        balance.deposit(bet)
    }

    // Balance의 canAfford를 반환
    fun canAfford(amount: Int): Boolean {
        return balance.canAfford(amount)
    }

    // 현재 잔액 반환
    fun getCurrentBalance(): Int {
        return balance.amount
    }

    // 잔액 출력
    fun displayBalance() {
        balance.displayBalance()
    }
}