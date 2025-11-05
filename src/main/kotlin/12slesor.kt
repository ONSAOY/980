import org.jetbrains.annotations.Async
import java.io.File
import java.io.IOException
import java.security.Security

// Класс для безопасной работы с файлами
class SafeFileManager{

    // Метод для безопасного чтения содержимого файла
    fun readFileSafely(filename: String): String? {
        return try {
            //Пытаемся прочитать файл
            val file = File(filename)
            if (!file.exists()){
                // Если файл не существует - это оштба логики
                println("Error: File '$filename' didnt exsist!")
                return null
            }
            file.readText() // прочитать в виде текста содержимое файла
        }catch (e: SecurityException){
            // нет прав на чтение файла
            println("Mismatch permissions for this file: $filename")
            null
        }catch (e: IOException){
            //Общая ошибка ввода или вывода(переполнение диска, повреждение файла/диска)
            println("Cannot read file: '$filename' - ${e.message}")
            null
        }catch (e: Exception){
            println("Unknow Error: ${e.message}")
            null
        }
    }
    fun writeFileSafely(filename: String, content: String): Boolean{
        return try {
            val file = File(filename)

            // Проверка можно ли создать файл
            if (file.parentFile != null && !file.parentFile.exists()){
                // Создаем директорию если такой ещё нет
                file.parentFile.mkdirs()
            }

            file.writeText(content)
            println("File: '$filename' successfuly saved")
            true
        }catch (e: SecurityException){
            //Общая ошибка ввода или вывода(переполнение диска, повреждение файла/диска)
            println("Mismatch write to file: $filename")
            false
        }
    }

    fun deleteFileSafely(filename: String): Boolean{
        return try {
            val file = File(filename)
            if (!file.exists()){
                println("Error: File '$filename' didnt exsist!")
                return false
            }

            if (file.delete()){
                println("File: '$filename' deleted successfuly")
                true
            }else{
                println("Cant delete file: $filename")
                false
            }
        }catch (e: SecurityException){
            println("Mismatch permissions for this file: $filename")
            false
        }
    }
}

// класс для системы сохранения игры с обработкой ошибок
class RobustSaveSystem{
    private val fileManager = SafeFileManager()
    private val saveDir = "game_saves"

    // Функция сохранения данных игрока
    fun savePlayerData(playerName: String, health: Int, level: Int, inventory: List<String>): Boolean{
        // Создание содержимого
        val content ="""
            Игрок: $playerName
            Здоровье: $health
            Уровень: $level
            Инвентарь: ${inventory.joinToString(", ")}
        """.trimIndent()


        // Создаем имя файла
        val timestamp = System.currentTimeMillis()
        val filename = "$saveDir/save_${playerName}_$timestamp.txt"

        return fileManager.writeFileSafely(filename, content)
    }

    // Функция для загрузки данных игрока
    fun loadPlayerData(filename: String): Map<String, String>? {
        val content = fileManager.readFileSafely(filename)
        // проверка на то не пустой ли наш content

        return try {
            //парсинг содержимого файла
            val data = mutableMapOf<String, String>()
            content?.lines()?.forEach { line ->
                if (line.contains(":")){
                    val parts = line.split(":", limit = 2)
                    if (parts.size == 2){
                        val key = parts[0].trim()
                        val value = parts[1].trim()
                        data[key] = value
                    }
                }
            }
            data
        } catch (e: Exception){
            println("Parting error: undefind file type - ${e.message}")
            null
        }
    }

    // Функция получения списка сохранения
    fun listSaves(): List<String>{
        return try {
            val dir = File(saveDir)
            if (!dir.exists()){
                return emptyList()
            }
            dir.list()?.toList() ?: emptyList()
        }catch (e: Exception){
            println("ERROR: you have no permissions to save directory")
            emptyList()
        }
    }

    //
    fun createBackup(originalFilename: String): Boolean{
        return try {
            val originalFile = File(originalFilename)
            if (originalFile.exists()){
                println("originalFile doesnt exists: $originalFilename")
                return false
            }

            val backupFilename = "$originalFilename.backup"
            val content = originalFile.readText()
            fileManager.writeFileSafely(backupFilename, content)
        }catch (e: Exception){
            println("ERROR while create backup: ${e.message}")
            false
        }
    }
}


// КЛАСС ВАЛИДАЦИИ ДАННЫХ ИГРЫ
class GameDataValidator{

    // ФУНКЦИЯ ДЛЯ ПРОВЕРКИ ДАННЫХ ИГРОКА
    fun validatePlayerData(name: String,health: Int,level: Int): Boolean{
        val errors = mutableListOf<String>()

        // ПРОВЕРКА ИМЕНИ ИГРОКА
        if (name.isBlank()){
            errors.add("Name cannot be empty")
        }
        if (name.length < 2){
            errors.add("Name cannot be less then 2 characters")
        }
        if (name.length > 20){
            errors.add("Name cannot be longer 20 characters")
        }
        if (name.any{it.isDigit()}){
            errors.add("Name cannot contains numbers")
        }

        // Проверка здоровья
        if (health < 0){
            errors.add("Health cannot be negative")
        }
        if (health > 1000){
            errors.add("Health cannot be more than 1000")
        }

        if (level < 1){
            errors.add("Level cannot be less then 1")
        }
        if (level > 100){
            errors.add("Level cannot be more then 100")
        }

        // ЕСЛИ ЕСТЬ ОШИБКИ ВЫБРАСЫВАЕМ ИСКЛЮЧЕНИЯ С ИНФОРМАЦИЕЙ
        if (errors.isNotEmpty()){
            // throw - явный метод, чтобы сказать программе, что выполнения завершилось с исключением
            throw GameDataException("Game data validate error", errors)
        }

        return true
    }
}

// КАСТОМНОЕ ИСКЛЮЧЕНИЕ ДЛЯ ОШИБОК НАШЕЙ ИГРЫ
class GameDataException(message: String, val validationErrors: List<String>): Exception(message){
    fun printValidationErrors(){
        println("DETECTED VALIDATION ERRORS: ")
        validationErrors.forEachIndexed { index, error ->
            println(" ${index + 1}. $error ")
        }
    }
}


fun main(){
    println("=== СИСТЕМА СОХРАНЕНИЯ ИГРЫ ===")

    val saveSystem = RobustSaveSystem()
    val validator = GameDataValidator()
    val safeInput = SafeInput()

    // ДЕМОНСТРАЦИЯ СОХРАНЕНИЯ ИГРЫ
    println("=== Сохранения данных игрока ===")

    val playerName = "go"
    val health = 100
    val level = 5
    val inventory = listOf("Меч", "Щит", "Зелье маны")

    if (saveSystem.savePlayerData(playerName,health,level,inventory)){
        println("Player data saved successfully")
    }else{
        println("Failed to save player data")
    }

    println("\n === Загрузка данных игрока ===")
    val saves = saveSystem.listSaves()
    if (saves.isNotEmpty()){
        val firstSave = saves.first()
        val loadedData = saveSystem.loadPlayerData("game_saves/$firstSave")

        if (loadedData != null){
            println("Loaded data: ")
            loadedData.forEach { (key, value) ->
                println("$key: $value")
            }
        }
    }else{
        println("Saves not found")
    }

    // Валидация данных
    try {
        validator.validatePlayerData("go", 100,10)
        println("Player data is correct")
    }catch (e: GameDataException){
        e.printValidationErrors()
    }

    try {
        validator.validatePlayerData("g",-5,0)
        println("Player data is correct")
    }catch (e: GameDataException){
        e.printValidationErrors()
    }
}