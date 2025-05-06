package com.example.accounting.screens

import android.app.Application
import androidx.lifecycle.*
import com.example.accounting.models.ExpenseRecord
import com.example.accounting.repositories.RecordRepository
import com.example.accounting.utils.DateUtils

/**
 * 首页ViewModel
 * 提供首页所需的数据
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val recordRepository = RecordRepository.getInstance(application)

    /**
     * 获取最近记账记录
     */
    fun getRecentRecords(): LiveData<List<ExpenseRecord>> {
        return recordRepository.getAllRecords()
    }

    /**
     * 获取今日支出
     * 使用MediatorLiveData代替map转换，更安全地处理null值
     */
    fun getTodayExpense(): LiveData<Double> {
        val startOfDay = DateUtils.getDayStart()
        val endOfDay = DateUtils.getDayEnd()
        val source = recordRepository.getTotalExpense(startOfDay, endOfDay)
        
        val result = MediatorLiveData<Double>()
        result.addSource(source) { value ->
            result.value = value ?: 0.0
        }
        return result
    }

    /**
     * 获取本月收支概览
     * 使用MediatorLiveData代替map转换，确保安全处理null值
     */
    fun getMonthSummary(): LiveData<MonthSummary> {
        val startOfMonth = DateUtils.getMonthStart()
        val endOfMonth = DateUtils.getMonthEnd()
        
        val totalExpense = recordRepository.getTotalExpense(startOfMonth, endOfMonth)
        
        val result = MediatorLiveData<MonthSummary>()
        result.addSource(totalExpense) { expense ->
            val safeExpense = expense ?: 0.0
            result.value = MonthSummary(
                monthName = DateUtils.formatMonth(startOfMonth),
                totalExpense = safeExpense,
                totalIncome = 0.0, // TODO: 添加收入统计
                balance = -safeExpense // 支出为负数，收入-支出为结余
            )
        }
        return result
    }
}

/**
 * 月度收支概览数据类
 */
data class MonthSummary(
    val monthName: String,
    val totalExpense: Double,
    val totalIncome: Double,
    val balance: Double
) 