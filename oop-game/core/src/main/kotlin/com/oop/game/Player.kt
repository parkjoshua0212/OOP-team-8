package com.oop.game

fun calculateScore(hand: MutableList<Card>): Int {
    var score = 0
    var aceCount = 0
    for (card in hand) {
        val rank = card.rank.value
        score += rank
        if (card.rank == Rank.ACE)
            aceCount++
    }
    // 점수가 21점 이상일 경우 에이스 조정
    while (score > 21 && aceCount > 0) {
        score -= 10
        aceCount--
    }
    return score
}

class Player { //Score class is for calculating player's score, need to fix later parent class and how to use it in player and dealer class
//Score shouldn't be a parent class of player and dealer, need to fix later, maybe create a method in score class to calculate score and call it in player and dealer class
    var hand = mutableListOf<Card>()
    var playerScore = 0

    fun drawCard(deck: Deck) {
        if (!deck.isEmpty()) {
            val card = deck.dealCard()
            hand.add(card)
            playerScore = calculateScore(hand)
        }
    }
    //Need to create player draw 2 cards at beginning of turn and then decide to draw more or not
    //Beginning of turn -> Player draw card, dealer draw, player draw, dealer draw.//check if this is the right way to call startTurn() method from GameTurn class
    //One card of the dealer is hidden
}

class Dealer {//same with dealer class, fix later
    var hand = mutableListOf<Card>()
    var dealerScore = 0

    fun drawCard(deck: Deck) {
        if (!deck.isEmpty()) {
            val card = deck.dealCard()
            hand.add(card)
            dealerScore = calculateScore(hand)
        }
    }
    
    //Need to implement dealer's logic to draw cards until score is 17 or highers
    
}

class GameTurn {
    val deck = Deck()
    val player = Player()
    val dealer = Dealer()

    fun startTurn() {
        player.drawCard(deck)
        dealer.drawCard(deck)
        player.drawCard(deck)
        dealer.drawCard(deck)

        println("Your hand: ${player.hand}, Score: ${player.playerScore}")
        println("Dealer shows: ${dealer.hand[0]}")

        while (player.playerScore < 21) {
            println("Do you want to hit? (yes/no)")
            val input = readLine()
            if (input.equals("yes", ignoreCase = true)) {
                player.drawCard(deck)
                println("Your hand: ${player.hand}, Score: ${player.playerScore}")
            } else {
                println("You stand.")
                break
            }
        }

        if (player.playerScore > 21) {
            println("You busted! Dealer wins.")
            return
        }

        println("Dealer's hand: ${dealer.hand}, Score: ${dealer.dealerScore}")
        while (dealer.dealerScore < 17) {
            dealer.drawCard(deck)
            println("Dealer draws. Hand: ${dealer.hand}, Score: ${dealer.dealerScore}")
        }

        println("--- RESULTS ---")
        println("Your hand: ${player.hand}, Score: ${player.playerScore}")
        println("Dealer hand: ${dealer.hand}, Score: ${dealer.dealerScore}")

        when {
            dealer.dealerScore > 21 -> println("Dealer busts! You win!")
            player.playerScore > dealer.dealerScore -> println("You win!")
            player.playerScore < dealer.dealerScore -> println("Dealer wins!")
            else -> println("It's a tie!")
        }
    }
}

fun main() {
    val game = GameTurn()
    game.startTurn()
}