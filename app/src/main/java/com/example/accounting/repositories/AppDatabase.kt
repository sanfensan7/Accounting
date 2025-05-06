package com.example.accounting.repositories

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.accounting.models.ExpenseRecord
import com.example.accounting.utils.DateConverter

/**
 * 应用数据库类
 */
@Database(entities = [ExpenseRecord::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
} 