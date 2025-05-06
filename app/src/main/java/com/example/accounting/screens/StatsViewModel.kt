package com.example.accounting.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.accounting.models.ExpenseRecord
import com.example.accounting.repositories.RecordRepository
import com.example.accounting.utils.DateUtils
import java.util.*
import kotlin.math.abs

/**
 * 统计界面ViewModel
 */
class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val recordRepository = RecordRepository.getInstance(application)
    
    // 选择的月份
    private val _selectedMonth = MutableLiveData<Date>(Date())
    
    // 显示支出还是收入
    private val _showExpense = MutableLiveData<Boolean>(true)
    
    // 显示一级分类还是全部分类
    private val _showPrimaryCategory = MutableLiveData<Boolean>(true)
    
    // 月度收支摘要数据
    private val _monthSummaryData = MutableLiveData<MonthSummary>()
    val monthSummaryData: LiveData<MonthSummary> = _monthSummaryData
    
    // 每日收支数据
    private val _dailyData = MutableLiveData<List<BarChartData>>()
    val dailyData: LiveData<List<BarChartData>> = _dailyData
    
    // 资产走势数据
    private val _assetTrendData = MutableLiveData<List<LineChartData>>()
    val assetTrendData: LiveData<List<LineChartData>> = _assetTrendData
    
    // 分类占比数据
    private val _categoryData = MutableLiveData<List<PieChartData>>()
    val categoryData: LiveData<List<PieChartData>> = _categoryData
    
    init {
        // 初始化数据
        updateMonthSummary()
        updateDailyData()
        updateAssetTrend()
        updateCategoryData()
    }
    
    /**
     * 设置选择的月份
     */
    fun setSelectedMonth(date: Date) {
        _selectedMonth.value = date
        
        // 更新所有数据
        updateMonthSummary()
        updateDailyData()
        updateAssetTrend()
        updateCategoryData()
    }
    
    /**
     * 设置是否显示支出
     */
    fun setShowExpense(showExpense: Boolean) {
        if (_showExpense.value != showExpense) {
            _showExpense.value = showExpense
            
            // 更新分类统计数据
            updateCategoryData()
        }
    }
    
    /**
     * 设置是否显示一级分类
     */
    fun setShowPrimaryCategory(showPrimaryCategory: Boolean) {
        if (_showPrimaryCategory.value != showPrimaryCategory) {
            _showPrimaryCategory.value = showPrimaryCategory
            
            // 更新分类统计数据
            updateCategoryData()
        }
    }
    
    /**
     * 更新月度收支摘要
     */
    private fun updateMonthSummary() {
        val monthDate = _selectedMonth.value ?: Date()
        val startOfMonth = DateUtils.getMonthStart(monthDate)
        val endOfMonth = DateUtils.getMonthEnd(monthDate)
        
        recordRepository.getRecordsByDateRange(startOfMonth, endOfMonth).observeForever { records ->
            _monthSummaryData.value = calculateMonthSummary(records)
        }
    }
    
    /**
     * 更新每日收支数据
     */
    private fun updateDailyData() {
        val monthDate = _selectedMonth.value ?: Date()
        val startOfMonth = DateUtils.getMonthStart(monthDate)
        val endOfMonth = DateUtils.getMonthEnd(monthDate)
        
        recordRepository.getRecordsByDateRange(startOfMonth, endOfMonth).observeForever { records ->
            _dailyData.value = calculateDailyData(records, monthDate)
        }
    }
    
    /**
     * 更新资产走势数据
     */
    private fun updateAssetTrend() {
        val monthDate = _selectedMonth.value ?: Date()
        val calendar = Calendar.getInstance().apply { time = monthDate }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        
        // 取最近3个月的数据
        calendar.set(year, month - 2, 1)
        val startDate = DateUtils.getMonthStart(calendar.time)
        
        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = DateUtils.getDayEnd(calendar.time)
        
        recordRepository.getRecordsByDateRange(startDate, endDate).observeForever { records ->
            _assetTrendData.value = calculateAssetTrend(records, startDate, endDate)
        }
    }
    
    /**
     * 更新分类占比数据
     */
    private fun updateCategoryData() {
        val monthDate = _selectedMonth.value ?: Date()
        val startOfMonth = DateUtils.getMonthStart(monthDate)
        val endOfMonth = DateUtils.getMonthEnd(monthDate)
        
        recordRepository.getRecordsByDateRange(startOfMonth, endOfMonth).observeForever { records ->
            val showExpense = _showExpense.value ?: true
            val showPrimaryCategory = _showPrimaryCategory.value ?: true
            
            _categoryData.value = calculateCategoryData(records, showExpense, showPrimaryCategory)
        }
    }
    
    /**
     * 计算月度汇总数据
     */
    private fun calculateMonthSummary(records: List<ExpenseRecord>): MonthSummary {
        var income = 0.0
        var expense = 0.0
        
        records.forEach { record ->
            if (record.amount > 0) {
                income += record.amount
            } else {
                expense += record.amount
            }
        }
        
        return MonthSummary(income, abs(expense), income + expense)
    }
    
    /**
     * 计算每日收支数据
     */
    private fun calculateDailyData(records: List<ExpenseRecord>, monthDate: Date): List<BarChartData> {
        val calendar = Calendar.getInstance().apply { time = monthDate }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // 初始化每日数据
        val dailyData = ArrayList<BarChartData>()
        for (day in 1..daysInMonth) {
            calendar.set(year, month, day)
            
            // 只取10天，避免x轴拥挤
            if (day <= 10 || day % 3 == 0 || day == daysInMonth) {
                val dayStr = day.toString()
                dailyData.add(BarChartData(dayStr, 0.0, 0.0))
            }
        }
        
        // 聚合收支数据
        records.forEach { record ->
            val recordDate = Calendar.getInstance().apply { time = record.date }
            val recordDay = recordDate.get(Calendar.DAY_OF_MONTH)
            
            val index = dailyData.indexOfFirst { it.day == recordDay.toString() }
            if (index != -1) {
                val data = dailyData[index]
                if (record.amount > 0) {
                    dailyData[index] = data.copy(income = data.income + record.amount)
                } else {
                    dailyData[index] = data.copy(expense = data.expense + record.amount)
                }
            }
        }
        
        return dailyData
    }
    
    /**
     * 计算资产趋势数据
     */
    private fun calculateAssetTrend(records: List<ExpenseRecord>, startDate: Date, endDate: Date): List<LineChartData> {
        val result = ArrayList<LineChartData>()
        
        val startCalendar = Calendar.getInstance().apply { time = startDate }
        val endCalendar = Calendar.getInstance().apply { time = endDate }
        
        val startMonth = startCalendar.get(Calendar.MONTH)
        val endMonth = endCalendar.get(Calendar.MONTH)
        
        // 按月生成数据点
        var currentBalance = 0.0
        for (month in startMonth..endMonth) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH, month)
            
            val monthStr = when (month) {
                Calendar.JANUARY -> "1月"
                Calendar.FEBRUARY -> "2月"
                Calendar.MARCH -> "3月"
                Calendar.APRIL -> "4月"
                Calendar.MAY -> "5月"
                Calendar.JUNE -> "6月"
                Calendar.JULY -> "7月"
                Calendar.AUGUST -> "8月"
                Calendar.SEPTEMBER -> "9月"
                Calendar.OCTOBER -> "10月"
                Calendar.NOVEMBER -> "11月"
                Calendar.DECEMBER -> "12月"
                else -> "${month + 1}月"
            }
            
            result.add(LineChartData(monthStr, currentBalance))
            
            // 计算该月收支
            val monthStart = DateUtils.getMonthStart(calendar.time)
            val monthEnd = DateUtils.getMonthEnd(calendar.time)
            
            var monthlyBalance = 0.0
            records.forEach { record ->
                if (record.date.time >= monthStart.time && record.date.time <= monthEnd.time) {
                    monthlyBalance += record.amount
                }
            }
            
            currentBalance += monthlyBalance
        }
        
        return result
    }
    
    /**
     * 计算类别占比数据
     */
    private fun calculateCategoryData(records: List<ExpenseRecord>, showExpense: Boolean, showPrimaryCategory: Boolean): List<PieChartData> {
        val categoryMap = mutableMapOf<String, Double>()
        
        records.forEach { record ->
            val amount = record.amount
            // 根据显示支出还是收入来过滤数据
            if ((showExpense && amount < 0) || (!showExpense && amount > 0)) {
                val category = record.category
                val currentAmount = categoryMap[category] ?: 0.0
                categoryMap[category] = currentAmount + abs(amount)
            }
        }
        
        // 最多显示前8个类别，其余归为"其他"
        if (categoryMap.size > 8) {
            val sortedCategories = categoryMap.entries.sortedByDescending { it.value }
            val top7 = sortedCategories.take(7).associate { it.key to it.value }
            
            val otherCategories = sortedCategories.drop(7)
            val otherTotal = otherCategories.sumOf { it.value }
            
            val result = top7.toMutableMap()
            result["其他"] = otherTotal
            
            return result.map { (category, amount) -> PieChartData(category, amount) }
        }
        
        return categoryMap.map { (category, amount) -> PieChartData(category, amount) }
    }
    
    /**
     * 月度汇总数据类
     */
    data class MonthSummary(
        val income: Double,      // 收入
        val expense: Double,     // 支出
        val balance: Double      // 结余
    )
}

/**
 * 柱状图数据类
 */
data class BarChartData(
    val day: String,        // 日期标签
    val income: Double,     // 收入
    val expense: Double     // 支出
)

/**
 * 折线图数据类
 */
data class LineChartData(
    val date: String,      // 日期标签
    val balance: Double    // 账户余额
)

/**
 * 饼图数据类
 */
data class PieChartData(
    val category: String,  // 类别
    val amount: Double     // 金额
) 