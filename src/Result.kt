package com.oop.game

enum class RoundResult{
    PLAYER_WIN,
    DEALER_WIN,
    TIE,
    PLAYER_BUST,
    BLACKJACK
}

class Result{
    fun determine(playerScore: Int, dealerScore: Int, playerHandSize: Int, dealerHandSize: Int): RoundResult{
        return when {
            playerScore == 21 && dealerScore == 21 && playerHandSize == 2 && dealerHandSize == 2 -> RoundResult.TIE
            playerHandSize == 2 && playerScore == 21 -> RoundResult.BLACKJACK
            playerScore > 21 -> RoundResult.PLAYER_BUST
            dealerHandSize == 2 && dealerScore == 21 -> RoundResult.DEALER_WIN
            dealerScore > 21 -> RoundResult.PLAYER_WIN
            playerScore > dealerScore -> RoundResult.PLAYER_WIN
            playerScore < dealerScore -> RoundResult.DEALER_WIN
            else -> RoundResult.TIE
        }
    }

    fun applyResult(result: RoundResult, bet: Int, wallet: Wallet, shop: Shop){
        val hasDouble = shop.checkMyItem("double")
        val hasInsurance = shop.checkMyItem("insurance")
        val effectiveBet = if (hasDouble) bet * 2 else bet

        when (result) {
            RoundResult.PLAYER_WIN -> {
                println("Win! +${effectiveBet * 2}$")
                wallet.playerWin(effectiveBet)
            }
            RoundResult.DEALER_WIN -> {
                println("Lost! -${effectiveBet}$")
            }
            RoundResult.TIE -> {
                println("Tie!")
                wallet.pushReturn(bet)
            }
            RoundResult.PLAYER_BUST -> {
                if (hasInsurance) {
                    println("Bust! but you've got insurance!")
                    wallet.pushReturn(bet)
                } else {
                    println("Bust! -${effectiveBet}$")
                }
            }
            RoundResult.BLACKJACK -> {
                println("Blackjack!")
                wallet.blackjackWin(bet)
            }
        }
    }
}