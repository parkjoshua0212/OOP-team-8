class Balance {
    var amount = 0
    fun deposit(deposit: Int) {
        this.amount += deposit
    }

    fun withdraw(withdraw: Int){
        if (withdraw <= amount) {
            this.amount -= withdraw
        }else {
            println("not enough money to continue")
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
    val balance = Balance()

    fun placeBet(bet: Int): Boolean {
        if (bet <= 0) {
            println("bet is negative")
            return false
        }
        if (balance.canAfford(bet)) {
            balance.withdraw(bet)
            return true
        } else {
            println("You cannot place that bet. Not enough funds.")
            return false
        }
    }
}

/*fun main(){
    val wallet = Wallet()
    wallet.balance.deposit(100)
    wallet.balance.displayBalance()


    println("Enter your bet:")
    val bet = readLine()?.toIntOrNull() ?: 0
    if (wallet.placeBet(bet)) {
        println("Bet of $bet placed successfully.")
        wallet.balance.displayBalance()
    }
    else {
        println("Bet of $bet could not be placed.")
        wallet.balance.displayBalance()
    }
}*/
