
class Score() { //Score class into a different property, need to fix later
    val cardValues = mapOf(
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

    fun calculateScore(hand: MutableList<String>): Int {
        var score = 0
        var aceCount = 0
        for (card in hand) {
            val rank = card.split(" ")[0]
            score += cardValues[rank] ?: 0
            if (rank =="Ace") {
                aceCount++
            }
        }
        // Adjust for aces if the score is over 21
        while (score > 21 && aceCount > 0) {
            score -= 10
            aceCount--
        }
        return score
    }
}

class Player{ //Score class is for calculating player's score, need to fix later parent class and how to use it in player and dealer class
    //Score shouldn't be a parent class of player and dealer, need to fix later, maybe create a method in score class to calculate score and call it in player and dealer class

    val wallet = Wallet()

    var hand = mutableListOf<String>()
    var playerScore = 0
    val score = Score()

    fun drawCard(deck: Deck) {
        if (!deck.isEmpty()) {
            val card = deck.dealCard()
            hand.add(card)
            playerScore = score.calculateScore(hand)
        }
    }
}
//Need to create player draw 2 cards at beginning of turn and then decide to draw more or not
//Beginning of turn -> Player draw card, dealer draw, player draw, dealer draw.//check if this is the right way to call startTurn() method from GameTurn class
//One card of the dealer is hidden


class Dealer {//same with dealer class, fix later
var hand = mutableListOf<String>()
    var dealerScore = 0
    val score = Score()

    fun drawCard(deck: Deck) {
        if (!deck.isEmpty()) {
            val card = deck.dealCard()
            hand.add(card)
            dealerScore = score.calculateScore(hand)
        }
    }

    //Need to implement dealer's logic to draw cards until score is 17 or highers

}

class GameTurn {
    var gameTurn = 1
    fun minimumBet(wallet: Wallet) {//minimum bet placement per round
        val minBet = (500 * Math.pow(3.0, (gameTurn - 1).toDouble())).toInt()//Change 3.0 multiplier to needed in the future

        if (wallet.balance.amount < minBet){
            println("Not enough money for this round")
            return
        }
        else {
            println("The minimum bet requirement for this round is: $minBet")
        }
    }


    val deck = Deck()
    val player = Player()
    val dealer = Dealer()
    val wallet = Wallet()

    fun startTurn() {
        player.hand.clear()//Each round player and dealer cards and score reset
        dealer.hand.clear()
        player.playerScore = 0
        dealer.dealerScore = 0

        //need to remove player money with betting cost


        player.drawCard(deck)
        dealer.drawCard(deck)
        player.drawCard(deck)
        dealer.drawCard(deck)

        println("Your hand: ${player.hand}, Score: ${player.playerScore}")
        println("Dealer shows: ${dealer.hand[0]} and [Hidden card]")

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
        gameTurn++
    }

fun main() {
    val game = GameTurn()
    while(true){
        game.startTurn()
    }
}}