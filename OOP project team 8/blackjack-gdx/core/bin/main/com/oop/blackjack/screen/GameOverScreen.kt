package com.oop.blackjack.screen

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import com.oop.blackjack.BlackjackGame
import com.oop.blackjack.UiFactory

class GameOverScreen(
    private val game: BlackjackGame,
    private val won: Boolean,
    private val finalBalance: Int,
    private val stageReached: Int
) : Screen {

    private val viewport = FitViewport(1280f, 720f)
    private val cam      get() = viewport.camera as OrthographicCamera
    private val stage    = Stage(viewport, game.batch)
    private val skin     = UiFactory.buildSkin(game)

    private var time = 0f

    // Particle-like confetti for win screen
    private data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float,
                                 val color: Color, var rot: Float, val rotSpeed: Float)
    private val particles = mutableListOf<Particle>()

    init {
        if (won) {
            repeat(80) {
                particles.add(Particle(
                    x = MathUtils.random(0f, 1280f),
                    y = MathUtils.random(400f, 900f),
                    vx = MathUtils.random(-40f, 40f),
                    vy = MathUtils.random(-120f, -40f),
                    color = Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1f),
                    rot = MathUtils.random(360f),
                    rotSpeed = MathUtils.random(-180f, 180f)
                ))
            }
        }

        buildUI()
        Gdx.input.inputProcessor = stage
    }

    private fun buildUI() {
        val root = Table().apply { setFillParent(true) }

        val titleText = if (won) "★  YOU WIN!  ★" else "GAME OVER"
        val titleStyle = if (won) "title" else "result"
        val subText = if (won)
            "Congratulations! You cleared all 20 stages!"
        else
            "You reached Stage $stageReached"

        val titleLbl = Label(titleText, skin, titleStyle).apply { setAlignment(Align.center) }
        val subLbl   = Label(subText,   skin, "gold"   ).apply { setAlignment(Align.center) }
        val balLbl   = Label("Final Balance: \$$finalBalance", skin, "medium").apply { setAlignment(Align.center) }

        val playAgainBtn = TextButton("  PLAY AGAIN  ", skin, "green")
        playAgainBtn.addListener(object : ClickListener() {
            override fun clicked(e: InputEvent, x: Float, y: Float) {
                game.setScreen(MainMenuScreen(game))
                dispose()
            }
        })

        root.add(titleLbl).padBottom(16f).row()
        root.add(subLbl).padBottom(12f).row()
        root.add(balLbl).padBottom(40f).row()
        root.add(playAgainBtn).width(260f).height(68f)

        stage.addActor(root)
    }

    override fun render(delta: Float) {
        time += delta

        // Update particles
        for (p in particles) {
            p.x += p.vx * delta
            p.y += p.vy * delta
            p.vy -= 60f * delta  // gravity
            p.rot += p.rotSpeed * delta
            if (p.y < -20f) {
                p.y = 780f
                p.x = MathUtils.random(0f, 1280f)
                p.vy = MathUtils.random(-120f, -40f)
            }
        }

        val bgR = if (won) 0.04f else 0.06f
        val bgG = if (won) 0.06f else 0.02f
        Gdx.gl.glClearColor(bgR, bgG, 0.04f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        cam.update()
        game.shape.projectionMatrix = cam.combined
        game.batch.projectionMatrix  = cam.combined

        // ── ShapeRenderer ──────────────────────────────────────────────────
        game.shape.begin(ShapeRenderer.ShapeType.Filled)

        // Pulsing glow behind title
        val glow = (MathUtils.sin(time * 2.5f) + 1f) * 0.5f
        if (won) {
            game.shape.setColor(0.5f * glow, 0.4f * glow, 0f, 0.35f)
            game.shape.ellipse(340f, 280f, 600f, 200f)
        } else {
            game.shape.setColor(0.4f * glow, 0f, 0f, 0.35f)
            game.shape.ellipse(340f, 280f, 600f, 200f)
        }

        // Confetti particles
        for (p in particles) {
            game.shape.color = p.color
            game.shape.rect(p.x, p.y, 10f, 6f)
        }
        game.shape.end()

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) = viewport.update(width, height, true)
    override fun show()   { Gdx.input.inputProcessor = stage }
    override fun hide()   {}
    override fun pause()  {}
    override fun resume() {}
    override fun dispose() { stage.dispose(); skin.dispose() }
}
