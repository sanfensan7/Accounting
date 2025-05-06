package com.example.accounting.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.accounting.models.ExpenseRecord
import com.example.accounting.repositories.RecordRepository
import com.example.accounting.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日历界面ViewModel
 */
class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val recordRepository = RecordRepository.getInstance(application)
    
    // 当前选中的日期
    private val _selectedDate = MutableLiveData<Date>(Date())
    val selectedDate: LiveData<Date> = _selectedDate
    
    // 当前选中的月份
    private val _currentMonth = MutableLiveData<Date>(Date())
    
    // 当前月份文本
    private val _currentMonthText = MutableLiveData<String>()
    val currentMonthText: LiveData<String> = _currentMonthText
    
    // 选中日期的文本表示
    private val _selectedDateText = MutableLiveData<String>()
    val selectedDateText: LiveData<String> = _selectedDateText
    
    // 月度收支摘要
    private val _monthlySummary = MutableLiveData<MonthlySummary>()
    val monthlySummary: LiveData<MonthlySummary> = _monthlySummary
    
    // 日历天数据
    private val _calendarDays = MutableLiveData<List<CalendarDay>>()
    val calendarDays: LiveData<List<CalendarDay>> = _calendarDays
    
    // 选中日期的记录
    private val _selectedDayRecords = MutableLiveData<List<ExpenseRecord>>()
    val selectedDayRecords: LiveData<List<ExpenseRecord>> = _selectedDayRecords
    
    init {
        // 初始化数据
        updateCurrentMonthText()
        updateSelectedDateText()
        updateMonthlySummary()
        updateCalendarDays()
        updateSelectedDayRecords()
    }
    
    /**
     * 选择日期
     */
    fun selectDate(date: Date) {
        _selectedDate.value = date
        
        // 如果所选日期不在当前显示的月份，更新当前月份
        val selectedCalendar = Calendar.getInstance().apply { time = date }
        val currentCalendar = Calendar.getInstance().apply { time = _currentMonth.value ?: Date() }
        
        if (selectedCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR) ||
            selectedCalendar.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH)) {
            _currentMonth.value = date
            updateCurrentMonthText()
            updateMonthlySummary()
            updateCalendarDays()
        }
        
        updateSelectedDateText()
        updateSelectedDayRecords()
    }
    
    /**
     * 导航到今天
     */
    fun navigateToToday() {
        val today = Date()
        _selectedDate.value = today
        _currentMonth.value = today
        
        updateCurrentMonthText()
        updateSelectedDateText()
        updateMonthlySummary()
        updateCalendarDays()
        updateSelectedDayRecords()
    }
    
    /**
     * 导航到前一个月
     */
    fun navigateToPreviousMonth() {
        val calendar = Calendar.getInstance().apply { 
            time = _currentMonth.value ?: Date()
            add(Calendar.MONTH, -1)
        }
        _currentMonth.value = calendar.time
        
        updateCurrentMonthText()
        updateMonthlySummary()
        updateCalendarDays()
    }
    
    /**
     * 导航到后一个月
     */
    fun navigateToNextMonth() {
        val calendar = Calendar.getInstance().apply { 
            time = _currentMonth.value ?: Date()
            add(Calendar.MONTH, 1)
        }
        _currentMonth.value = calendar.time
        
        updateCurrentMonthText()
        updateMonthlySummary()
        updateCalendarDays()
    }
    
    /**
     * 更新当前月份文本
     */
    private fun updateCurrentMonthText() {
        val dateFormat = SimpleDateFormat("yyyy年M月", Locale.getDefault())
        _currentMonthText.value = dateFormat.format(_currentMonth.value ?: Date())
    }
    
    /**
     * 更新选中日期文本
     */
    private fun updateSelectedDateText() {
        val dateFormat = SimpleDateFormat("yyyy年M月d日", Locale.getDefault())
        _selectedDateText.value = dateFormat.format(_selectedDate.value ?: Date())
    }
    
    /**
     * 更新月度收支摘要
     */
    private fun updateMonthlySummary() {
        val date = _currentMonth.value ?: Date()
        val startOfMonth = DateUtils.getMonthStart(date)
        val endOfMonth = DateUtils.getMonthEnd(date)
        
        recordRepository.getRecordsByDateRange(startOfMonth, endOfMonth).observeForever { records ->
            _monthlySummary.value = calculateMonthlySummary(records)
        }
    }
    
    /**
     * 更新日历天数据
     */
    private fun updateCalendarDays() {
        _calendarDays.value = generateCalendarDays(
            _currentMonth.value ?: Date(),
            _selectedDate.value ?: Date()
        )
    }
    
    /**
     * 更新选中日期的记录
     */
    private fun updateSelectedDayRecords() {
        val date = _selectedDate.value ?: Date()
        val startOfDay = DateUtils.getDayStart(date)
        val endOfDay = DateUtils.getDayEnd(date)
        
        recordRepository.getRecordsByDateRange(startOfDay, endOfDay).observeForever { records ->
            _selectedDayRecords.value = records
        }
    }
    
    /**
     * 计算月度收支摘要
     */
    private fun calculateMonthlySummary(records: List<ExpenseRecord>): MonthlySummary {
        var income = 0.0
        var expense = 0.0
        
        records.forEach { record ->
            if (record.amount > 0) {
                income += record.amount
            } else {
                expense += record.amount
            }
        }
        
        return MonthlySummary(income, Math.abs(expense), income + expense)
    }
    
    /**
     * 生成月历数据
     */
    private fun generateCalendarDays(
        monthDate: Date,
        selectedDate: Date
    ): List<CalendarDay> {
        val result = mutableListOf<CalendarDay>()
        
        val monthCalendar = Calendar.getInstance().apply { time = monthDate }
        val selectedCalendar = Calendar.getInstance().apply { time = selectedDate }
        val today = Calendar.getInstance()
        
        val year = monthCalendar.get(Calendar.YEAR)
        val month = monthCalendar.get(Calendar.MONTH)
        
        // 获取这个月的第一天是星期几
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK)
        
        // 获取这个月的天数
        val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // 获取上个月的天数
        monthCalendar.add(Calendar.MONTH, -1)
        val daysInPreviousMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        monthCalendar.add(Calendar.MONTH, 1) // 重新设置为当前月
        
        // 添加上个月的尾部日期
        for (i in 0 until firstDayOfWeek - 1) {
            val dayNumber = daysInPreviousMonth - (firstDayOfWeek - 2) + i
            
            val dayCalendar = Calendar.getInstance().apply {
                set(year, month - 1, dayNumber)
            }
            
            result.add(
                createCalendarDay(
                    dayCalendar,
                    dayNumber.toString(),
                    isCurrentMonth = false,
                    isSelected = false,
                    isToday = false
                )
            )
        }
        
        // 添加当前月的日期
        for (i in 1..daysInMonth) {
            val dayCalendar = Calendar.getInstance().apply {
                set(year, month, i)
            }
            
            val isSelected = selectedCalendar.get(Calendar.YEAR) == year &&
                    selectedCalendar.get(Calendar.MONTH) == month &&
                    selectedCalendar.get(Calendar.DAY_OF_MONTH) == i
            
            val isToday = today.get(Calendar.YEAR) == year &&
                    today.get(Calendar.MONTH) == month &&
                    today.get(Calendar.DAY_OF_MONTH) == i
            
            // 获取这一天的记录
            val dayStart = DateUtils.getDayStart(dayCalendar.time)
            val dayEnd = DateUtils.getDayEnd(dayCalendar.time)
            val dayRecords = recordRepository.getCachedRecordsByDateRange(dayStart, dayEnd)
            
            // 计算这一天的收支
            var dayIncome = 0.0
            var dayExpense = 0.0
            
            dayRecords.forEach { record ->
                if (record.amount > 0) {
                    dayIncome += record.amount
                } else {
                    dayExpense += Math.abs(record.amount)
                }
            }
            
            // 设置节日或农历
            val lunarDay = getLunarDayForDate(year, month, i)
            
            result.add(
                CalendarDay(
                    date = dayCalendar.time,
                    dayNumber = i.toString(),
                    lunarDay = lunarDay,
                    isCurrentMonth = true,
                    isSelected = isSelected,
                    isToday = isToday,
                    income = dayIncome,
                    expense = dayExpense
                )
            )
        }
        
        // 如果不满6行，添加下个月的头部日期
        val remainingCells = 42 - result.size // 6行7列 = 42个格子
        if (remainingCells > 0) {
            for (i in 1..remainingCells) {
                val dayCalendar = Calendar.getInstance().apply {
                    set(year, month + 1, i)
                }
                
                result.add(
                    createCalendarDay(
                        dayCalendar,
                        i.toString(),
                        isCurrentMonth = false,
                        isSelected = false,
                        isToday = false
                    )
                )
            }
        }
        
        return result
    }
    
    /**
     * 创建日历日期对象
     */
    private fun createCalendarDay(
        calendar: Calendar,
        dayNumber: String,
        isCurrentMonth: Boolean,
        isSelected: Boolean,
        isToday: Boolean
    ): CalendarDay {
        // 获取农历信息
        val lunarDay = getLunarDayForDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        return CalendarDay(
            date = calendar.time,
            dayNumber = dayNumber,
            lunarDay = lunarDay,
            isCurrentMonth = isCurrentMonth,
            isSelected = isSelected,
            isToday = isToday,
            income = 0.0,
            expense = 0.0
        )
    }
    
    /**
     * 获取农历日期 (简化版，实际应使用农历算法库)
     */
    private fun getLunarDayForDate(year: Int, month: Int, day: Int): String {
        // 这里只是简单地查找是否是节日，真实应用应该使用完整的农历转换库
        val festivals = mapOf(
            "0101" to "元旦",
            "0214" to "情人节",
            "0501" to "劳动节",
            "0601" to "儿童节",
            "0910" to "教师节",
            "1001" to "国庆",
            "1225" to "圣诞"
        )
        
        val key = String.format("%02d%02d", month + 1, day)
        return festivals[key] ?: ""
    }
}

/**
 * 月度摘要数据类
 */
data class MonthlySummary(
    val income: Double,    // 收入
    val expense: Double,   // 支出
    val balance: Double    // 结余
)

/**
 * 日历日期数据类
 */
data class CalendarDay(
    val date: Date,                  // 日期
    val dayNumber: String,           // 日期数字
    val lunarDay: String,            // 农历或节日
    val isCurrentMonth: Boolean,     // 是否是当前月份
    val isSelected: Boolean,         // 是否被选中
    val isToday: Boolean,            // 是否是今天
    val income: Double,              // 当日收入
    val expense: Double              // 当日支出
) 