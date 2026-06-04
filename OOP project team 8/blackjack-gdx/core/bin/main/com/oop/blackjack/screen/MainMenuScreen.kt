package com.oop.blackjack.screen

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import com.oop.blackjack.BlackjackGame
import com.oop.blackjack.UiFactory

class MainMenuScreen(private val game: BlackjackGame) : Screen {

    private val viewport = FitViewport(1280f, 720f)
    private val stage    = Stage(viewport, game.batch)
    private val skin     = UiFactory.buildSkin(game)

    // Animated card decorations
    private val cardX    = floatArrayOf(60f,  200f, 980f, 1120f, 640f)
    private val cardY    = floatArrayOf(80f,  540f, 60f,  520f,  10f)
    private val cardRot  = floatArrayOf(-15f, 12f, -8f,  20f,  -5f)
    private val cardSpin = floatArrayOf(8f,  -6f,  5f,  -9f,   7f)   // deg/s
    private var time     = 0f

    private val suits    = arrayOf("♠", "♥", "♦", "♣")
    private val ranks    = arrayOf("A", "K", "Q", "J", "10")
    private val cardData = Array(5) { i -> suits[i % 4] to ranks[i % 5] }

    init {
        buildStage()
        Gdx.input.inputProcessor = stage
    }

    private fun buildStage() {
        val root = Table().apply { setFillParent(true) }

        // ── Glow banner ──────────────────────────────────────────────────────
        val title = Label("♠  BLACKJACK  ♠", skin, "title").apply {
            setAlignment(Align.center)
        }
        val sub = Label("Clear all 20 stages to win", skin, "gold").apply {
            setAlignment(Align.center)
        }

        // ── Info block ───────────────────────────────────────────────────────
        val infoText = """
            |Start Balance :  ${'$'}1,000
            |Stages         :  20  (5 rounds each)
            |Min Bet        :  10% of stage target
            |Blackjack pays :  3 : 2
            |Shop items     :  Peek · Shield · Double Up
        """.trimMargin()
        val info = Label(infoText, skin).apply { setAlignment(Align.center) }

        // ── Play button ──────────────────────────────────────────────────────
        val playBtn = TextButton("  PLAY  ", skin, "green").apply {
            pad(18f, 50f, 18f, 50f)
        }
        playBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                game.setScreen(GameScreen(game))
                dispose()
            }
        })

        root.defaults().pad(12f)
        root.add(title).padBottom(4f).row()
        root.add(sub).padBottom(24f).row()
        root.add(info).padBottom(30f).row()
        root.add(playBtn).row()

        stage.addActor(root)
    }

    override fun render(delta: Float) {
        time += delta
        for (i in cardRot.indices) cardRot[i] += cardSpin[i] * delta

        Gdx.gl.glClearColor(0.05f, 0.08f, 0.05f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        val cam = (viewport.camera as OrthographicCamera)
        cam.update()

        // ── Draw decorative floating cards ────────────────────────────────
        game.shape.projectionMatrix = cam.combined
        game.batch.projectionMatrix = cam.combined

        // ── Draw felt table oval FIRST (background layer) ─────────────────
        game.shape.begin(ShapeRenderer.ShapeType.Filled)
        game.shape.setColor(0.10f, 0.32f, 0.10f, 0.55f)
        game.shape.ellipse(240f, 100f, 800f, 520f)
        game.shape.end()

        // ── Decorative floating cards ON TOP of the felt ──────────────────
        for (i in cardData.indices) {
            val cx = cardX[i] + MathUtils.sin(time * 0.5f + i) * 8f
            val cy = cardY[i] + MathUtils.cos(time * 0.4f + i * 1.3f) * 10f
            drawDecorCard(cx, cy, cardData[i].first, cardData[i].second)
        }

        stage.act(delta)
        stage.draw()
    }

    private fun drawDecorCard(x: Float, y: Float, suit: String, rank: String) {
        val w = 70f; val h = 98f
        val cx = x + w / 2; val cy = y + h / 2

        game.shape.begin(ShapeRenderer.ShapeType.Filled)
        // Shadow
        game.shape.setColor(0f, 0f, 0f, 0.35f)
        game.shape.rect(x + 5f, y - 5f, w, h)
        // Card face
        game.shape.setColor(0.97f, 0.96f, 0.93f, 0.85f)
        game.shape.rect(x, y, w, h)
        game.shape.end()

        game.shape.begin(ShapeRenderer.ShapeType.Line)
        game.shape.setColor(0.6f, 0.6f, 0.6f, 0.7f)
        game.shape.rect(x, y, w, h)
        game.shape.end()

        val red = suit == "♥" || suit == "♦"
        game.batch.begin()
        game.fontCard.setColor(if (red) Color(0.8f, 0.1f, 0.1f, 0.85f) else Color(0.1f, 0.1f, 0.1f, 0.85f))
        game.fontCard.draw(game.batch, rank, x + 6f, y + h - 6f)
        game.fontCard.draw(game.batch, suit, cx - 10f, cy + 12f)
        game.batch.end()
    }

    override fun resize(width: Int, height: Int) = viewport.update(width, height, true)
    override fun show()    {}
    override fun hide()    {}
    override fun pause()   {}
    override fun resume()  {}
    override fun dispose() { stage.dispose(); skin.dispose() }
}
