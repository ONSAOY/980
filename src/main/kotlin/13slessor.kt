import jdk.dynalink.Operation
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.sound.midi.MetaMessage

//
class GameLogger{
    private val logFile = "game_log.txt"
    private val fileManager = SafeFileManager()

    //
    fun log(message: String, level: String = "INFO"){
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val logEntry = "[$timestamp] [$level] $message\n"
        // Проверка на дублирование в консоль важных сообщений
        if (level == "ERROR" || level == "WARN"){
            println("LOG [$level]: $message")
        }

        //Запись в файл логирования
        fileManager.writeFileSafely(logFile, logEntry)
    }

    // методы помощники для разных уровней логирования
    fun info(message: String) = log(message, "INFO")
    fun warn(message: String) = log(message, "WARN")
    fun error(message: String) = log(message, "ERROR")
    fun debug(message: String) = log(message, "DEBUG")
}

// БАЗОВЫЙ КЛАСС ДЛЯ ВСЕХ ИГРОВЫХ СИСТЕМ С ОБРОБОТКОЙ ОШИБОК
abstract class GameSystem(val systemName: String, protected val logger: GameLogger){

    // МЕТОД ДЛЯ БЕЗОПАСНОГО ВЫПОЛНЕНИЯ ОПЕРАЦИЙ СИСТЕМЫ
    // <T> - объявление обобщенного типа данных
    // T - ПЛЕЙСХОЛДЕР (заполнитель для любого типа данных)
    // Надо думать о T чем то временном или обстрактном
    protected fun <T> executeSafely(operation: String, block: () -> T): T? {
        // Читаем верхнюю строку как:
        // Функция executeSafely выполняется с каким-то типом T
        // Она принимает операцию и блок кода, который возвращает T
        // После выполнения возвращает T? (T or null)
        try {
            logger.debug("$systemName: Начало операции: $operation")
            val result = block() // Выполняем переданный блок кода
            logger.debug("$systemName: Операции $operation завершена успешно")
            // вернуть результат работы
            return  result
        } catch (e: Exception){
            logger.error("$systemName: Ошибка операции: $operation - ${e.message}")
            return null
        }
    }
    // Абстрактный метод для инициализации системы
    abstract fun initialize(): Boolean
    // Абстрактный метод для экстренной остановки системы
    abstract fun emergencyShutdown()

    val result: String? = executeSafely("Получение имени игрока"){
        // Сдесь может быть неограниченное число кода, которое после вычесления должен вернуть String
        "Oleg"
    }
    val resultInt: Int? = executeSafely("Расчет урона"){
        42
    }
    val resultBool: Boolean? = executeSafely("Проверка жизни"){
        true
    }
}

// Система боя с обработкой ошибок
class CombatSystem(logger: GameLogger) : GameSystem("CombatSystem", logger){
    private var isInitialized = false

    override fun initialize(): Boolean {
        return  executeSafely("initialize"){
            // Имитация инициализации системы бой
            logger.info("Инициализация системы боя...")
            Thread.sleep(100)
            isInitialized = true
            logger.info("Система боя успешно инициализирована")
            true
        } ?: false
    }

    fun performAttack(attacker: String, target: String, damage: Int): Boolean{
        if (!isInitialized){
            logger.warn("Попытка атаки при не инициализированной системы боя")
            return false
        }

        return executeSafely("performAttack"){
            // Проверка корректности введенных параметров
            if (damage < 0){
                throw IllegalArgumentException("Урон не может быть отрицательным: $damage")
            }
            if (attacker.isBlank() || target.isBlank()){
                throw IllegalArgumentException("Имена персонажей не могут быть пустыми")
            }

            logger.info("$attacker атакует $target с уроном: $damage")
            true
        } ?: false
    }

    override fun emergencyShutdown() {
        logger.warn("Аварийное завершение системы боя")
        isInitialized = false
        // Здесь в будущем будет освобождение ресурсов сохранения состояния и тд
    }
}

class InventorySystem(logger: GameLogger): GameSystem("InventoruSystem", logger){
    private val items = mutableListOf<String>()
    private var isInitialized = false

    override fun initialize(): Boolean {
        return executeSafely("initialize"){
            logger.info("Инит. системы инвенторя...")
            // Загрузка предметов по умолчанию при создание игрока
            items.addAll(listOf("Старый меч", "Поношенный доспех"))
            isInitialized = true
            logger.info("Система инвенторя инит. успешно")
            true
        } ?: false
    }
    fun addItem(item: String): Boolean{
        if (!isInitialized){
            logger.warn("Попытка добавить в предемет в инвентарь без инит системы")
            return false
        }

        return executeSafely("addItem"){
            if (item.isBlank()){
                throw IllegalArgumentException("Название предмета не может быть пустым")
            }
            if (items.size >= 20){
                throw IllegalArgumentException("Инвентарь переполнен(макс - 20 предметов)")
            }

            items.add(item)
            logger.info("Предмет $item добавлен в инвентарь, всего предметов: ${items.size}")

            true
        } ?: false
    }

    fun getItems(): List<String>{
        if (!isInitialized){
            logger.warn("Попытка получить предметы в инвентарь без инит системы")
            return emptyList()
        }

        return executeSafely("getItems"){
            items.toList() // Возвращение копии списка
        } ?: emptyList()
    }

    override fun emergencyShutdown() {
        val backUp = items.joinToString(" \n ")
        logger.warn("СИСТЕМА ОТКЛЮЧАЕТСЯ!!!!!!!!!! ТЫ ДОЛЖЕН СЖЕЧЬ СВОЙ КОМПЬЮТЕР СЕЙЧАС ЖЕ!!!!!!!!!!!!!!!!")
        val popa = File("backUpFile.txt").writeText(backUp)
        isInitialized = false
        // Логирование warn экстренного отключения системы перед отключением
        // Сохранения состояния инвенторя перед отключением
        // Создание бэкап-списка использовать метод joinToString("\n")
        // Используя метод Fail в атрибут которого мы кладем название файла бэкапа .txt + записать в файл созданный бэкап список writeText
        // Проверка на инициализацию должна стать false
    }
}