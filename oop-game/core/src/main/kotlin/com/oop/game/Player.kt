import com.oop.game.Deck

open class Score() { //Sc0re class into a different property, need to fix later
        open val cardValues = mapOf(
            "2" to 2,
            "3" to 3,
            "4" to 4,
            "5" to 5,
            "6" to 6,
            "7" to 7,
            "8" to 8,
            "9" to 9,
            "10" to 10,
            "Jack" to 10,
            "Queen" to 10,
            "King" to 10,
            "Ace" to 11 //Ace need to be 1 or 11 depedning on players score, fix later
        )
}

class Player: Score(){ //Score class is for calculating player's score, need to fix later parent class and how to use it in player and dealer class
//Score shouldn't be a parent class of player and dealer, need to fix later, maybe create a method in score class to calculate score and call it in player and dealer class
    var hand = mutableListOf<String>()
    var playerScore = 0
    private var playerTurn = 0
    fun drawCard(deck: Deck) {
        if (deck.shuffledDeck().isNotEmpty()) {
            val card = deck.shuffledDeck().removeAt(0)
            hand.add(card)
            playerScore += Score().cardValues[card.split(" ")[0]] ?:0
        }
    }
    //Need to create player draw 2 cards at beginning of turn and then decide to draw more or not
    //Beginning of turn -> Player draw card, dealer draw, player draw, dealer draw.
    val playerStartTurn = GameTurn().startTurn()//check if this is the right way to call startTurn() method from GameTurn class
    fun takeTurn() {
        playerStartTurn
        println("Player's turn $playerTurn")
        println("Player's hand: $hand, Score: $playerScore")
        println("Do you want to draw another card? (yes/no)")
        val input = readLine()
        if (input.equals("yes", ignoreCase = true)) {
            drawCard(Deck())
        } else {
            println("Player stands. Score: $playerScore")
        }
        playerTurn++
    }
}
//One card of the dealer is hidden
class Dealer: Score() {//same with dealer class, fix later
    var hand = mutableListOf<String>()
    var dealerScore = 0
    private var dealerTurn = 0
    fun drawCard(deck: Deck) {
        if (deck.shuffledDeck().isNotEmpty()) {
            val card = deck.shuffledDeck().removeAt(0)
            hand.add(card)
            dealerScore += Score().cardValues[card.split(" ")[0]] ?:0
        }
    }
    //Need to implement dealer's logic to draw cards until score is 17 or highers
    fun takeTurn() {
        println("Dealer's turn $dealerTurn")
        println("Dealer's hand: ${hand[0]}, Score: ${Score().cardValues[hand[0].split(" ")[0]] ?:0}")
        while (dealerScore < 17) {
            drawCard(Deck())
            println("Dealer draws a card. Hand: $hand, Score: $dealerScore")
        }
        println("Dealer stands. Final hand: $hand, Final score: $dealerScore")
        dealerTurn++
    }
}

class GameTurn {//need to fix other classes before this
    //Later on animation need to be implemented showing card draw one by one instead of showing everything at once, need to fix later
    val deck = Deck()
    val player = Player()
    val dealer = Dealer()

    fun startTurn() {
        // Initial draw
        player.drawCard(deck)
        dealer.drawCard(deck)
        player.drawCard(deck)
        dealer.drawCard(deck)

        // Player's turn
        while (player.playerScore < 21) {
            println("Player's hand: ${player.hand}, Score: ${player.playerScore}")
            println("Do you want to draw another card? (yes or no)")
            val input = readLine()
            if (input.equals("yes", ignoreCase = true)) {
                player.drawCard(deck)
            } else {
                break
            }
        }

        // Dealer's turn
        while (dealer.dealerScore < 17) {
            dealer.drawCard(deck)
        }

        // Determine winner
        println("Player's final hand: ${player.hand}, Score: ${player.playerScore}")
        println("Dealer's final hand: ${dealer.hand}, Score: ${dealer.dealerScore}")
        when {
            player.playerScore > 21 -> println("Player busts. Dealer wins.")
            dealer.dealerScore > 21 -> println("Dealer busts. Player wins.")
            player.playerScore > dealer.dealerScore -> println("Player wins!")
            player.playerScore < dealer.dealerScore -> println("Dealer wins!")
            else -> println("It's a tie!")
        }
    }
}

fun main() {
    val game = GameTurn()
    game.startTurn()
}