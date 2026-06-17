package com.blackjack

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class BlackjackScreen : ScreenAdapter() {

    private val batch = SpriteBatch()
    private val font = BitmapFont()

    private val wallet = Wallet()
    private val shop = Shop()
    private val result = Result()
    private val player = Player()
    private val dealer = Dealer()

    private val cardTextures = mutableMapOf<String, Texture>()
    private val cardBack: Texture by lazy { Texture(Gdx.files.internal("cards/card_back.png")) }

    private var bet = 0
    private var betInput = ""
    private var shopInput = ""
    private var stage = 1
    private var round = 1
    private var targetBalance = 2000
    private val totalStages = 20
    private val roundsPerStage = 5

    enum class GameState {
        SHOPPING, BETTING, ITEM_USE, PLAYER_TURN, ROUND_OVER, GAME_OVER, STAGE_CLEAR, ALL_CLEAR
    }

    private var state = GameState.SHOPPING
    private var message = ""

    private val screenW = 1280f
    private val screenH = 720f

    init {
        wallet.initialize(1000)
        loadCardTextures()
        startNewRound()
    }

    private fun loadCardTextures() {
        val suits = listOf("hearts", "diamonds", "clubs", "spades")
        val ranks = listOf("02", "03", "04", "05", "06", "07", "08", "09", "10", "J", "Q", "K", "A")
        for (suit in suits) {
            for (rank in ranks) {
                val key = "${suit}_${rank}"
                val path = "cards/card_${suit}_${rank}.png"
                cardTextures[key] = Texture(Gdx.files.internal(path))
            }
        }
    }

    private fun getCardTexture(card: Card): Texture {
        val suitName = when (card.suit) {
            Suit.HEARTS -> "hearts"
            Suit.DIAMONDS -> "diamonds"
            Suit.CLUBS -> "clubs"
            Suit.SPADES -> "spades"
        }
        val rankName = when (card.rank) {
            Rank.TWO -> "02"
            Rank.THREE -> "03"
            Rank.FOUR -> "04"
            Rank.FIVE -> "05"
            Rank.SIX -> "06"
            Rank.SEVEN -> "07"
            Rank.EIGHT -> "08"
            Rank.NINE -> "09"
            Rank.TEN -> "10"
            Rank.JACK -> "J"
            Rank.QUEEN -> "Q"
            Rank.KING -> "K"
            Rank.ACE -> "A"
        }
        return cardTextures["${suitName}_${rankName}"] ?: cardBack
    }

    private fun startNewRound() {
        val minBet = targetBalance / 10
        if (!wallet.canAfford(minBet)) {
            state = GameState.GAME_OVER
            message = "Game Over  R = Restart"
            return
        }
        player.hand.clear()
        dealer.hand.clear()
        shopInput = ""
        if (round == 1) {
            state = GameState.SHOPPING
            message = "1 = hint(200)  2 = insurance(300)  3 = double(500)  |  SPACE to skip shop"
        } else {
            state = GameState.BETTING
            betInput = ""
            message = "Enter bet amount and press Enter"
        }
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.1f, 0.4f, 0.1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        handleInput()

        batch.begin()
        drawCards()
        drawUI()
        batch.end()
    }

    private fun handleInput() {
        when (state) {
            GameState.SHOPPING -> {
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                    shop.buyItem(shop.itemList[0].name, wallet)
                    message = "1 = ${shop.itemList[0].name}  2 = ${shop.itemList[1].name}  3 = ${shop.itemList[2].name}  |  SPACE to skip"
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
                    shop.buyItem(shop.itemList[1].name, wallet)
                    message = "1 = ${shop.itemList[0].name}  2 = ${shop.itemList[1].name}  3 = ${shop.itemList[2].name}  |  SPACE to skip"
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
                    shop.buyItem(shop.itemList[2].name, wallet)
                    message = "1 = ${shop.itemList[0].name}  2 = ${shop.itemList[1].name}  3 = ${shop.itemList[2].name}  |  SPACE to skip"
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    state = GameState.BETTING
                    betInput = ""
                    message = "Enter bet amount and press Enter"
                }
            }
            GameState.BETTING -> {
                val digitKeys = mapOf(
                    Input.Keys.NUM_0 to "0", Input.Keys.NUM_1 to "1", Input.Keys.NUM_2 to "2",
                    Input.Keys.NUM_3 to "3", Input.Keys.NUM_4 to "4", Input.Keys.NUM_5 to "5",
                    Input.Keys.NUM_6 to "6", Input.Keys.NUM_7 to "7", Input.Keys.NUM_8 to "8",
                    Input.Keys.NUM_9 to "9"
                )
                for ((key, digit) in digitKeys) {
                    if (Gdx.input.isKeyJustPressed(key)) betInput += digit
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && betInput.isNotEmpty()) {
                    betInput = betInput.dropLast(1)
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && betInput.isNotEmpty()) {
                    val amount = betInput.toIntOrNull()
                    if (amount != null) placeBet(amount) else message = "Invalid input"
                    betInput = ""
                }
            }
            GameState.ITEM_USE -> {
                if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) {
                    shop.activateItems()
                    state = GameState.PLAYER_TURN
                    message = "H = Hit  |  S = Stand"
                    if (shop.checkMyItem("hint")) {
                        message += "  |  [Hint] Dealer hidden: ${dealer.hand[1]}"
                    }
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
                    state = GameState.PLAYER_TURN
                    message = "H = Hit  |  S = Stand"
                }
            }
            GameState.PLAYER_TURN -> {
                if (Gdx.input.isKeyJustPressed(Input.Keys.H)) playerHit()
                if (Gdx.input.isKeyJustPressed(Input.Keys.S)) playerStand()
            }
            GameState.ROUND_OVER -> {
                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) nextRound()
            }
            GameState.STAGE_CLEAR -> {
                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) startNewRound()
            }
            GameState.GAME_OVER, GameState.ALL_CLEAR -> {
                if (Gdx.input.isKeyJustPressed(Input.Keys.R)) restartGame()
            }
        }
    }

    private fun placeBet(amount: Int) {
        val minBet = targetBalance / 10
        if (amount < minBet) {
            message = "Minimum bet is $minBet!"
            return
        }
        if (!wallet.canAfford(amount)) {
            message = "Not enough funds!"
            return
        }
        bet = amount
        wallet.placeBet(bet)
        val newDeck = Deck()
        player.drawCard(newDeck)
        dealer.drawCard(newDeck)
        player.drawCard(newDeck)
        dealer.drawCard(newDeck)
        if (shop.hasItems()) {
            state = GameState.ITEM_USE
            message = "Use items? ${shop.ownedItems}  Y = Yes  N = No"
        } else {
            state = GameState.PLAYER_TURN
            message = "H = Hit  |  S = Stand"
        }
    }

    private fun playerHit() {
        val newDeck = Deck()
        player.drawCard(newDeck)
        if (player.score > 21) endRound()
    }

    private fun playerStand() {
        val newDeck = Deck()
        while (dealer.score < 17) dealer.drawCard(newDeck)
        endRound()
    }

    private fun endRound() {
        val roundResult = result.determine(player.score, dealer.score, player.hand.size, dealer.hand.size)
        result.applyResult(roundResult, bet, wallet, shop)
        message = when (roundResult) {
            RoundResult.PLAYER_WIN -> "You win! +${bet * 2}"
            RoundResult.DEALER_WIN -> "Dealer wins! -$bet"
            RoundResult.TIE -> "Tie! Bet returned"
            RoundResult.PLAYER_BUST -> if (shop.checkMyItem("insurance")) "Bust! Insurance - bet returned" else "Bust! -$bet"
            RoundResult.BLACKJACK -> "Blackjack!"
        }
        message += "  |  SPACE = next round"
        state = GameState.ROUND_OVER
    }

    private fun nextRound() {
        shop.clearItems()
        round++
        if (round > roundsPerStage) {
            if (wallet.getCurrentBalance() >= targetBalance) {
                stage++
                round = 1
                targetBalance *= 2
                if (stage > totalStages) {
                    state = GameState.ALL_CLEAR
                    message = "YOU WIN!  R = Restart"
                    return
                }
                state = GameState.STAGE_CLEAR
                message = "Stage Clear!  SPACE = next stage"
            } else {
                state = GameState.GAME_OVER
                message = "Game Over  R = Restart"
            }
            return
        }
        startNewRound()
    }

    private fun restartGame() {
        stage = 1
        round = 1
        targetBalance = 2000
        shop.clearItems()
        shop.ownedItems.clear()
        wallet.initialize(1000)
        startNewRound()
    }

    private fun drawCards() {
        if (state == GameState.SHOPPING || state == GameState.BETTING) return

        val cardWidth = 80f
        val cardHeight = 110f
        val spacing = 90f
        val centerX = screenW / 2f

        val dealerTotalWidth = cardWidth + (dealer.hand.size - 1) * spacing
        val dealerStartX = centerX - dealerTotalWidth / 2f
        for (i in dealer.hand.indices) {
            val x = dealerStartX + i * spacing
            val y = 430f
            val hideCard = i == 1 && (state == GameState.ITEM_USE ||
                (state == GameState.PLAYER_TURN && !shop.checkMyItem("hint")))
            if (hideCard) {
                batch.draw(cardBack, x, y, cardWidth, cardHeight)
            } else {
                batch.draw(getCardTexture(dealer.hand[i]), x, y, cardWidth, cardHeight)
            }
        }

        val playerTotalWidth = cardWidth + (player.hand.size - 1) * spacing
        val playerStartX = centerX - playerTotalWidth / 2f
        for (i in player.hand.indices) {
            val x = playerStartX + i * spacing
            val y = 180f
            batch.draw(getCardTexture(player.hand[i]), x, y, cardWidth, cardHeight)
        }
    }

    private fun drawUI() {
        font.color = Color.WHITE
        font.data.setScale(1.5f)

        val minBet = targetBalance / 10
        font.draw(batch, "Stage: $stage / $totalStages   Round: $round / $roundsPerStage", 50f, 700f)
        font.draw(batch, "Balance: ${wallet.getCurrentBalance()}   Target: $targetBalance   Min Bet: $minBet", 50f, 670f)
        font.draw(batch, "Bet: $bet", 50f, 640f)

        if (state == GameState.SHOPPING) {
            font.data.setScale(1.8f)
            font.color = Color.YELLOW
            font.draw(batch, "=== SHOP ===", screenW / 2f - 100f, 580f)
            font.data.setScale(1.3f)
            font.color = Color.WHITE
            var itemY = 530f
            for (item in shop.itemList) {
                val owned = if (shop.ownedItems.contains(item.name)) " [OWNED]" else ""
                font.draw(batch, "${item.name} - ${item.price}  |  ${item.description}$owned", screenW / 2f - 300f, itemY)
                itemY -= 45f
            }
            font.draw(batch, "Owned: ${shop.ownedItems}", screenW / 2f - 300f, itemY - 10f)
            font.color = Color.WHITE
            font.draw(batch, "> ${shopInput}_", screenW / 2f - 300f, 200f)
        } else {
            val dealerScoreText = if (state == GameState.PLAYER_TURN || state == GameState.ITEM_USE) {
                val visibleScore = dealer.hand[0].rank.value
                "$visibleScore + ?"
            } else {
                dealer.score.toString()
            }
            font.draw(batch, "Dealer score: $dealerScoreText", screenW / 2f - 80f, 560f)
            font.draw(batch, "Your score: ${player.score}", screenW / 2f - 80f, 310f)

            if (state == GameState.BETTING) {
                font.draw(batch, "> ${betInput}_", screenW / 2f - 60f, 120f)
            }
        }

        font.data.setScale(1.3f)
        font.color = Color.YELLOW
        font.draw(batch, message, 50f, 80f)
    }

    override fun dispose() {
        batch.dispose()
        font.dispose()
        for (texture in cardTextures.values) texture.dispose()
        cardBack.dispose()
    }
}
