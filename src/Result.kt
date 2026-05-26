package com.oop.game

enum class RoundResult{
    PLAYER_WIN,
    DEALER_WIN,
    TIE,
    PLAYER_BUST
}

class Result{
    fun determine(playerScore: Int, dealerScore: Int): RoundResult{
        return when {
            playerScore > 21 -> RoundResult.PLAYER_BUST
            dealerScore > 21 -> RoundResult.DEALER_WIN
            playerScore > dealerScore -> RoundResult.PLAYER_WIN
            playerScore < dealerScore -> RoundResult.DEALER_WIN
            else -> RoundResult.TIE
        }
    }

    fun applyResult(result: RoundResult, bet: Int, wallet: Wallet,  shop: Shop){
        val hasDouble = shop.hasItem("double")
        val hasInsurance = shop.hasItem("insurance")
        val effectiveBet = if (hasDouble) bet * 2 else bet

        when (result) {
            RoundResult.PLAYER_WIN -> {
                println("Win! +${effectiveBet * 2}$")
                wallet.playerWin(effectiveBet)

            }
            RoundResult.DEALER_WIN -> {
                println("Lost! -${bet}$")
                //if item double is used
                if (hasDouble) {
                    //if hasDouble extra loss in wallet
                    wallet.placeBet(bet)
                }
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
                    println("Bust! -${bet}$")
                    if (hasDouble) {
                        wallet.placeBet(bet)
                    }
                }
            }
        }
    }
}