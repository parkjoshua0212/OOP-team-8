package com.oop.blackjack.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.oop.blackjack.BlackjackGame

fun main() {
    val config = Lwjgl3ApplicationConfiguration().apply {
        setTitle("♠ Blackjack — OOP Team 8")
        setWindowedMode(1280, 720)
        setResizable(false)
        useVsync(true)
        setForegroundFPS(60)
    }
    Lwjgl3Application(BlackjackGame(), config)
}
