package com.blackjack

class Item(
    val name: String,
    val price: Int,
    val description: String
)

class Shop {

    val itemList = mutableListOf(
        Item("hint", 200, "Peek at dealer hidden card"),
        Item("insurance", 300, "Refund bet on bust"),
        Item("double", 500, "Double payout on win")
    )

    val ownedItems = mutableListOf<String>()
    val activeItems = mutableListOf<String>()

    fun showItems() {
        println("===== 상점 =====")
        for (item in itemList) {
            println("${item.name} - ${item.price}원 | ${item.description}")
        }
        println("================")
    }

    fun buyItem(itemName: String, wallet: Wallet) {
        val foundItem = itemList.find {
            it.name.equals(itemName, ignoreCase = true)
        }
        if (foundItem == null) {
            println("This item does not exists")
            return
        }
        if (ownedItems.contains(foundItem.name)) {
            println("You already have this item")
            return
        }
        if (!wallet.canAfford(foundItem.price)) {
            println("You don't have enough funds! (Require: ${foundItem.price}")
            return
        }
        wallet.placeBet(foundItem.price)
        ownedItems.add(foundItem.name)
        println("${foundItem.name} Purchase complete!")
    }

    fun activateItems() {
        activeItems.addAll(ownedItems)
        ownedItems.clear()
    }

    fun checkMyItem(itemName: String): Boolean {
        return activeItems.contains(itemName)
    }

    fun clearItems() {
        activeItems.clear()
    }

    fun hasItems(): Boolean {
        return ownedItems.isNotEmpty()
    }
}