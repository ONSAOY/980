import kotlin.random.Random

class Item(
    val id: String,
    val name: String,
    val description: String,
    val value: Int = 0,
    val useEffect: (Player) -> Unit = {}
){
    fun use(player: Player){
        println("Используется: $name")
        useEffect(player)
    }

    fun displayInfo(){
        println("$name - $description (Ценность: $value)")
    }
}

class Inventory{
    //mutableListOf - создает пустой изменяемый список в который можно положить только предметы
    //private - доступ к списку предметов есть только внутри класса инвенторя
    private val items = mutableListOf<Item>()

    fun addItem(item: Item){
        // .add(item) - метод добавления предмета в конец списка
        items.add(item)
        println("Предмет '${item.name}' добавлен в инвентарь")
    }

    fun removeItem(index: Int): Boolean{
        // index in 0 until items.size - проверяет находится ли index в диапозоне от 0 до конца списка
        if (index in 0 until items.size){
            val removeItem = items.removeAt(index)
            println("Предмет: ${removeItem.name} удален из инвенторя")
        }
        println("Неверный индекс предмета, его нет в инаенторе!")
        return false

    }

    fun useItem(index: Int, player: Player): Boolean{
        if (index in 0 until items.size){
            val item = items[index]
            item.use(player)
            items.removeAt(index)
            return true
        }
        println("Неверный индекс предмета, его нет в инаенторе!")
        return false
    }

    fun display(){
        if (items.isEmpty()){
            println("Инвентарь пуст")
        }else{
            println("\n === Инвентарь === ")
            // { index, item -> - лямбда выражение с параметрами index и item
            items.forEachIndexed { index, item ->
                println("${index + 1}. ${item.name} - ${item.description}")
            }
            println("Всего предметов ${items.size}")
        }
    }

    fun findItemById(itemId: String): Item? {
        //.find {} - ищет 1 элемент удоблетворяющий условию поиска
        // it - ключевое слово , обозначающие текущий элемент в поиске
        // ? - функция может вернуть null, если ничего не найдено
        return  items.find { it.id == itemId}
    }
    fun hasItem(itemId: String): Boolean{
        // Вернет true если бы зотябы 1 элемент соответствует поиску
        return items.any { it.id == itemId }
    }

    fun countItems(itemId: String): Int{
        return items.count{it.id == itemId}
    }
}

open class Character(val name: String, var health: Int, val attack: Int){
    val isAlive: Boolean get() = health > 0

    open fun takeDamage(damage: Int){
        health -= damage
        println("$name получает $damage")
        if (health <= 0) println("$name пал в бою!")
    }

    fun attack(target: Character){
        if (!isAlive || !target.isAlive) return
        val damage = Random.nextInt(attack - 3, attack + 4)
        println("$name атакует ${target.name}!")
        target.takeDamage(damage)
    }
}

// Класс квеста
class Quest(
    val id: String,
    val name: String,
    val description: String,
    val requiredItemId: String? = null,
    val rewardGold: Int = 0,
    val rewardItem: Item? = null,

){
    var isCompleted: Boolean = false
    var isActive: Boolean = false

    fun checkCompletion(player: Player): Boolean{
        if(!isCompleted && isActive){
            // Если квест требует предмет проверяем его наличие у игрока
            if(requiredItemId != null && player.inventory.hasItem(requiredItemId)){
                completeQuest(player)
                return true
            }
        }
        return false
    }

    private fun completeQuest(player: Player){
        isCompleted = true
        isActive = true

        println("\n*** Квест $name выполнен ***")
        println("Награда: ")

        if (rewardGold > 0){
            println(" - Золото: $rewardGold")
            //В реальной игре тут добавляем золотишко игроку
        }

        if (rewardItem != null){
            println(" - Предмет: ${rewardItem.name}")
            player.inventory.addItem(rewardItem)
        }
    }

    fun displayInfo(){
        val status = when{
            isCompleted -> "Выполнен"
            isActive -> "Активен"
            else -> "Не активен"
        }
        println("[$status] $name: $description")
    }
}

class QuestManager{
    //Создаем изменяемый список с квестами, гдe:
    // String - типом значения данных ключа
    // Quest - тип значения
    private  val quests = mutableMapOf<String, Quest>()

    fun addQuest(quest: Quest){
        // quests[quest.id] = quest = добавляет в словарь по ключю quest.id
        quests[quest.id] = quest
    }

    fun getQuest(questId: String): Quest?{
        return quests[questId]
    }
}

class Player(
    name: String,
    health: Int,
    attack: Int
) : Character(name, health, attack){
    val inventory = Inventory()

    fun usePotion() {
        //используем поиск по id зелья здоровья
        val potion = inventory.findItemById("health_potions")
        if (potion != null){
            potion.use(this) // this - ссылка на текущий обьект Player
        }else{
            println(" У вас нет зелий здоровья")
        }
    }

    fun pickUpItem(item: Item){
        inventory.addItem(item)
    }

    fun showInventory(){
        inventory.display()
    }
}



fun main(){
    println(" === Система инвенторя === ")

    val player = Player("Onsaoy", 100, 15)

    val healthPotion = Item(
        "health_potions",
        "Зелье здоровья",
        "Восстанавливает 30 Hp",
        25,
        { player ->
            player.health += 30
            println("${player.name} восстанавливает 30 Hp")
        }
    )

    val strenghtPotion = Item(
        "strenght_potion",
        "Зелье силы",
        "Увиличивает урон на 10 (на 3 хода)",
        40,
        { player ->
            println("${player.name} вы чуствуете [РЕШИМОСТЬ], атака была увеличена")
        }
    )

    val oldKey = Item(
        "old_key",
        "Старый ключ",
        "Может открыть что-то",
        5
    ) // useEffect не указан - он по умолчанию остается {}

    println("\n === Игра началась === ")

    println("Игрок порылся в карманах, и нашел предметы")

    player.pickUpItem(healthPotion)
    player.pickUpItem(strenghtPotion)
    player.pickUpItem(oldKey)
    player.pickUpItem(healthPotion)

    player.showInventory()

    println("----- Использование предметов -----")
    player.inventory.useItem(0, player)

    println(" --- Поиск предметов --- ")
    val foundKey = player.inventory.findItemById("old_key")
    if (foundKey != null){
        println("Вы открываете дверь. Но он рассыпался у вас в руках")
    } else{
        println("Мне нужен ключ от этой двери")
    }



    if (player.inventory.hasItem("health_potions")){
        println("Вы можете излечится, у вас есть зелье здоровья: ${player.inventory.countItems("health_potions")} штук")
    }
}







