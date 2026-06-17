package com.oop.game

// Player와 Dealer의 공통 속성/기능을 묶은 부모 클래스
// open: 상속할 수 있도록 열어둠

open class Participant {
    // 손패 리스트, protected set: 외부에서 읽기는 가능하나 수정은 이 클래스와 하위 클래스에서만
    var hand = mutableListOf<Card>()
        protected set
    // 점수, 위와 마찬가지
    var score = 0
        protected set

    // Ace를 11로 계산하되, 21 초과 시 순서대로 1로 줄여 최적 점수 계산
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

    // 덱에서 카드를 뽑아 손패에 추가하고 점수 갱신
    fun drawCard(deck: Deck) {
        if (!deck.isEmpty()) {
            val card = deck.dealCard()
            hand.add(card)
            score = calculateScore(hand)
        }
    }
}


// Participant를 상속받은 플레이어 클래스
class Player : Participant() {
    // 점수가 21 미만일 때 히트/스탠드 반복 선택
    fun takeTurn(deck: Deck) {
        while (score < 21) {
            println("Do you want to hit? (yes/no)")
            val input = readlnOrNull()
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


// 딜러는 17 미만이면 무조건 히트 (플레이어와 달리 선택지 없음)
class Dealer : Participant() {
    fun takeTurn(deck: Deck) {
        while (score < 17) {
            drawCard(deck)
        }
    }
}

// 게임 전체 흐름을 관리하는 클래스
class Game {
    val wallet = Wallet()
    val shop = Shop()
    val result = Result()

    val initialBalance = 1000
    val totalStages = 20
    val roundsPerStage = 5

    // 최소 베팅액 이상, 잔액 이내로 베팅 입력받기
    fun getBet(minBet: Int): Int {
        while (true) {
            println("Enter betting amount: (Minimum betting req: $minBet)")
            val input = readlnOrNull()?.toIntOrNull()

            if (input == null || input < minBet) {
                println("Minimum bet is $minBet. Please bet above minimum bet")
                continue
            }
            if (!wallet.canAfford(input)) {
                println("Not enough funds")
                continue
            }
            wallet.placeBet(input)
            println("$input bet complete!")
            return input
        }
    }

    // 상점을 열어 아이템 구매 진행
    fun openShop() {
        shop.showItems()
        println("Enter the item you want to purchase.")
        val itemName = readlnOrNull()
        if (!itemName.isNullOrEmpty()) {
            shop.buyItem(itemName, wallet)
            wallet.displayBalance()
        }
    }

    // 한 라운드 진행: 카드 배분 → 아이템 사용 → 플레이어 턴 → 딜러 턴 → 결과 처리
    fun playRound(bet: Int) {
        val deck = Deck()
        val player = Player()
        val dealer = Dealer()

        player.drawCard(deck)
        dealer.drawCard(deck)
        player.drawCard(deck)
        dealer.drawCard(deck)

        println("\nMy hand: ${player.hand}, Score: ${player.score}")
        println("Dealers hand: ${dealer.hand[0]}, Score: ${dealer.hand[0].rank.value}")

        if (shop.hasItems()) {
            println("Do you want to use your items? ${shop.ownedItems} (yes/no)")
            val input = readlnOrNull()
            if (input.equals("yes", ignoreCase = true)) {
                shop.activateItems()
            }
        }

        if (shop.checkMyItem("hint")) {
            println("[Hint used!] Dealers hidden card is: ${dealer.hand[1]}")
        }

        player.takeTurn(deck)

        if (player.score > 21) {
            val roundResult = result.determine(player.score, dealer.score, player.hand.size, dealer.hand.size)
            result.applyResult(roundResult, bet, wallet, shop)
            wallet.displayBalance()
            return
        }

        dealer.takeTurn(deck)
        println("Dealers card: ${dealer.hand}, Score: ${dealer.score}")

        println("This rounds result")
        println("Players score: ${player.score} | Dealers score: ${dealer.score}")
        val roundResult = result.determine(player.score, dealer.score, player.hand.size, dealer.hand.size)
        result.applyResult(roundResult, bet, wallet, shop)
        wallet.displayBalance()
    }

    // 한 스테이지(5라운드) 진행, 목표 금액 달성 시 true 반환
    fun playStage(stage: Int, targetBalance: Int): Boolean {
        val minBet = targetBalance / 10

        println("\n===== Stage $stage =====")
        println("Stage $stage Target: $targetBalance")

        for (round in 1..roundsPerStage) {
            println("\n Round $round / $roundsPerStage")
            println("Minimum bet: $minBet")
            wallet.displayBalance()

            if (!wallet.canAfford(minBet)) {
                println("Not enough funds! Cannot continue with game")
                println("Game over")
                return false
            }

            if (stage != 1 && round == 1) {
                println("Use shop? (yes/no)")
                val shopInput = readlnOrNull()
                if (shopInput.equals("yes", ignoreCase = true)) {
                    openShop()
                }
            }

            val bet = getBet(minBet)
            playRound(bet)
            shop.clearItems()
        }

        if (wallet.getCurrentBalance() >= targetBalance) {
            println("================")
            println("Stage $stage Clear!")
            println("Total money: ${wallet.getCurrentBalance()}")
            println("================")
            return true
        } else {
            println("================")
            println("You DIED")
            println("Total money: ${wallet.getCurrentBalance()}")
            println("================")
            return false
        }
    }

    // 게임 시작: 잔액 초기화 후 20스테이지 순서대로 진행
    fun start() {
        wallet.initialize(initialBalance)

        println("================")
        println("Welcome to the blackjack game")
        println("If you beat $totalStages stages you win!")
        println("================")

        var targetBalance = initialBalance * 2

        for (stage in 1..totalStages) {
            val cleared = playStage(stage, targetBalance)
            if (!cleared) return
            targetBalance *= 2
        }

        println("================")
        println("YOU CLEARED ALL STAGES!")
        println("Total money: ${wallet.getCurrentBalance()}")
        println("================")
    }
}

// 프로그램 시작
fun main() {
    val game = Game()
    game.start()
}