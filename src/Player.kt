package com.oop.game

open class Participant {
    var hand = mutableListOf<Card>()
        protected set
    var score = 0
        protected set

    fun calculateScore(hand: MutableList<Card>): Int {
        var score = 0
        var aceCount = 0
        for (card in hand) {
            val rank = card.rank.value
            score += rank
            if (card.rank == Rank.ACE)
                aceCount++
        }
        while (score > 21 && aceCount > 0) {
            score -= 10
            aceCount--
        }
        return score
    }

    fun drawCard(deck: Deck) {
        if (!deck.isEmpty()) {
            val card = deck.dealCard()
            hand.add(card)
            score = calculateScore(hand)
        }
    }
}


class Player : Participant() {
    fun takeTurn(deck: Deck) {
        while (score < 21) {
            println("Do you want to hit? (yes/no)")
            val input = readLine()
            if (input != null && input.equals("yes", ignoreCase = true)) {
                drawCard(deck)
                println("Your hand: $hand, Score: $score")
            } else {
                println("You stand.")
                break
            }
        }
    }
}


class Dealer : Participant() {
    fun takeTurn(deck: Deck) {
        while (score < 17) {
            drawCard(deck)
        }
    }
}

class Game {
    val wallet = Wallet()
    val shop = Shop()
    val result = Result()

    val initialBalance = 1000
    val totalStages = 20
    val roundsPerStage = 5

    fun getBet(minBet: Int): Int {
        while (true) {
            println("Enter betting amount: (Minimum betting req: $minBet)")
            val input = readLine()?.toIntOrNull()

            if (input == null || input < minBet) {
                println("Minimum bet is $minBet. Please bet above minimum bet")
                continue
            }
            if (!wallet.canAfford(input)) {
                println("Not enough funds")
                continue
            }
            wallet.placeBet(input)
            println("${input} bet complete!")
            return input
        }
    }

    fun openShop() {
        shop.showItems()
        println("Enter the item you want to purchase.")
        val itemName = readLine()
        if (!itemName.isNullOrEmpty()) {
            shop.buyItem(itemName, wallet)
            wallet.displayBalance()
        }
    }

    fun playRound(bet: Int) {
        val deck = Deck()
        val player = Player()
        val dealer = Dealer()

        player.drawCard(deck)
        dealer.drawCard(deck)
        player.drawCard(deck)
        dealer.drawCard(deck)

        println("\nMy hand: ${player.hand}, Score: ${player.score}")
        println("Dealers hand: ${dealer.hand[0]}, Score: ${dealer.hand[0]} + ?")

        if (shop.hasItem("hint")) {
            println("[Hint used!] Dealers hidden card is: ${dealer.hand[1]}")
        }

        player.takeTurn(deck)

        if (player.score > 21) {
            val roundResult = result.determine(player.score, dealer.score, player.hand.size, dealer.hand.size)
            result.applyResult(roundResult, bet, wallet, shop)
            wallet.displayBalance()
            return
        }

        dealer.takeTurn(deck)
        println("Dealers card: ${dealer.hand}, Score: ${dealer.score}")

        println("This rounds result")
        println("Players score: ${player.score} | Dealers score: ${dealer.score}")
        val roundResult = result.determine(player.score, dealer.score, player.hand.size, dealer.hand.size)
        result.applyResult(roundResult, bet, wallet, shop)
        wallet.displayBalance()
    }

    fun playStage(stage: Int, targetBalance: Int): Boolean {
        val minBet = targetBalance / 10

        println("\n===== Stage $stage =====")
        println("Stage $stage Target: $targetBalance")

        for (round in 1..roundsPerStage) {
            println("\n Round $round / $roundsPerStage")
            println("Minimum bet: $minBet")
            wallet.displayBalance()

            if (!wallet.canAfford(minBet)) {
                println("Not enough funds! Cannot continue with game")
                println("Game over")
                return false
            }

            if (stage != 1 && round == 1) {
                println("Use shop?")
                val shopInput = readlnOrNull()
                if (shopInput.equals("Yes", ignoreCase = true)) {
                    openShop()
                }
            }

            val bet = getBet(minBet)
            playRound(bet)
            shop.clearItems()
        }

        if (wallet.getCurrentBalance() >= targetBalance) {
            println("================")
            println("Stage $stage Clear!")
            println("Total money: ${wallet.getCurrentBalance()}")
            println("================")
            return true
        } else {
            println("================")
            println("You DIED")
            println("Total money: ${wallet.getCurrentBalance()}")
            println("================")
            return false
        }
    }

    fun start() {
        wallet.initialize(initialBalance)

        println("================")
        println("Welcome to the blackjack game")
        println("If you beat $totalStages stages you win!")
        println("================")

        var targetBalance = initialBalance * 2

        for (stage in 1..totalStages) {
            val cleared = playStage(stage, targetBalance)
            if (!cleared) return
            targetBalance *= 2
        }

        println("================")
        println("YOU CLEARED ALL STAGES!")
        println("Total money: ${wallet.getCurrentBalance()}")
        println("================")
    }
}

fun main() {
    val game = Game()
    game.start()
}