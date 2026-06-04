package com.oop.blackjack.screen

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import com.oop.blackjack.BlackjackGame
import com.oop.blackjack.UiFactory
import com.oop.blackjack.model.*

class GameScreen(private val game: BlackjackGame) : Screen {

    // ── Layout constants ──────────────────────────────────────────────────────
    companion object {
        const val W        = 1280f
        const val H        = 720f
        const val CARD_W   = 82f
        const val CARD_H   = 115f
        const val DEALER_Y = 460f
        const val PLAYER_Y = 170f
        const val PAD      = 70f        // table wood border thickness
        const val DEAL_SPD = 1.6f       // t units per second for card animation
    }

    // ── Phases where the dealer's hole card stays hidden ─────────────────────
    private val HOLE_HIDDEN = setOf(
        GamePhase.DEALING, GamePhase.PLAYER_TURN, GamePhase.ITEM_PROMPT,
        GamePhase.PEEK, GamePhase.BETTING, GamePhase.SHOP_PROMPT, GamePhase.SHOP
    )

    private val viewport = FitViewport(W, H)
    private val cam      get() = viewport.camera as OrthographicCamera
    private val stage    = Stage(viewport, game.batch)
    private val skin     = UiFactory.buildSkin(game)

    // ── Game state ────────────────────────────────────────────────────────────
    private val gs = GameState()

    // ── Card deal animation ───────────────────────────────────────────────────
    /** sx/sy = start position, tx/ty = target position, t drives easing (staggered start via negative t) */
    private data class CardAnim(
        val sx: Float, val sy: Float,
        val tx: Float, val ty: Float,
        var t: Float = 0f
    ) {
        var x: Float = sx
        var y: Float = sy
    }
    private val anims      = mutableListOf<CardAnim>()
    private var dealDone   = false

    // ── Timers ────────────────────────────────────────────────────────────────
    private var dealerTimer  = 0f
    private val DEALER_DELAY = 0.90f

    private var peekTimer   = 0f
    private val PEEK_HOLD   = 2.8f

    private var resultTimer  = 0f
    private val RESULT_HOLD  = 99f   // shown until player clicks Continue

    // ── Result display ────────────────────────────────────────────────────────
    private var resultText     = ""
    private var resultPositive = true

    // ── Bet input ─────────────────────────────────────────────────────────────
    private var currentBetInput = 0
    private var betLabel: Label? = null

    // ── Flash / pulse ─────────────────────────────────────────────────────────
    private var time = 0f

    // ═════════════════════════════════════════════════════════════════════════
    //  INIT
    // ═════════════════════════════════════════════════════════════════════════

    init {
        startBettingPhase()
        Gdx.input.inputProcessor = stage
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  PHASE TRANSITIONS
    // ═════════════════════════════════════════════════════════════════════════

    private fun startBettingPhase() {
        gs.startNewRound()
        currentBetInput = gs.minBet
        // Stage 2+ round 1: offer shop first
        if (gs.stage > 1 && gs.round == 1) {
            gs.phase = GamePhase.SHOP_PROMPT
            buildShopPromptUI()
        } else {
            gs.phase = GamePhase.BETTING
            buildBetUI()
        }
    }

    private fun openShop() {
        gs.phase = GamePhase.SHOP
        buildShopUI()
    }

    private fun startDealing() {
        gs.phase = GamePhase.DEALING
        gs.deal()
        dealDone = false
        anims.clear()

        val deckX = W - PAD - 50f
        val deckY = H - PAD - 30f
        val pSize = gs.player.hand.size   // always 2
        val dSize = gs.dealer.hand.size   // always 2

        for (i in 0 until pSize) {
            val tx = cardStartX(i, pSize)
            anims.add(CardAnim(deckX, deckY, tx, PLAYER_Y, t = -(i * 0.20f)))
        }
        for (i in 0 until dSize) {
            val tx = cardStartX(i, dSize)
            anims.add(CardAnim(deckX, deckY, tx, DEALER_Y, t = -((pSize + i) * 0.20f)))
        }
        stage.clear()
    }

    private fun afterDealing() {
        when {
            gs.player.isBlackjack -> triggerDealerReveal()   // immediate dealer reveal
            gs.shop.hasOwned()    -> { gs.phase = GamePhase.ITEM_PROMPT; buildItemPromptUI() }
            else                  -> startPlayerTurn()
        }
    }

    private fun startPlayerTurn() {
        gs.phase = GamePhase.PLAYER_TURN
        buildPlayerTurnUI()
    }

    private fun playerHit() {
        gs.player.draw(gs.deck)
        buildPlayerTurnUI()   // rebuild to keep buttons live
        if (gs.player.isBust || gs.player.score == 21) {
            triggerDealerReveal()
        }
    }

    private fun playerStand() {
        triggerDealerReveal()
    }

    private fun triggerDealerReveal() {
        gs.phase = GamePhase.DEALER_TURN
        dealerTimer = 0f
        stage.clear()
    }

    private fun finishRound() {
        val result = ResultResolver.determine(gs.player, gs.dealer)
        ResultResolver.apply(result, gs.currentBet, gs.wallet, gs.shop)
        gs.shop.clearActive()
        gs.lastResult = result

        resultText     = ResultResolver.displayText(result, gs.shop)
        resultPositive = ResultResolver.isPlayerPositive(result)
        resultTimer    = RESULT_HOLD
        gs.phase       = GamePhase.ROUND_RESULT
        buildResultUI()
    }

    private fun afterResult() {
        if (!gs.advanceRound()) {
            // Stage over — check target
            if (gs.wallet.balance >= gs.targetBalance) {
                if (!gs.advanceStage()) {
                    navigateTo(true)   // all 20 stages cleared
                } else {
                    gs.phase = GamePhase.STAGE_CLEAR
                    buildStageClearUI()
                }
            } else {
                navigateTo(false)   // failed to reach target
            }
        } else {
            // More rounds remain in this stage
            if (!gs.wallet.canAfford(gs.minBet)) { navigateTo(false); return }
            startBettingPhase()
        }
    }

    private fun navigateTo(won: Boolean) {
        game.setScreen(GameOverScreen(game, won, gs.wallet.balance, gs.stage))
        dispose()
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  UI BUILDERS
    // ═════════════════════════════════════════════════════════════════════════

    /** Shared bet panel (used both fresh and after skipping shop) */
    private fun buildBetUI() {
        stage.clear()
        val root = Table().apply { setFillParent(true); bottom().padBottom(28f) }

        betLabel = Label("\$${currentBetInput}", skin, "gold").apply { setAlignment(Align.center) }
        val titleLbl  = Label("PLACE YOUR BET", skin, "medium")
        val infoLbl   = Label("Min \$${gs.minBet}   ·   Balance \$${gs.wallet.balance}", skin)

        val minusBtn  = TextButton("  −  ", skin, "small")
        val plusBtn   = TextButton("  +  ", skin, "small")
        val dealBtn   = TextButton("   DEAL   ", skin, "green")

        minusBtn.addListener(object : ClickListener() {
            override fun clicked(e: InputEvent, x: Float, y: Float) {
                val step = if (currentBetInput > gs.minBet * 5) 100 else 50
                currentBetInput = maxOf(gs.minBet, currentBetInput - step)
                betLabel?.setText("\$$currentBetInput")
            }
        })
        plusBtn.addListener(object : ClickListener() {
            override fun clicked(e: InputEvent, x: Float, y: Float) {
                val step = if (currentBetInput >= gs.minBet * 5) 100 else 50
                currentBetInput = minOf(gs.wallet.balance, currentBetInput + step)
                betLabel?.setText("\$$currentBetInput")
            }
        })
        dealBtn.addListener(object : ClickListener() {
            override fun clicked(e: InputEvent, x: Float, y: Float) {
                if (!gs.wallet.canAfford(currentBetInput)) return
                gs.wallet.placeBet(currentBetInput)
                gs.currentBet = currentBetInput
                startDealing()
            }
        })

        val betRow = Table()
        betRow.add(minusBtn).padRight(18f)
        betRow.add(betLabel!!).width(190f)
        betRow.add(plusBtn).padLeft(18f)

        root.add(titleLbl).padBottom(6f).row()
        root.add(infoLbl).padBottom(14f).row()
        root.add(betRow).padBottom(18f).row()
        root.add(dealBtn).width(230f).height(66f)
        stage.addActor(root)
    }

    private fun buildShopPromptUI() {
        stage.clear()
        val root = Table().apply { setFillParent(true); bottom().padBottom(28f) }
        val lbl    = Label("Visit the shop before Round 1?", skin, "gold").apply { setAlignment(Align.center) }
        val yesBtn = TextButton("   SHOP   ", skin, "gold")
        val noBtn  = TextButton("   SKIP   ", skin, "red")

        yesBtn.addListener(object : ClickListener() {
            override fun clicked(e: InputEvent, x: Float, y: Float) { openShop() }
        })
        noBtn.addListener(object : ClickListener() {
            override fun clicked(e: InputEvent, x: Float, y: Float) { gs.phase = GamePhase.BETTING; buildBetUI() }
        })

        root.add(lbl).colspan(2).padBottom(18f).row()
        root.add(yesBtn).width(190f).height(62f).padRight(20f)
        root.add(noBtn).width(190f).height(62f)
        stage.addActor(root)
    }

    private fun buildShopUI() {
        stage.clear()
        val root  = Table().apply { setFillParent(true); center() }
        val panel = Table()
        panel.background = skin.newDrawable("pixel", Color(0.04f, 0.04f, 0.06f, 0.92f))
        panel.pad(28f)

        panel.add(Label("★   SHOP   ★", skin, "title")).colspan(4).padBottom(22f).row()

        for (item in gs.shop.catalog) {
            val owned  = gs.shop.isOwned(item.id) || gs.shop.isActive(item.id)
            val canBuy = !owned && gs.wallet.canAfford(item.price)

            panel.add(Label(item.name, skin, "gold")).left().padRight(18f)
            panel.add(Label(item.description, skin)).left().expandX().padRight(18f)
            panel.add(Label("\$${item.price}", skin)).padRight(14f)

            val buyBtn = TextButton(if (owned) " OWNED " else "  BUY  ", skin, if (canBuy) "gold" else "small")
            buyBtn.isDisabled = !canBuy
            buyBtn.addListener(object : ClickListener() {
                override fun clicked(e: InputEvent, x: Float, y: Float) {
                    if (buyBtn.isDisabled) return
                    gs.shop.buy(item.id, gs.wallet)
                    buildShopUI()   // refresh
                }
            })
            panel.add(buyBtn).width(130f).height(52f).padBottom(14f).row()
        }

        panel.add(Label("Balance:  \$${gs.wallet.balance}", skin, "medium"))
             .colspan(4).padTop(14f).padBottom(18f).row()

        val closeBtn = TextButton("   CLOSE   ", skin, "red")
        closeBtn.addListener(object : ClickListener() {
            override fun clicked(e: InputEvent, x: Float, y: Float) { gs.phase = GamePhase.BETTING; buildBetUI() }
        })
        panel.add(closeBtn).colspan(4).width(210f).height(60f)

        root.add(panel).width(800f)
        stage.addActor(root)
    }

    private fun buildItemPromptUI() {
        stage.clear()
        val root    = Table().apply { setFillParent(true); bottom().padBottom(28f) }
        val names   = gs.shop.owned.joinToString("  ·  ")
        val lbl     = Label("Activate items now?\n[ $names ]", skin, "gold").apply { setAlignment(Align.center) }
        val yesBtn  = TextButton("   ACTIVATE   ", skin, "gold")
        val noBtn   = TextButton("   SKIP   ",     skin, "small")

        yesBtn.addListener(object : ClickListener() {
            override fun clicked(e: InputEvent, x: Float, y: Float) {
                gs.shop.activateOwned()
                if (gs.shop.isActive("hint")) {
                    gs.phase = GamePhase.PEEK; peekTimer = PEEK_HOLD; buildPeekUI()
                } else {
                    startPlayerTurn()
                }
            }
        })
        noBtn.addListener(object : ClickListener() {
            override fun clicked(e: InputEvent, x: Float, y: Float) { startPlayerTurn() }
        })

        root.add(lbl).colspan(2).padBottom(18f).row()
        root.add(yesBtn).width(210f).height(62f).padRight(20f)
        root.add(noBtn).width(165f).height(62f)
        stage.addActor(root)
    }

    private fun buildPeekUI() {
        stage.clear()
        val root = Table().apply { setFillParent(true); bottom().padBottom(40f) }
        val card = gs.dealer.hand[1]
        root.add(
            Label("[ PEEK ]   Dealer's hole card:  $card", skin, "gold")
                .apply { setAlignment(Align.center) }
        )
        stage.addActor(root)
    }

    private fun buildPlayerTurnUI() {
        stage.clear()
        val root     = Table().apply { setFillParent(true); bottom().padBottom(28f) }
        val hitBtn   = TextButton("   HIT   ",   skin, "green")
        val standBtn = TextButton("   STAND   ", skin, "red")

        hitBtn.addListener(object : ClickListener() {
            override fun clicked(e: InputEvent, x: Float, y: Float) { playerHit() }
        })
        standBtn.addListener(object : ClickListener() {
            override fun clicked(e: InputEvent, x: Float, y: Float) { playerStand() }
        })

        root.add(hitBtn).width(200f).height(66f).padRight(26f)
        root.add(standBtn).width(200f).height(66f)
        stage.addActor(root)
    }

    private fun buildResultUI() {
        stage.clear()
        val root    = Table().apply { setFillParent(true); bottom().padBottom(28f) }
        val contBtn = TextButton("   CONTINUE   ", skin, "green")
        contBtn.addListener(object : ClickListener() {
            override fun clicked(e: InputEvent, x: Float, y: Float) { afterResult() }
        })
        root.add(contBtn).width(260f).height(66f)
        stage.addActor(root)
    }

    private fun buildStageClearUI() {
        stage.clear()
        val root    = Table().apply { setFillParent(true); bottom().padBottom(28f) }
        val nextBtn = TextButton("   NEXT STAGE   ", skin, "green")
        nextBtn.addListener(object : ClickListener() {
            override fun clicked(e: InputEvent, x: Float, y: Float) { startBettingPhase() }
        })
        root.add(nextBtn).width(270f).height(66f)
        stage.addActor(root)
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  UPDATE
    // ═════════════════════════════════════════════════════════════════════════

    private fun update(delta: Float) {
        time += delta

        // ── Card deal animation ───────────────────────────────────────────
        if (gs.phase == GamePhase.DEALING) {
            for (a in anims) {
                a.t += delta * DEAL_SPD
                if (a.t < 0f) continue
                val p      = MathUtils.clamp(a.t, 0f, 1f)
                val smooth = p * p * (3f - 2f * p)   // smoothstep
                a.x = MathUtils.lerp(a.sx, a.tx, smooth)
                a.y = MathUtils.lerp(a.sy, a.ty, smooth)
            }
            // Done when every card has t >= 1
            if (!dealDone && anims.all { it.t >= 1f }) {
                dealDone = true
                // Snap to exact targets
                anims.forEach { it.x = it.tx; it.y = it.ty }
                afterDealing()
            }
        }

        // ── Dealer auto-play ─────────────────────────────────────────────
        if (gs.phase == GamePhase.DEALER_TURN) {
            dealerTimer += delta
            if (dealerTimer >= DEALER_DELAY) {
                dealerTimer = 0f
                if (gs.dealer.shouldDraw()) gs.dealer.draw(gs.deck) else finishRound()
            }
        }

        // ── Peek timeout ─────────────────────────────────────────────────
        if (gs.phase == GamePhase.PEEK) {
            peekTimer -= delta
            if (peekTimer <= 0f) startPlayerTurn()
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RENDER
    // ═════════════════════════════════════════════════════════════════════════

    override fun render(delta: Float) {
        update(delta)

        Gdx.gl.glClearColor(0.03f, 0.05f, 0.03f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        cam.update()
        game.shape.projectionMatrix = cam.combined
        game.batch.projectionMatrix  = cam.combined

        // ── 1. Filled shapes: table, card bodies, chips, overlays ────────
        game.shape.begin(ShapeRenderer.ShapeType.Filled)
        drawTable()
        drawAllCardBodies()
        if (gs.currentBet > 0) drawChips(W / 2f - 20f, H / 2f - 55f, gs.currentBet)
        if (gs.phase == GamePhase.ROUND_RESULT) drawDimOverlay(0.72f)
        if (gs.phase == GamePhase.STAGE_CLEAR)  drawDimOverlay(0.80f)
        game.shape.end()

        // ── 2. Line shapes: card borders, table deco ─────────────────────
        game.shape.begin(ShapeRenderer.ShapeType.Line)
        drawAllCardBorders()
        drawTableDeco()
        game.shape.end()

        // ── 3. SpriteBatch: card text, HUD, result overlay text ──────────
        game.batch.begin()
        drawAllCardText()
        drawHUD()
        if (gs.phase == GamePhase.ROUND_RESULT) drawResultText()
        if (gs.phase == GamePhase.STAGE_CLEAR)  drawStageClearText()
        game.batch.end()

        // ── 4. Scene2D stage ──────────────────────────────────────────────
        stage.act(delta)
        stage.draw()
    }

    // ─── Table ────────────────────────────────────────────────────────────────

    private fun drawTable() {
        // Wood border
        game.shape.setColor(0.25f, 0.14f, 0.04f, 1f)
        game.shape.rect(0f, 0f, W, H)

        // Felt surface
        game.shape.setColor(0.09f, 0.30f, 0.11f, 1f)
        game.shape.rect(PAD, PAD, W - PAD * 2, H - PAD * 2)

        // Center highlight ellipse
        game.shape.setColor(0.11f, 0.36f, 0.13f, 1f)
        game.shape.ellipse(200f, 130f, 880f, 460f)

        // Top HUD bar
        game.shape.setColor(0f, 0f, 0f, 0.60f)
        game.shape.rect(PAD, H - PAD - 50f, W - PAD * 2, 50f)

        // Deck pile shadow + back cards (top-right)
        val dx = W - PAD - 80f; val dy = H - PAD - 110f
        game.shape.setColor(0f, 0f, 0f, 0.5f)
        game.shape.rect(dx + 6f, dy - 6f, 68f, 96f)
        game.shape.setColor(0.10f, 0.18f, 0.55f, 1f)
        game.shape.rect(dx + 4f, dy - 4f, 68f, 96f)
        game.shape.setColor(0.14f, 0.22f, 0.65f, 1f)
        game.shape.rect(dx,      dy,      68f, 96f)

        // Bet zone plate
        game.shape.setColor(0.07f, 0.22f, 0.08f, 0.65f)
        game.shape.ellipse(W / 2f - 90f, H / 2f - 65f, 180f, 78f)
    }

    private fun drawTableDeco() {
        // Inner felt border arc
        game.shape.setColor(0.50f, 0.36f, 0.10f, 0.55f)
        game.shape.rect(PAD + 10f, PAD + 10f, W - PAD * 2 - 20f, H - PAD * 2 - 20f)

        // Casino text line
        game.shape.setColor(0.50f, 0.36f, 0.10f, 0.30f)
        game.shape.line(PAD + 20f, H / 2f, W - PAD - 20f, H / 2f)
    }

    // ─── Card position helpers ────────────────────────────────────────────────

    private fun cardStartX(index: Int, total: Int): Float {
        val span = total * (CARD_W + 10f) - 10f
        return W / 2f - span / 2f + index * (CARD_W + 10f)
    }

    private fun playerCardPos(i: Int): Pair<Float, Float> =
        if (gs.phase == GamePhase.DEALING && i < anims.size)
            Pair(anims[i].x, anims[i].y)
        else
            Pair(cardStartX(i, gs.player.hand.size), PLAYER_Y)

    private fun dealerCardPos(i: Int): Pair<Float, Float> {
        val ai = i + gs.player.hand.size
        return if (gs.phase == GamePhase.DEALING && ai < anims.size)
            Pair(anims[ai].x, anims[ai].y)
        else
            Pair(cardStartX(i, gs.dealer.hand.size), DEALER_Y)
    }

    private fun isDealerHoleHidden() = gs.phase in HOLE_HIDDEN

    // ─── Card bodies (filled rects) ───────────────────────────────────────────

    private fun drawAllCardBodies() {
        for (i in gs.dealer.hand.indices) {
            val (x, y) = dealerCardPos(i)
            drawCardBody(x, y, i == 1 && isDealerHoleHidden())
        }
        for (i in gs.player.hand.indices) {
            val (x, y) = playerCardPos(i)
            drawCardBody(x, y, false)
        }
    }

    private fun drawCardBody(x: Float, y: Float, faceDown: Boolean) {
        // Drop shadow
        game.shape.setColor(0f, 0f, 0f, 0.45f)
        game.shape.rect(x + 5f, y - 5f, CARD_W, CARD_H)

        if (faceDown) {
            // Card back — dark blue with stripe pattern
            game.shape.setColor(0.13f, 0.20f, 0.60f, 1f)
            game.shape.rect(x, y, CARD_W, CARD_H)
            game.shape.setColor(0.18f, 0.28f, 0.78f, 0.55f)
            var px = x + 5f
            while (px < x + CARD_W - 5f) {
                game.shape.rect(px, y + 5f, 5f, CARD_H - 10f)
                px += 12f
            }
        } else {
            // Card face — warm white
            game.shape.setColor(0.97f, 0.96f, 0.93f, 1f)
            game.shape.rect(x, y, CARD_W, CARD_H)
        }
    }

    // ─── Card borders ─────────────────────────────────────────────────────────

    private fun drawAllCardBorders() {
        val playerBust = gs.player.isBust &&
                gs.phase in setOf(GamePhase.DEALER_TURN, GamePhase.ROUND_RESULT)
        val dealerBust = gs.dealer.isBust && gs.phase == GamePhase.ROUND_RESULT

        for (i in gs.dealer.hand.indices) {
            val (x, y) = dealerCardPos(i)
            if (dealerBust) game.shape.setColor(1f, 0.2f, 0.2f, 0.9f)
            else            game.shape.setColor(0.45f, 0.45f, 0.45f, 0.8f)
            game.shape.rect(x, y, CARD_W, CARD_H)
            // Inner accent line
            game.shape.setColor(0.65f, 0.65f, 0.65f, 0.4f)
            game.shape.rect(x + 3f, y + 3f, CARD_W - 6f, CARD_H - 6f)
        }
        for (i in gs.player.hand.indices) {
            val (x, y) = playerCardPos(i)
            if (playerBust) game.shape.setColor(1f, 0.2f, 0.2f, 0.9f)
            else            game.shape.setColor(0.45f, 0.45f, 0.45f, 0.8f)
            game.shape.rect(x, y, CARD_W, CARD_H)
            game.shape.setColor(0.65f, 0.65f, 0.65f, 0.4f)
            game.shape.rect(x + 3f, y + 3f, CARD_W - 6f, CARD_H - 6f)
        }
    }

    // ─── Card text ────────────────────────────────────────────────────────────

    private fun drawAllCardText() {
        val showHole = !isDealerHoleHidden()
        for (i in gs.dealer.hand.indices) {
            if (i == 1 && !showHole) continue
            val (x, y) = dealerCardPos(i)
            drawCardFace(gs.dealer.hand[i], x, y)
        }
        for (i in gs.player.hand.indices) {
            val (x, y) = playerCardPos(i)
            drawCardFace(gs.player.hand[i], x, y)
        }
    }

    private fun drawCardFace(card: Card, x: Float, y: Float) {
        val red = card.suit.isRed
        val fg  = if (red) Color(0.80f, 0.07f, 0.07f, 1f) else Color(0.08f, 0.08f, 0.08f, 1f)

        // Top-left rank
        game.fontCard.color = fg
        game.fontCard.draw(game.batch, card.rank.display, x + 7f, y + CARD_H - 7f)

        // Center suit symbol (larger)
        game.fontMd.color = fg
        game.fontMd.draw(game.batch, card.suit.symbol, x + CARD_W / 2f - 12f, y + CARD_H / 2f + 16f)

        // Bottom-right rank (small, inverted corner)
        game.fontSm.color = fg
        game.fontSm.draw(game.batch, card.rank.display, x + CARD_W - 22f, y + 24f)
    }

    // ─── Chip stack visuals ───────────────────────────────────────────────────

    private fun drawChips(cx: Float, cy: Float, amount: Int) {
        val chipColors = arrayOf(
            Color(0.85f, 0.10f, 0.10f, 1f),   // red   ($5)
            Color(0.10f, 0.28f, 0.85f, 1f),   // blue  ($10)
            Color(0.12f, 0.12f, 0.12f, 1f),   // black ($25)
            Color(0.70f, 0.58f, 0.08f, 1f)    // gold  ($100)
        )
        val stacks = minOf(amount / 50 + 1, 10)
        for (i in 0 until stacks) {
            val col = chipColors[i % chipColors.size]
            // Shadow
            game.shape.setColor(col.r * 0.5f, col.g * 0.5f, col.b * 0.5f, 1f)
            game.shape.ellipse(cx + i * 7f - 2f, cy + i * 4f - 2f, 48f, 18f)
            // Chip
            game.shape.setColor(col)
            game.shape.ellipse(cx + i * 7f, cy + i * 4f, 48f, 18f)
            // Shine
            game.shape.setColor(1f, 1f, 1f, 0.18f)
            game.shape.ellipse(cx + i * 7f + 6f, cy + i * 4f + 5f, 22f, 8f)
        }
    }

    // ─── HUD ─────────────────────────────────────────────────────────────────

    private fun drawHUD() {
        val hudY = H - PAD - 6f

        // Stage / Round / Balance in gold
        game.fontMd.color = Color(1f, 0.84f, 0f, 1f)
        game.fontMd.draw(game.batch, "STAGE ${gs.stage} / $TOTAL_STAGES",      PAD + 12f, hudY)
        game.fontMd.draw(game.batch, "ROUND ${gs.round} / $ROUNDS_PER_STAGE",  W / 2f - 90f, hudY)
        game.fontMd.draw(game.batch, "BALANCE: \$${gs.wallet.balance}",         W - 370f, hudY)

        // Section labels
        game.fontSm.color = Color(0.75f, 0.75f, 0.75f, 0.9f)
        game.fontSm.draw(game.batch, "D E A L E R", PAD + 16f, DEALER_Y + CARD_H + 26f)
        game.fontSm.draw(game.batch, "P L A Y E R", PAD + 16f, PLAYER_Y + CARD_H + 26f)

        // Scores
        drawScore(gs.player.score, gs.player.isBust, PLAYER_Y - 8f, alwaysShow = true)
        val showDealerFull = gs.phase !in HOLE_HIDDEN
        if (showDealerFull) {
            drawScore(gs.dealer.score, gs.dealer.isBust, DEALER_Y - 8f, alwaysShow = true)
        } else if (gs.dealer.hand.isNotEmpty()) {
            game.fontMd.color = Color(0.65f, 0.65f, 0.65f, 0.9f)
            game.fontMd.draw(game.batch, "Shows: ${gs.dealer.hand[0].rank.baseValue}", PAD + 16f, DEALER_Y - 8f)
        }

        // Bet display under chips
        if (gs.currentBet > 0) {
            game.fontSm.color = Color(1f, 0.84f, 0f, 0.85f)
            game.fontSm.draw(game.batch, "Bet: \$${gs.currentBet}", W / 2f - 55f, H / 2f + 8f)
        }

        // Target at bottom-left
        game.fontSm.color = Color(0.55f, 0.88f, 0.58f, 0.9f)
        game.fontSm.draw(game.batch, "Target: \$${gs.targetBalance}", PAD + 16f, PAD + 40f)

        // Active item badges
        var badgeY = PAD + 40f
        if (gs.shop.isActive("hint"))      { drawBadge("PEEK",   Color(0.95f, 0.75f, 0.10f, 1f), badgeY); badgeY += 28f }
        if (gs.shop.isActive("insurance")) { drawBadge("SHIELD", Color(0.20f, 0.60f, 0.95f, 1f), badgeY); badgeY += 28f }
        if (gs.shop.isActive("double"))    { drawBadge("2×WIN",  Color(0.90f, 0.20f, 0.85f, 1f), badgeY) }
    }

    private fun drawScore(score: Int, bust: Boolean, y: Float, alwaysShow: Boolean) {
        if (!alwaysShow && score == 0) return
        game.fontMd.color = when {
            bust       -> Color(1f, 0.28f, 0.28f, 1f)
            score == 21 -> Color(1f, 0.84f, 0f, 1f)
            else        -> Color(0.92f, 0.92f, 0.92f, 1f)
        }
        val txt = if (bust) "BUST ($score)" else "$score"
        game.fontMd.draw(game.batch, txt, PAD + 16f, y)
    }

    private fun drawBadge(text: String, color: Color, y: Float) {
        game.fontSm.color = color
        game.fontSm.draw(game.batch, "[$text]", W - PAD - 150f, y)
    }

    // ─── Overlays ─────────────────────────────────────────────────────────────

    private fun drawDimOverlay(alpha: Float) {
        game.shape.setColor(0f, 0f, 0f, alpha)
        game.shape.rect(PAD, PAD, W - PAD * 2, H - PAD * 2)
    }

    private fun drawResultText() {
        // Glow effect: draw text multiple times with color bleed
        val col    = if (resultPositive) Color(0.25f, 1f, 0.35f, 1f) else Color(1f, 0.28f, 0.28f, 1f)
        val glow   = Color(col.r, col.g, col.b, 0.28f)
        val pulse  = (MathUtils.sin(time * 4f) * 0.12f + 0.88f)

        game.fontLg.color = glow
        for (offset in listOf(-3f, 3f)) {
            game.fontLg.draw(game.batch, resultText, offset, H / 2f + 55f, W, Align.center, false)
        }
        game.fontLg.color = Color(col.r * pulse, col.g * pulse, col.b * pulse, 1f)
        game.fontLg.draw(game.batch, resultText, 0f, H / 2f + 55f, W, Align.center, false)

        // Sub-line: balance
        game.fontMd.color = Color(1f, 0.84f, 0f, 0.95f)
        val balText = "Balance: \$${gs.wallet.balance}   ·   Stage ${gs.stage}  Round ${gs.round}"
        game.fontMd.draw(game.batch, balText, 0f, H / 2f - 18f, W, Align.center, false)
    }

    private fun drawStageClearText() {
        val pulse = MathUtils.sin(time * 3.5f) * 0.14f + 0.86f
        game.fontLg.color = Color(1f * pulse, 0.84f * pulse, 0f, 1f)
        game.fontLg.draw(game.batch, "STAGE ${gs.stage - 1} CLEAR!", 0f, H / 2f + 70f, W, Align.center, false)
        game.fontMd.color = Color(0.78f, 0.96f, 0.78f, 1f)
        game.fontMd.draw(game.batch,
            "Balance: \$${gs.wallet.balance}      Next target: \$${gs.targetBalance}",
            0f, H / 2f - 14f, W, Align.center, false)
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ═════════════════════════════════════════════════════════════════════════

    override fun show() {
        viewport.update(Gdx.graphics.width, Gdx.graphics.height, true)
        Gdx.input.inputProcessor = stage
    }

    override fun resize(width: Int, height: Int) = viewport.update(width, height, true)
    override fun hide()   {}
    override fun pause()  {}
    override fun resume() {}
    override fun dispose() { stage.dispose(); skin.dispose() }
}
