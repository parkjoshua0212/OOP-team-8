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

    fun resetHand(){ //function to reset players hand and score
        hand.clear()
        score = 0
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

    //Minimum round betting system
    val minBets = listOf(250, 1000, 2500, 5000, 10000)
    val totalRounds = 5

    fun start() {
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

            //Card distribution
            player.drawCard(deck)
            dealer.drawCard(deck)
            player.drawCard(deck)
            dealer.drawCard(deck)

            //Show player hands
            println("\nMy hand: ${player.hand}, Score: ${player.score}")
            println("Dealers hand: ${dealer.hand[0]}")

            //Hint item function -> Shows dealers hidden hand
            if (shop.hasItem("hint")) {
                println("[Hint used!] Dealers hidden card is: ${dealer.hand[1]}")

            }

            //player takes turn
            player.takeTurn(deck)

            //Check for bust
            if (player.score > 21) {
                val roundResult = result.determine(player.score, dealer.score)
                result.applyResult(roundResult, bet, wallet, shop)
                wallet.displayBalance()
                return
            }

            //Dealers turn
            dealer.takeTurn(deck)
            println("Dealers card: ${dealer.hand}, Score: ${dealer.score}")

            //Result check
            println("This rounds result")
            println("Players score: ${player.score} | Dealers score: ${dealer.score}")
            val roundResult = result.determine(player.score, dealer.score)
            result.applyResult(roundResult, bet, wallet, shop)
            wallet.displayBalance()
        }

        wallet.initialize(1000)

        println("================")
        println("Welcome to the blackjack game")
        println("If you beat 5 rounds of blackjack you win!")
        println("================")

        for (round in 1..totalRounds) {
            val minBet = minBets[round - 1]

            println("\n Round $round / $totalRounds")
            println("Minimum bet: $minBet")
            wallet.displayBalance()

            //Minimum betting check
            if (!wallet.canAfford(minBet)) {
                println("Not enough funds! Cannot continue with game")
                println("Game over")
                return
            }

            //shop
            println("Use shop?")
            val shopInput = readLine()
            if (shopInput.equals("Yes", ignoreCase = true) == true) {
                openShop()
            }

            val bet = getBet(minBet)

            //Play round
            playRound(bet)

            //When round end clear item
            shop.clearItems()

            //If player beats 5 rounds
            println("================")
            println("Congratulations! You Win!")
            println("Total money: ${wallet.getCurrentBalance()}")
            println("================")
        }
    }
}

fun main() {
    val game = Game()
    game.start()
}
