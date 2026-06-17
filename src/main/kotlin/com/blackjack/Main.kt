package com.blackjack

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

fun main() {
    val config = Lwjgl3ApplicationConfiguration()
    config.setTitle("Blackjack")
    config.setWindowedMode(1280, 720)
    config.setResizable(false)
    Lwjgl3Application(BlackjackGame(), config)
}
