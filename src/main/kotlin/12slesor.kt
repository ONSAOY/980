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
            content.lines().forEach { line ->
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
        } // Сделать перехватчик исключений на то верный ли формат файла сохранения
    }
}