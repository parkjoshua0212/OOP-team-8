package com.oop.game

// 라운드 결과 유형을 enum으로 정의
enum class RoundResult {
    PLAYER_WIN,
    DEALER_WIN,
    TIE,
    PLAYER_BUST,
    BLACKJACK
}

// 라운드 결과 판정 및 베팅 정산을 담당하는 클래스
class Result {
    // 점수와 패 장수를 기반으로 라운드 결과 판정
    // 판정 순서 중요: 동시 블랙잭 → 플레이어 블랙잭 → 버스트 → 딜러 블랙잭 → 점수 비교 순
    fun determine(playerScore: Int, dealerScore: Int, playerHandSize: Int, dealerHandSize: Int): RoundResult {
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

    // 결과에 따라 베팅 정산 처리
    // double 아이템 보유 시 유효 베팅액 2배 적용, insurance는 버스트 시에만 작동
    fun applyResult(result: RoundResult, bet: Int, wallet: Wallet, shop: Shop) {
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
                // 동점: 배팅액 그대로 반환
                wallet.pushReturn(bet)
            }
            RoundResult.PLAYER_BUST -> {
                if (hasInsurance) {
                    println("Bust! but you've got insurance!")
                    // insurance 발동: 버스트여도 배팅액 반환
                    wallet.pushReturn(bet)
                } else {
                    println("Bust! -${effectiveBet}$")
                }
            }
            RoundResult.BLACKJACK -> {
                println("Blackjack!")
                // 블랙잭 승리: 배팅액의 2.5배 지급 (Wallet에서 처리)
                wallet.blackjackWin(bet)
            }
        }
    }
}