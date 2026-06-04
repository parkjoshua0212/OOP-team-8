package com.oop.blackjack

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.*

object UiFactory {

    fun buildSkin(game: BlackjackGame): Skin {
        val skin = Skin()

        // 1×1 white pixel — base for all NinePatchDrawables
        val pm = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pm.setColor(Color.WHITE); pm.fill()
        skin.add("pixel", Texture(pm))
        pm.dispose()

        // NOTE: fonts are NOT added to the skin.
        // skin.dispose() disposes every Disposable it manages; adding shared
        // BitmapFont objects would destroy them when any screen disposes its skin.

        // ── Label styles ─────────────────────────────────────────────────────
        skin.add("default", Label.LabelStyle(game.fontSm, Color.WHITE))
        skin.add("medium",  Label.LabelStyle(game.fontMd, Color.WHITE))
        skin.add("title",   Label.LabelStyle(game.fontLg, Color(1f, 0.84f, 0f, 1f)))
        skin.add("gold",    Label.LabelStyle(game.fontMd, Color(1f, 0.84f, 0f, 1f)))
        skin.add("red",     Label.LabelStyle(game.fontMd, Color(1f, 0.3f, 0.3f, 1f)))
        skin.add("green",   Label.LabelStyle(game.fontMd, Color(0.3f, 1f, 0.4f, 1f)))
        skin.add("result",  Label.LabelStyle(game.fontLg, Color.WHITE))

        // ── Button styles ────────────────────────────────────────────────────
        fun btnStyle(font: com.badlogic.gdx.graphics.g2d.BitmapFont,
                     fgColor: Color, bgUp: Color, bgOver: Color, bgDown: Color
        ) = TextButton.TextButtonStyle().also { s ->
            s.font      = font
            s.fontColor = fgColor
            s.up        = skin.newDrawable("pixel", bgUp)
            s.over      = skin.newDrawable("pixel", bgOver)
            s.down      = skin.newDrawable("pixel", bgDown)
        }

        // default — brown/gold (for neutral actions)
        skin.add("default", btnStyle(
            game.fontMd,
            Color(1f, 0.88f, 0.4f, 1f),
            Color(0.22f, 0.16f, 0.05f, 0.92f),
            Color(0.38f, 0.28f, 0.08f, 0.92f),
            Color(0.50f, 0.38f, 0.12f, 0.92f)
        ))

        // green — positive (HIT, DEAL, PLAY)
        skin.add("green", btnStyle(
            game.fontMd,
            Color.WHITE,
            Color(0.08f, 0.38f, 0.10f, 0.92f),
            Color(0.14f, 0.56f, 0.16f, 0.92f),
            Color(0.20f, 0.70f, 0.22f, 0.92f)
        ))

        // red — negative (STAND, BACK)
        skin.add("red", btnStyle(
            game.fontMd,
            Color.WHITE,
            Color(0.45f, 0.08f, 0.08f, 0.92f),
            Color(0.65f, 0.12f, 0.12f, 0.92f),
            Color(0.80f, 0.18f, 0.18f, 0.92f)
        ))

        // gold — shop / items
        skin.add("gold", btnStyle(
            game.fontMd,
            Color(0.12f, 0.08f, 0.02f, 1f),
            Color(0.80f, 0.65f, 0.10f, 0.92f),
            Color(1.00f, 0.84f, 0.20f, 0.92f),
            Color(1.00f, 0.90f, 0.40f, 0.92f)
        ))

        // small — for +/- buttons
        skin.add("small", btnStyle(
            game.fontSm,
            Color.WHITE,
            Color(0.20f, 0.20f, 0.20f, 0.90f),
            Color(0.35f, 0.35f, 0.35f, 0.90f),
            Color(0.50f, 0.50f, 0.50f, 0.90f)
        ))

        return skin
    }
}
