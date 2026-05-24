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

    // 상점 목록 출력
    fun showItems() {
        println("===== 상점 =====")
        for (item in itemList) {
            println("${item.name} - ${item.price}원 | ${item.description}")
        }
        println("================")
    }

    // 아이템 구매
    fun buyItem(itemName: String, balance: Int): Int {
        // 아이템 찾기
        var foundItem: Item? = null
        for (item in itemList) {
            if (item.name == itemName) {
                foundItem = item
            }
        }

        // 없는 아이템
        if (foundItem == null) {
            println("없는 아이템이에요!")
            return balance
        }

        // 잔액 부족
        if (balance < foundItem.price) {
            println("잔액이 부족해요! (잔액: ${balance}원, 필요: ${foundItem.price}원)")
            return balance
        }

        // 구매 성공
        println("${foundItem.name} 구매 완료! (-${foundItem.price}원)")
        return balance - foundItem.price
    }
}