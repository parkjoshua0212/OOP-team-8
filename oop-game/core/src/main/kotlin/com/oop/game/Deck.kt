
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

class CardValue {
    val cardValues = mapOf{
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
        // Ace can be 1 or 11 
        //if Value = Ace + 10 <= 21 then Ace = 11 
        //else Ace = 1
    }
}

fun main() {
    val deck = Deck()
    println(deck.shuffledDeck())
}
