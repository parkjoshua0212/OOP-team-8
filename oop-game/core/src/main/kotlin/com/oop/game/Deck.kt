class Deck {
    val suit = listOf("Hearts", "Diamonds", "Clubs", "Spades")
    val rank = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace")
// Deck use Enum (suit and rank) instead of list, need to fix later
    val cards = mutableListOf<String>() //Change <String> to Card class later, need to create Card class first

    init {
        for (s in suit) {
            for (r in rank) {
                cards.add("$r of $s")
            }
        }
        cards.shuffle()
    }

    fun dealCard(): String {
        return cards
    }

    fun isEmpty(): Boolean {
        return cards.isEmpty()
    }
}

fun main() { //Testing to see deck is working properly, no need to keep this in final code
    val deck = Deck()
    println("Deck size: ${deck.cards.size}")
    println("First card: ${deck.dealCard()}")
    println("Deck size after dealing: ${deck.cards.size}")
}