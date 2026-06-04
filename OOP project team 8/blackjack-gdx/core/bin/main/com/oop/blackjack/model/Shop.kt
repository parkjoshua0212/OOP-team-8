package com.oop.blackjack.model

data class ShopItem(
    val id: String,
    val name: String,
    val price: Int,
    val description: String
)

class Shop {
    val catalog = listOf(
        ShopItem("hint",      "Peek",     200, "Reveal the dealer's hidden card"),
        ShopItem("insurance", "Shield",   300, "Get your bet back if you bust"),
        ShopItem("double",    "Double Up",500, "2× payout on a win this round")
    )

    /** Items the player owns (available to activate before a round). */
    val owned = mutableListOf<String>()

    /** Items activated for the current round. */
    val active = mutableListOf<String>()

    fun buy(itemId: String, wallet: Wallet): BuyResult {
        val item = catalog.find { it.id == itemId } ?: return BuyResult.NOT_FOUND
        if (owned.contains(itemId) || active.contains(itemId)) return BuyResult.ALREADY_OWNED
        if (!wallet.canAfford(item.price)) return BuyResult.INSUFFICIENT_FUNDS
        wallet.placeBet(item.price)
        owned.add(itemId)
        return BuyResult.SUCCESS
    }

    fun activateOwned() {
        active.addAll(owned)
        owned.clear()
    }

    fun isActive(itemId: String) = active.contains(itemId)
    fun isOwned(itemId: String)  = owned.contains(itemId)
    fun hasOwned()               = owned.isNotEmpty()

    /** Clear active items at end of round. */
    fun clearActive() = active.clear()

    /** Full reset between stage sessions if needed. */
    fun fullReset() { owned.clear(); active.clear() }
}

enum class BuyResult { SUCCESS, NOT_FOUND, ALREADY_OWNED, INSUFFICIENT_FUNDS }
