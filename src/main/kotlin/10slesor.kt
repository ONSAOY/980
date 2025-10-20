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

    fun startQuest(questId: String): Boolean{
        val quest = quests[questId]
        if (quest != null && !quest.isCompleted){
            quest.isActive = true
            println("Квест активирован: ${quest.name}")
            return true
        }
        return false
    }

    // Функция проверки выполнения всех активных квестов
    fun checkAllQuests(player: Player){
        // .values - получает все значения словаря (все квесты)
        // .filter { } - фильтрует только активные квесты
        quests.values.filter { it.isActive }.forEach { quest ->
            quest.checkCompletion(player)
        }
    }

    fun displayQuests(){
        if (quests.isEmpty()){
            println("Список квестов пуст")
        }else {
            println("\n === Журнал квестов === ")
            // Перебор всех значений словаря квестой
            quests.values.forEach { quest ->
                quest.displayInfo()
            }
        }
    }

    fun getActiveQuests(): List<Quest>{
        // .toList() - преобразует в изменяемый список
        return quests.values.filter { it.isActive }.toList()
    }
}

class NPC(val name: String, val description: String){
    // mutableMapOf<String, String> - словарь диалогов
    // Ключ - фраза игрока, Значение - ответ NPC
    private val dialogues = mutableMapOf<String, String>()

    fun addDialogue(playerPhrase: String, npcResponse: String){
        dialogues[playerPhrase] = npcResponse
    }

    fun talk(){
        println("\n === Диалог с $name === ")
        println("$name: $description")

        if (dialogues.isEmpty()){
            println("$name не хочет говорить")
            return
        }

        // Показываем варианты ответов игрока
        println("\n Варианты ответов: ")
        dialogues.keys.forEachIndexed { index, phrase ->
            println("${index + 1}. $phrase")
        }
        println("${dialogues.size + 1}. Уйти")
        // Обрабатываем ввод игрока
        println("Выберете реплику: ")
        val choise = readln().toIntOrNull() ?: 0

        if (choise in 1..dialogues.size){
            // Преобразуем ключи в список и берем по индексу
            val playerPhrase = dialogues.keys.toList()[choise - 1]
            val npcResponse = dialogues[playerPhrase] // получаем ответ NPC по ключу (фразе игрока)

            println("\nВы: $playerPhrase")
            println("$name: $npcResponse")
        }else{
            println("Вы прощаетесь с $name")
        }
    }
}

class Player(
    name: String,
    health: Int,
    attack: Int
) : Character(name, health, attack){
    val inventory = Inventory()

    val questManager = QuestManager()

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

class Shop(val name: String, val description: String){
    private val itemsForSale = mutableMapOf<Item, Int>()

    private val buyPrices = mutableMapOf<String, Int>()

    fun addItem(item: Item, price: Int){
        itemsForSale[item] = price
        buyPrices[item.id] = (price * 0.6).toInt()
    }
    fun openShop(player: Player){
        println("\n === Бобро пожаловать в магазин: $name === ")
        println(description)

        var shopping = true

        while (shopping){
            println("\n Меню магазина ")
            println("1. Купить предметы")
            println("2. Продать предметы")
            println("3. Уйти")

            println("Выберете действие: ")
            when(readln().toIntOrNull() ?: 0){
                1 -> showItemForSale(player)
                2 -> showBuyMenu(player)
                3 -> {
                    shopping = false
                    println(" бб ")
                }
                else -> println("попробуй ещё раз") 
            }
        }
    }

    private fun showItemForSale(player: Player){
        if (itemsForSale.isEmpty()){
            println("Товаров нет")
            return
        }
        println("Товары на продажу: ")
        itemsForSale.forEach { (item, price) ->
            println("${item.name} - ${item.description} | Цена: $price fucking american dollars")
        }
        println("${itemsForSale.size + 1}. Назад")

        println("Выберете товар для покупки: ")
        val choise = readln().toIntOrNull() ?: 0

        if (choise in 1..itemsForSale.size){
            val selectedItem = itemsForSale.keys.toList()[choise - 1]
            val price = itemsForSale[selectedItem] ?: 0

            //ДЗ РЕАЛИЗОВАТЬ ПРОВЕРКУ ЗОЛОТА У ИГРОКА (ХВАТАЕТ ИЛИ НЕ)
            println(" Вы покупаете ${selectedItem.name} за $price золотых")
            player.inventory.addItem(selectedItem)
            // ДЗ ВЫЧЕСТЬ ЗОЛОТЫЕ
        }
    }
}

fun main(){

    println("Система квестов и npc")

    val player = Player("gigaNiga", 100, 15)

    // предметы для квестов создание
    val misteryHerb = Item(
        "mistery_herb",
        "Таинственная трава",
        "Редкое растение с целебными свойствами",
        15
    )

    val ancientAmulet = Item(
        "ancient_amulet",
        "Старинный амулет",
        "Древний амулет с магическими свойствами",
        100
    )

    //Создание квестов

    val herbQuest = Quest(
        "find_herbs",
        "Сбор целебных трав",
        "Найдите таинственную траву в лесу",
        "mistery_herb",
        50,
        ancientAmulet
    )

    val monsterQuest = Quest(
        "kill_monsters",
        "Очистка леса",
        "Убейте 3 врагов",
        rewardGold = 100
    )

    val villageElder = NPC("Cтарейшина деревни", "Мутный старик")

    villageElder.addDialogue("Поздороваться", "Бобро пожаловать путник")
    villageElder.addDialogue("Cпросить о работе", "Лес кишит тварями, поможешь мне?")
    villageElder.addDialogue("Cпросить о траве", "В глубине леса растет трава, собери для меня немного")

    player.questManager.addQuest(herbQuest)
    player.questManager.addQuest(monsterQuest)

    player.questManager.startQuest("find_herbs")

    // Игра с демонстрацией работы
    println(" === Взаимодейстие с npc === ")
    villageElder.talk()

    println("\n === Проверка квестов === ")
    player.questManager.displayQuests()

    println("\n === Игрок нашел траву === ")
    player.inventory.addItem(misteryHerb)

    println("\n === Проверка на выполнение квеста === ")
    player.questManager.checkAllQuests(player)

    println("\n === Финальный статус квестов === ")
    player.questManager.displayQuests()



//    val apple = Item(
//        "apple",
//        "Яблучко",
//        "Восстанавливает 10 Hp",
//        0,
//        { player ->
//            player.health += 10
//            println("${player.name} восстанавливает 10 Hp")
//        }
//    )

//    player.pickUpItem(apple)
//    player.pickUpItem(apple)
//    player.pickUpItem(apple)

//    player.showInventory()



//    if (player.inventory.hasItem("apple")){
//        println("Вы можете сьесть яблучко, у вас есть: ${player.inventory.countItems("apple")} штук")
//    }

}







