

abstract class User(
    val id: Int,
    val listName: String,
    val name: String,
    val birthDay: String,
    val email: String,
    private val password: String
) {
    fun getFIO() = "$listName $name"
    fun login(pwd: String): Boolean {
        val success = pwd == password
        if (success) println(" ${getFIO()} вошёл в систему.")
        else println(" Неверный пароль.")
        return success
    }
    fun logout() = println(" ${getFIO()} завершил сеанс.")
}

class Student(
    id: Int, listName: String, name: String, birthDay: String, email: String, pwd: String,
    val group: String, val course: Int, val studyForm: String
) : User(id, listName, name, birthDay, email, pwd)

class Teacher(
    id: Int, listName: String, name: String, birthDay: String, email: String, pwd: String,
    val position: String, val department: String
) : User(id, listName, name, birthDay, email, pwd) {
    fun setGrade(record: AcademicRecordItem, grade: Int) = record.review(grade)
    fun markPresence(record: AcademicRecordItem) = println("Преподаватель отметил посещение.")
}

class Admin(
    id: Int, listName: String, name: String, birthDay: String, email: String, pwd: String,
    val faculty: String
) : User(id, listName, name, birthDay, email, pwd) {
    fun addSchedule(schedule: Schedule, item: ScheduleItem) {
        schedule.addItem(item)
        println("Администратор добавил элемент в расписание.")
    }
    fun addNews(feed: NewsFeed, item: NewsItem) {
        feed.addItem(item)
        println("Администратор опубликовал новость.")
    }
}


class Schedule {
    private val items = mutableListOf<ScheduleItem>()
    fun addItem(item: ScheduleItem) = items.add(item)
    fun getItems() = items.toList()
}

class ScheduleItem(val subject: String, val day: String, val room: String, val teacher: Teacher) {
    override fun toString() = "$subject ($day, $room) | Преподаватель: ${teacher.getFIO()}"
}

class NewsFeed {
    private val items = mutableListOf<NewsItem>()
    fun addItem(item: NewsItem) = items.add(item)
    fun getItems() = items.toList()
}

class NewsItem(val title: String, val content: String, val date: String) {
    override fun toString() = "[$date] $title: $content"
}

class AcademicJournal {
    private val records = mutableListOf<AcademicRecordItem>()
    fun addRecord(rec: AcademicRecordItem) = records.add(rec)
    fun getRecords() = records.toList()
}


enum class RecordState { CREATED, WAITING_SUBMISSION, UNDER_REVIEW, GRADE_ASSIGNED, WAITING_RETAKE, COMPLETED, DISMISSED }

class AcademicRecordItem(val student: Student, val subject: String) {
    var state: RecordState = RecordState.CREATED
    var grade: Int? = null
        private set
    var attempts: Int = 0
        private set

    init {
        state = RecordState.WAITING_SUBMISSION
    }

    fun submit() {
        if (state == RecordState.WAITING_SUBMISSION) {
            state = RecordState.UNDER_REVIEW
            println("Студент сдал работу. Состояние: на проверке")
        } else println("Невозможно сдать работу в текущем состоянии: $state")
    }

    fun review(grade: Int) {
        if (state != RecordState.UNDER_REVIEW) { println("Нельзя выставить оценку сейчас."); return }
        this.grade = grade
        attempts++
        state = RecordState.GRADE_ASSIGNED
        println("Оценка выставлена: $grade. Попытка: $attempts")
        processGuardConditions()
    }

    private fun processGuardConditions() {
        when {
            grade!! > 3 -> {
                state = RecordState.COMPLETED
                println("Оценка > 3. Жизненный цикл завершён успешно.")
            }
            attempts < 3 -> {
                state = RecordState.WAITING_RETAKE
                println("Оценка < 3. Попыток осталось: ${3 - attempts}. Переход в пересдача.")
            }
            else -> {
                state = RecordState.DISMISSED
                println("Не сдано 3 раза. Студент отчислен.")
            }
        }
    }

    fun startRetake() {
        if (state == RecordState.WAITING_RETAKE) {
            state = RecordState.WAITING_SUBMISSION
            println("Начата пересдача. Состояние: ОЖИДАЕТ СДАЧИ")
        } else println("Пересдача недоступна в текущем состоянии.")
    }
}


fun main() {
    println("Система 'Обучение студента в университете'")
    val admin = Admin(1, "Иванов", "Админ", "01.01.1980", "admin@uni.ru", "admin123", "ФИТ")
    val teacher = Teacher(2, "Петров", "Препод", "05.05.1975", "petrov@uni.ru", "teach123", "Доцент", "Каф. ПО")
    val student = Student(3, "Сидоров", "Студент", "12.12.2000", "sid@uni.ru", "stud123", "ПО-101", 2, "Очная")

    val schedule = Schedule()
    val newsFeed = NewsFeed()
    val journal = AcademicJournal()


    admin.addSchedule(schedule, ScheduleItem("ТСПП", "Пн", "305", teacher))
    admin.addNews(newsFeed, NewsItem("Каникулы", "Зимние каникулы с 20.12", "10.12.2023"))

    val record = AcademicRecordItem(student, "ТСПП")
    journal.addRecord(record)

    while (true) {
        println("\nТекущее состояние заявки: ${record.state}")
        println("1. Сдать работу (студент)")
        println("2. Выставить оценку (преподаватель)")
        println("3. Начать пересдачу")
        println("4. Выйти")
        print("Выбор: ")
        when (readlnOrNull()?.trim()) {
            "1" -> record.submit()
            "2" -> {
                print("Введите оценку (2-5): ")
                readlnOrNull()?.toIntOrNull()?.let { teacher.setGrade(record, it) }
            }
            "3" -> record.startRetake()
            "4" -> break
            else -> println("Неверный ввод")
        }
    }
}