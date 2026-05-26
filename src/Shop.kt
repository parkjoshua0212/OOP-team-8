package com.oop.game

// 상점에서 판매할 아이템 하나를 나타내는 클래스
class Item(
    val name: String,
    val price: Int,
    val description: String
)

class Shop {

    // 판매할 아이템 목록
    val itemList = mutableListOf(
        Item("hint", 200, "딜러의 카드 한 장을 살짝 봅니다"),
        Item("insurance", 300, "버스트 시 배팅금을 돌려받습니다"),
        Item("double", 500, "다음 판 배팅금이 2배가 됩니다")
    )

    //Players current rounds items
    val ownedItems = mutableListOf<String>()

    // 상점 목록 출력
    fun showItems() {
        println("===== 상점 =====")
        for (item in itemList) {
            println("${item.name} - ${item.price}원 | ${item.description}")
        }
        println("================")
    }

    // 아이템 구매
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
            println("You dont have enough funds! (Require: ${foundItem.price}")
            return
        }

        wallet.placeBet(foundItem.price) //reduce money from wallet
        ownedItems.add(foundItem.name)
        println("${foundItem.name} Purchase complete!")
    }
    fun hasItem(itemName: String):Boolean {
        return ownedItems.contains(itemName)
    }

    fun clearItems() {
        ownedItems.clear()
    }
}