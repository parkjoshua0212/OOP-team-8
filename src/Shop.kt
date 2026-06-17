package com.oop.game

// 아이템 정보를 담는 클래스 (이름, 가격, 설명)
class Item(
    val name: String,
    val price: Int,
    val description: String
)

// 상점 관리 클래스
class Shop {
    // 판매 중인 아이템 목록
    val itemList = mutableListOf(
        Item("hint", 200, "딜러의 카드 한 장을 살짝 봅니다"),
        Item("insurance", 300, "버스트 시 배팅금을 돌려받습니다"),
        Item("double", 500, "이번 라운드 승리시 받는 금액이 2배가 됩니다")
    )

    // 구매했지만 아직 라운드에 사용하지 않은 아이템
    val ownedItems = mutableListOf<String>()
    // 이번 라운드에 활성화된 아이템
    val activeItems = mutableListOf<String>()

    // 판매 목록 출력
    fun showItems() {
        println("===== 상점 =====")
        for (item in itemList) {
            println("${item.name} - ${item.price}원 | ${item.description}")
        }
        println("================")
    }

    // 아이템 구매: 없는 아이템 / 중복 보유 / 잔액 부족 순서로 검사 후 구매
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

    // 보유 아이템을 활성 상태로 전환 (라운드 시작 시 "yes" 선택 시 호출)
    fun activateItems() {
        activeItems.addAll(ownedItems)
        ownedItems.clear()
    }

    // 특정 아이템이 이번 라운드에 활성화되어 있는지 확인
    fun checkMyItem(itemName: String): Boolean {
        return activeItems.contains(itemName)
    }

    // 라운드 종료 후 활성 아이템 초기화
    fun clearItems() {
        activeItems.clear()
    }

    // 사용 가능한 보유 아이템이 있는지 확인
    fun hasItems(): Boolean {
        return ownedItems.isNotEmpty()
    }
}