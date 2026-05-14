class Deck {
    val suit = listOf("Hearts", "Diamonds", "Clubs", "Spades")
    val rank = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace")

    fun createDeck(): List<String> {
        val deck = mutableListOf<String>()
        for (s in suit) {
            for (r in rank) {
                deck.add("$r of $s")
            }
        }
        return deck
    }

    fun shuffledDeck(): List<String> {
        return createDeck().shuffled()
    }
}

fun main() {
    val deck = Deck()
    println(deck.shuffledDeck())
}