package com.oop.blackjack.model

const val TOTAL_STAGES   = 20
const val ROUNDS_PER_STAGE = 5
const val INITIAL_BALANCE  = 1000

enum class GamePhase {
    BETTING,        // Player setting their bet
    DEALING,        // Cards animating onto table
    SHOP_PROMPT,    // Ask player if they want to visit shop (start of stage 2+)
    SHOP,           // Shop overlay open
    ITEM_PROMPT,    // Ask player if they want to activate owned items
    PEEK,           // Hint item: show dealer hidden card briefly
    PLAYER_TURN,    // Player chooses hit / stand
    DEALER_TURN,    // Dealer auto-plays
    ROUND_RESULT,   // Show round outcome
    STAGE_CLEAR,    // Stage target reached
    STAGE_FAIL,     // Ran out of money mid-stage
    GAME_WIN,       // All 20 stages cleared
    GAME_OVER       // Couldn't afford min bet / failed stage
}

class GameState {
    val wallet  = Wallet(INITIAL_BALANCE)
    val shop    = Shop()
    val deck    = Deck()
    val player  = Player()
    val dealer  = Dealer()

    var stage   = 1
    var round   = 1
    var currentBet = 0
    var phase   = GamePhase.BETTING
    var lastResult: RoundResult? = null

    val targetBalance: Int get() = INITIAL_BALANCE * (1 shl stage)  // 2000, 4000, 8000...
    val minBet: Int        get() = targetBalance / 10

    fun startNewRound() {
        deck.reset()
        player.reset()
        dealer.reset()
        shop.clearActive()
        currentBet = 0
        lastResult = null
        phase = GamePhase.BETTING
    }

    fun deal() {
        repeat(2) { player.draw(deck) }
        repeat(2) { dealer.draw(deck) }
    }

    fun advanceRound(): Boolean {
        if (round < ROUNDS_PER_STAGE) {
            round++
            return true   // more rounds in this stage
        }
        return false      // stage over
    }

    fun advanceStage(): Boolean {
        if (stage < TOTAL_STAGES) {
            stage++
            round = 1
            shop.fullReset()
            return true
        }
        return false      // all stages cleared
    }
}
