package com.example.accounting.repositories

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.accounting.models.ExpenseRecord
import java.util.*

/**
 * 记账记录数据访问对象接口
 */
@Dao
interface RecordDao {
    
    @Insert
    fun insert(record: ExpenseRecord)
    
    @Update
    fun update(record: ExpenseRecord)
    
    @Delete
    fun delete(record: ExpenseRecord)
    
    @Query("SELECT * FROM expense_records ORDER BY date DESC")
    fun getAllRecords(): LiveData<List<ExpenseRecord>>
    
    @Query("SELECT * FROM expense_records WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getRecordsByDateRange(start: Date, end: Date): LiveData<List<ExpenseRecord>>
    
    /**
     * 同步获取日期范围内的记录（非LiveData）
     * 用于缓存加载
     */
    @Query("SELECT * FROM expense_records WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getRecordsByDateRangeSync(start: Date, end: Date): List<ExpenseRecord>
    
    @Query("SELECT * FROM expense_records WHERE category = :category ORDER BY date DESC")
    fun getRecordsByCategory(category: String): LiveData<List<ExpenseRecord>>
    
    @Query("SELECT * FROM expense_records WHERE payMethod = :payMethod ORDER BY date DESC")
    fun getRecordsByPayMethod(payMethod: String): LiveData<List<ExpenseRecord>>
    
    @Query("SELECT SUM(amount) FROM expense_records WHERE date BETWEEN :start AND :end")
    fun getTotalExpense(start: Date, end: Date): LiveData<Double>
    
    @Query("SELECT category, SUM(amount) as total FROM expense_records WHERE date BETWEEN :start AND :end GROUP BY category")
    fun getCategoryExpensesRaw(start: Date, end: Date): LiveData<List<CategoryExpense>>
    
    // 该方法需要在RecordRepository中实现，不是数据库直接查询
    // 因此将其从DAO接口中移除，改为在Repository中定义
}

/**
 * 类别消费统计结果类
 */
data class CategoryExpense(
    val category: String,
    val total: Double
) 