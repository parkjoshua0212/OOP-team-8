package com.oop.blackjack

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.oop.blackjack.screen.MainMenuScreen

class BlackjackGame : Game() {

    lateinit var batch:    SpriteBatch
    lateinit var shape:    ShapeRenderer
    private lateinit var fontShader: ShaderProgram

    lateinit var fontSm:   BitmapFont   // body text
    lateinit var fontMd:   BitmapFont   // labels / scores
    lateinit var fontLg:   BitmapFont   // titles / results
    lateinit var fontCard: BitmapFont   // card rank & suit

    override fun create() {
        // ── Custom shader ─────────────────────────────────────────────────────
        // LibGDX's default BitmapFont atlas stores glyphs as white pixels on a
        // black opaque background (every pixel alpha = 1).  The standard shader
        // multiplies vertex-color × texel, which turns black background pixels
        // into black rectangles and makes glyph shapes invisible.
        //
        // Fix: use luminance (brightness) of the texel as the alpha mask.
        //   • white glyph pixel  → luminance = 1  → fully opaque, vertex color shown
        //   • black bg pixel     → luminance = 0  → fully transparent
        //   • grey anti-alias    → luminance = 0–1 → partial opacity (smooth edges)
        // The step() handles transparent-background fonts (FreeType) too.
        ShaderProgram.pedantic = false
        fontShader = ShaderProgram(
            """
            attribute vec4 a_position;
            attribute vec4 a_color;
            attribute vec2 a_texCoord0;
            uniform mat4 u_projTrans;
            varying vec4 v_color;
            varying vec2 v_texCoords;
            void main() {
                v_color = a_color;
                v_color.a = v_color.a * (255.0/254.0);
                v_texCoords = a_texCoord0;
                gl_Position = u_projTrans * a_position;
            }
            """.trimIndent(),
            """
            #ifdef GL_ES
            precision mediump float;
            #endif
            varying vec4 v_color;
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            void main() {
                vec4 t = texture2D(u_texture, v_texCoords);
                float lum  = dot(t.rgb, vec3(0.299, 0.587, 0.114));
                // opaque pixels (a~1): use luminance; transparent pixels (a<1): use alpha
                float mask = mix(lum, t.a, step(t.a, 0.9999));
                gl_FragColor = vec4(v_color.rgb, v_color.a * mask);
            }
            """.trimIndent()
        )
        if (!fontShader.isCompiled)
            Gdx.app.error("Shader", fontShader.log)

        batch = SpriteBatch()
        if (fontShader.isCompiled) batch.shader = fontShader
        shape = ShapeRenderer()

        // ── Fonts ─────────────────────────────────────────────────────────────
        // Linear texture filtering is critical: without it, non-integer scale
        // factors cause the GPU sampler to land on black background texels,
        // making entire characters appear invisible.
        fontSm   = makeFont(1.1f)
        fontMd   = makeFont(1.6f)
        fontLg   = makeFont(2.6f)
        fontCard = makeFont(1.4f)

        setScreen(MainMenuScreen(this))
    }

    private fun makeFont(scale: Float) = BitmapFont().apply {
        data.setScale(scale)
        // Apply Linear mag/min filter so text stays readable at any scale
        region.texture.setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        )
    }

    override fun dispose() {
        screen?.dispose()
        batch.dispose()
        shape.dispose()
        if (::fontShader.isInitialized) fontShader.dispose()
        fontSm.dispose()
        fontMd.dispose()
        fontLg.dispose()
        fontCard.dispose()
    }
}
