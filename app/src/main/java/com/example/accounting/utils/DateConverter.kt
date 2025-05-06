package com.example.accounting.utils

import androidx.room.TypeConverter
import java.util.*

/**
 * Room数据库日期转换器
 * 用于在Date和Long类型之间转换
 */
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
} 