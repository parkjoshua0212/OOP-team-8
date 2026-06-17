package com.blackjack

import com.badlogic.gdx.Game

class BlackjackGame : Game() {
    override fun create() {
        setScreen(BlackjackScreen())
    }
}
