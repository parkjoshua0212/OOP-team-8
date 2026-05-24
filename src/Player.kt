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

class GameTurn {
    val deck = Deck()
    val player = Player()
    val dealer = Dealer()
    val shop = Shop()
    var balance = 1000

    fun startTurn() {
        println("상점을 이용하시겠습니까? (yes/no)")
        val input = readLine()
        if (input == "yes") {
            openShop()
        }
        player.drawCard(deck)
        dealer.drawCard(deck)
        player.drawCard(deck)
        dealer.drawCard(deck)

        println("Your hand: ${player.hand}, Score: ${player.score}")
        println("Dealer shows: ${dealer.hand[0]}")

        player.takeTurn(deck)

        if (player.score > 21) {
            println("You busted! Dealer wins.")
            return
        }

        println("Dealer's hand: ${dealer.hand}, Score: ${dealer.score}")
        dealer.takeTurn(deck)
        println("Dealer draws. Hand: ${dealer.hand}, Score: ${dealer.score}")


        println("--- RESULTS ---")
        println("Your hand: ${player.hand}, Score: ${player.score}")
        println("Dealer hand: ${dealer.hand}, Score: ${dealer.score}")

        when {
            dealer.score > 21 -> println("Dealer busts! You win!")
            player.score > dealer.score -> println("You win!")
            player.score < dealer.score -> println("Dealer wins!")
            else -> println("It's a tie!")
        }
    }
    fun openShop() {
        shop.showItems()
        println("구매할 아이템 이름을 입력하세요 (없으면 그냥 엔터)")
        val itemName = readLine()
        if (itemName != null && itemName != "") {
            balance = shop.buyItem(itemName, balance)
            println("현재 잔액: ${balance}원")
        }
    }
}


    fun main() {
        val game = GameTurn()
        game.startTurn()
    }
