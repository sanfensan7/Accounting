package com.example.accounting.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.map
import androidx.room.Room
import com.example.accounting.models.ExpenseRecord
import com.example.accounting.utils.DateUtils
import java.util.*
import java.util.concurrent.Executors

/**
 * 记账记录仓库类
 * 提供对数据库中记账记录的访问
 */
class RecordRepository private constructor(context: Context) {

    companion object {
        private var INSTANCE: RecordRepository? = null

        fun getInstance(context: Context): RecordRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RecordRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "accounting-database"
    ).build()

    private val recordDao = database.recordDao()
    private val executor = Executors.newSingleThreadExecutor()
    
    // 记录缓存，用于日历视图快速获取
    private var cachedRecords: List<ExpenseRecord> = emptyList()
    private var lastCacheTime: Long = 0

    /**
     * 添加记账记录
     */
    fun addRecord(record: ExpenseRecord) {
        executor.execute {
            recordDao.insert(record)
            
            // 更新缓存
            invalidateCache()
        }
    }

    /**
     * 更新记账记录
     */
    fun updateRecord(record: ExpenseRecord) {
        executor.execute {
            recordDao.update(record)
            
            // 更新缓存
            invalidateCache()
        }
    }

    /**
     * 删除记账记录
     */
    fun deleteRecord(record: ExpenseRecord) {
        executor.execute {
            recordDao.delete(record)
            
            // 更新缓存
            invalidateCache()
        }
    }

    /**
     * 获取所有记账记录
     */
    fun getAllRecords(): LiveData<List<ExpenseRecord>> {
        return recordDao.getAllRecords()
    }

    /**
     * 获取今日记账记录
     */
    fun getTodayRecords(): LiveData<List<ExpenseRecord>> {
        val today = DateUtils.getDayStart()
        val endOfDay = DateUtils.getDayEnd()
        return recordDao.getRecordsByDateRange(today, endOfDay)
    }

    /**
     * 获取本月记账记录
     */
    fun getCurrentMonthRecords(): LiveData<List<ExpenseRecord>> {
        val startOfMonth = DateUtils.getMonthStart()
        val endOfMonth = DateUtils.getMonthEnd()
        return recordDao.getRecordsByDateRange(startOfMonth, endOfMonth)
    }

    /**
     * 根据日期范围获取记账记录
     */
    fun getRecordsByDateRange(start: Date, end: Date): LiveData<List<ExpenseRecord>> {
        return recordDao.getRecordsByDateRange(start, end)
    }
    
    /**
     * 根据日期范围获取缓存的记账记录
     * 用于日历视图快速获取各日期的记录，不触发数据库查询
     */
    fun getCachedRecordsByDateRange(start: Date, end: Date): List<ExpenseRecord> {
        // 如果缓存超过5分钟或为空，则刷新缓存
        if (System.currentTimeMillis() - lastCacheTime > 5 * 60 * 1000 || cachedRecords.isEmpty()) {
            refreshCache()
        }
        
        // 从缓存中筛选指定日期范围的记录
        return cachedRecords.filter { record ->
            record.date.time >= start.time && record.date.time <= end.time
        }
    }
    
    /**
     * 刷新缓存
     */
    private fun refreshCache() {
        executor.execute {
            // 获取最近3个月的数据
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -2)
            val threeMonthsAgo = DateUtils.getMonthStart(calendar.time)
            val endOfCurrentMonth = DateUtils.getMonthEnd()
            
            // 查询数据库并更新缓存
            cachedRecords = recordDao.getRecordsByDateRangeSync(threeMonthsAgo, endOfCurrentMonth)
            lastCacheTime = System.currentTimeMillis()
        }
    }
    
    /**
     * 使缓存失效
     */
    private fun invalidateCache() {
        lastCacheTime = 0
    }

    /**
     * 根据类别获取记账记录
     */
    fun getRecordsByCategory(category: String): LiveData<List<ExpenseRecord>> {
        return recordDao.getRecordsByCategory(category)
    }

    /**
     * 根据支付方式获取记账记录
     */
    fun getRecordsByPayMethod(payMethod: String): LiveData<List<ExpenseRecord>> {
        return recordDao.getRecordsByPayMethod(payMethod)
    }

    /**
     * 获取消费总额
     */
    fun getTotalExpense(start: Date, end: Date): LiveData<Double> {
        return recordDao.getTotalExpense(start, end)
    }

    /**
     * 获取各类别消费统计（以Map形式）
     * 使用MediatorLiveData替代map转换，确保安全处理null值
     */
    fun getCategoryExpenses(start: Date, end: Date): LiveData<Map<String, Double>> {
        val rawData = recordDao.getCategoryExpensesRaw(start, end)
        val result = MediatorLiveData<Map<String, Double>>()
        
        result.addSource(rawData) { categoryExpenses ->
            val map = categoryExpenses?.associate { categoryExpense -> 
                categoryExpense.category to (categoryExpense.total ?: 0.0)
            } ?: emptyMap()
            result.value = map
        }
        
        return result
    }
} 