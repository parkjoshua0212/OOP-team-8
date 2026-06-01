package com.oop.game

class Item(
    val name: String,
    val price: Int,
    val description: String
)

class Shop {

    val itemList = mutableListOf(
        Item("hint", 200, "딜러의 카드 한 장을 슬쩍 봅니다"),
        Item("insurance", 300, "버스트 시 배팅금을 돌려받습니다"),
        Item("double", 500, "이번 라운드 승리시 받는 금액이 2배가 됩니다 ")
    )

    val ownedItems = mutableListOf<String>()
    val activeItems = mutableListOf<String>()  // 이번 라운드에 실제로 사용 중인 아이템

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

    // "yes" 선택 시: ownedItems -> activeItems로 이동
    fun activateItems() {
        activeItems.addAll(ownedItems)
        ownedItems.clear()
    }

    // activeItems 기준으로 확인 (이번 라운드에 사용 중인지)
    fun checkMyItem(itemName: String): Boolean {
        return activeItems.contains(itemName)
    }

    // 라운드 끝: activeItems만 초기화 (ownedItems는 유지)
    fun clearItems() {
        activeItems.clear()
    }

    // 보유 아이템이 있는지 (ownedItems 기준)
    fun hasItems(): Boolean {
        return ownedItems.isNotEmpty()
    }
}