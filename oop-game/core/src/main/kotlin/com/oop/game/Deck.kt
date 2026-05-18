package com.oop.game

//카드 무늬 - 카드 무늬는 4개의 선택지 고정이므로 enum 클래스로
enum class Suit(val symbol: String, val displayName: String) {
    HEARTS  ("♥", "Hearts"),
    DIAMONDS("♦", "Diamonds"),
    CLUBS   ("♣", "Clubs"),
    SPADES  ("♠", "Spades")
}

//카드 숫자 - 위와 마찬가지
enum class Rank(val displayName: String, val value: Int) {
    TWO  ("2", 2),
    THREE("3", 3),
    FOUR ("4", 4),
    FIVE ("5", 5),
    SIX  ("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE ("9", 9),
    TEN  ("10", 10),
    JACK ("J", 10),
    QUEEN("Q", 10),
    KING ("K", 10),
    ACE  ("A", 11);
    // 블랙잭에서 Ace는 1 또는 11 둘 다 가능
    // Ace 1/11 처리는 Hand 단계에서

    override fun toString(): String = displayName
}

//카드 클래스 생성 - 출력 시 "K♤"와 같은 형태로 출력
class Card(val suit : Suit, val rank : Rank) {
    override fun toString(): String = rank.displayName + suit.symbol
}

class Deck {
    //private으로 지정하여 밖에서는 못 보도록 설계
    private val cards = mutableListOf<Card>()

    //리스트에 카드 52장을 무작위로 섞어 넣음
    init {
        for (s in Suit.values()) {
            for (r in Rank.values()) {
                cards.add(Card(s, r))
            }
        }
        cards.shuffle()
    }

    //남은 카드 갯수 세기
    val size: Int get() = cards.size

    //리스트 맨 뒤 카드를 꺼내서 반환
    fun dealCard(): Card {
        return cards.removeAt(cards.size - 1)
    }

    //덱이 비었는지 확인
    fun isEmpty(): Boolean = cards.isEmpty()
}
