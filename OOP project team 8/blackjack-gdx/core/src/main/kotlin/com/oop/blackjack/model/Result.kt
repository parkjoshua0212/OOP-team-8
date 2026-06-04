package com.oop.blackjack.model

enum class RoundResult {
    BLACKJACK,   // Player natural 21 (and dealer doesn't have one)
    PLAYER_WIN,
    DEALER_WIN,
    PUSH,        // Tie
    PLAYER_BUST,
    DEALER_BUST  // Dealer busts → player wins
}

object ResultResolver {
    fun determine(player: Player, dealer: Dealer): RoundResult = when {
        player.isBust                                        -> RoundResult.PLAYER_BUST
        player.isBlackjack && dealer.isBlackjack             -> RoundResult.PUSH
        player.isBlackjack                                   -> RoundResult.BLACKJACK
        dealer.isBlackjack                                   -> RoundResult.DEALER_WIN
        dealer.isBust                                        -> RoundResult.DEALER_BUST
        player.score > dealer.score                          -> RoundResult.PLAYER_WIN
        player.score < dealer.score                          -> RoundResult.DEALER_WIN
        else                                                 -> RoundResult.PUSH
    }

    /**
     * Applies the result to the wallet.
     * NOTE: bet has already been deducted from wallet before this is called.
     */
    fun apply(result: RoundResult, bet: Int, wallet: Wallet, shop: Shop) {
        val hasDouble    = shop.isActive("double")
        val hasInsurance = shop.isActive("insurance")
        val effectiveBet = if (hasDouble && result == RoundResult.PLAYER_WIN) bet * 2 else bet

        when (result) {
            RoundResult.BLACKJACK   -> wallet.onBlackjack(bet)
            RoundResult.PLAYER_WIN,
            RoundResult.DEALER_BUST -> wallet.onWin(effectiveBet)
            RoundResult.PUSH        -> wallet.onPush(bet)
            RoundResult.PLAYER_BUST -> if (hasInsurance) wallet.onPush(bet)
            RoundResult.DEALER_WIN  -> Unit   // bet already lost
        }
    }

    fun displayText(result: RoundResult, shop: Shop): String = when (result) {
        RoundResult.BLACKJACK   -> "✦ BLACKJACK! ✦"
        RoundResult.PLAYER_WIN  -> if (shop.isActive("double")) "WIN  ×2 BONUS!" else "YOU WIN!"
        RoundResult.DEALER_BUST -> "DEALER BUSTS — YOU WIN!"
        RoundResult.PUSH        -> "PUSH — BET RETURNED"
        RoundResult.PLAYER_BUST -> if (shop.isActive("insurance")) "BUST — SHIELD SAVED YOU!" else "BUST!"
        RoundResult.DEALER_WIN  -> "DEALER WINS"
    }

    fun isPlayerPositive(result: RoundResult): Boolean = result in setOf(
        RoundResult.BLACKJACK, RoundResult.PLAYER_WIN, RoundResult.DEALER_BUST
    )
}
