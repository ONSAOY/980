//import kotlinx.coroutines.*  // Импорт беблиотеки карутин * - значит импортировать все
//import kotlin.system.measureTimeMillis // Функция измерения времени выполнения
//import kotlin.time.Duration
//import kotlin.time.DurationUnit
//import kotlin.time.ExperimentalTime
//
//// Класс для управления игровым временем
//class GameTime{
//    // L - Long - тип данныъ для больших чисел в милисекунду
//    private var lastFrameTime = 0L
//
//    val deltaTime: Float
//        get(){
//            val currentTime = System.currentTimeMillis() // Возвращает текущее время в милисекундах
//            val delta = (currentTime - lastFrameTime) / 1000f // 1000f - преобразование милисекунд в секунды
//            lastFrameTime = currentTime // Обновление времени последнего кадра
//
//            return delta
//        }
//
//    fun initialize(){
//        lastFrameTime = System.currentTimeMillis() // Инициализируем время при старте игры
//    }
//}
//
//open class GameObject(var x: Float, var y: Float){
//    open val speed: Float = 50f // 50 пикселей в секунду
//}
//
//
//
//
//















