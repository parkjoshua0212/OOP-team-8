import com.oop.game.Deck

open class Score {
    open var playerScore = 0
    open var dealerScore = 0

    open fun updateScore(playerScore: Int, dealerScore: Int) {
        this.playerScore = playerScore
        this.dealerScore = dealerScore
    }

}
class Player {
    var hand = mutableListOf<String>()
    override var playerScore = 0
    private var playerTurn = 0
    fun drawCard(deck: Deck) {
        if (deck.shuffledDeck().isNotEmpty()) {
            val card = deck.shuffledDeck().removeAt(0)
            hand.add(card)
            playerScore += CardValue().cardValues[card.split(" ")[0]] ?:0
        }
    }
    //Need to create player draw 2 cards at beginning of turn and then decide to draw more or not
    playerTurn++
    println("Player's turn $playerTurn")
    if (playerScore > 21) {
        println("Bust! Score: $playerScore")
    } else {
        println("Score: $playerScore")
    }
}

class Dealer {
    var hand = mutableListOf<String>()
    override var dealerScore = 0
    private var dealerTurn = 0
    fun drawCard(deck: Deck) {
        if (deck.shuffledDeck().isNotEmpty()) {
            val card = deck.shuffledDeck().removeAt(0)
            hand.add(card)
            dealerScore += CardValue().cardValues[card.split(" ")[0]] ?:0
        }
    }
    //Need to implement dealer's logic to draw cards until score is 17 or highers
    dealerTurn++
    println("Dealer's turn $dealerTurn")
    if (dealerScore < 17) {
        println("Dealer draws a card. Score: $dealerScore")
    } else if (dealerScore > 21) {
        println("Dealer busts! Score: $dealerScore")
    } else {
        println("Dealer stands. Score: $dealerScore")
    }
}