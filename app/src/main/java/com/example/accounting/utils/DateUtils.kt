package com.example.accounting.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * 日期工具类
 */
object DateUtils {
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
    private val fullDateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val friendlyDateFormat = SimpleDateFormat("M月d日 HH:mm", Locale.getDefault())

    /**
     * 格式化日期时间
     */
    fun formatDateTime(timestamp: Long): String {
        return try {
            dateTimeFormat.format(Date(timestamp))
        } catch (e: Exception) {
            "未知时间"
        }
    }

    /**
     * 格式化日期时间
     */
    fun formatDateTime(date: Date?): String {
        return try {
            date?.let { dateTimeFormat.format(it) } ?: "未知时间"
        } catch (e: Exception) {
            "未知时间"
        }
    }

    /**
     * 仅格式化日期
     */
    fun formatDate(timestamp: Long): String {
        return try {
            dateFormat.format(Date(timestamp))
        } catch (e: Exception) {
            "未知日期"
        }
    }

    /**
     * 仅格式化日期
     */
    fun formatDate(date: Date?): String {
        return try {
            date?.let { dateFormat.format(it) } ?: "未知日期"
        } catch (e: Exception) {
            "未知日期"
        }
    }

    /**
     * 仅格式化时间
     */
    fun formatTime(timestamp: Long): String {
        return try {
            timeFormat.format(Date(timestamp))
        } catch (e: Exception) {
            "未知时间"
        }
    }

    /**
     * 仅格式化时间
     */
    fun formatTime(date: Date?): String {
        return try {
            date?.let { timeFormat.format(it) } ?: "未知时间"
        } catch (e: Exception) {
            "未知时间"
        }
    }

    /**
     * 格式化年月
     */
    fun formatMonth(date: Date?): String {
        return try {
            date?.let { monthFormat.format(it) } ?: "未知月份"
        } catch (e: Exception) {
            "未知月份"
        }
    }

    /**
     * 格式化为完整日期时间（包含秒）
     */
    fun formatFullDateTime(date: Date?): String {
        return try {
            date?.let { fullDateTimeFormat.format(it) } ?: "未知时间"
        } catch (e: Exception) {
            "未知时间"
        }
    }

    /**
     * 格式化为友好的日期时间格式
     */
    fun formatFriendlyDateTime(date: Date?): String {
        return try {
            date?.let { friendlyDateFormat.format(it) } ?: "未知时间"
        } catch (e: Exception) {
            "未知时间"
        }
    }

    /**
     * 判断日期是否是今天
     */
    fun isToday(date: Date?): Boolean {
        if (date == null) return false
        
        val todayStart = getDayStart()
        val todayEnd = getDayEnd()
        
        return date.time >= todayStart.time && date.time <= todayEnd.time
    }
    
    /**
     * 判断日期是否是昨天
     */
    fun isYesterday(date: Date?): Boolean {
        if (date == null) return false
        
        val yesterdayStart = getDayStart(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))
        val yesterdayEnd = getDayEnd(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))
        
        return date.time >= yesterdayStart.time && date.time <= yesterdayEnd.time
    }

    /**
     * 获取今天的开始时间
     */
    fun getDayStart(date: Date = Date()): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.time
    }

    /**
     * 获取今天的结束时间
     */
    fun getDayEnd(date: Date = Date()): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.time
    }

    /**
     * 获取本月的开始日期
     */
    fun getMonthStart(date: Date = Date()): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.time
    }

    /**
     * 获取本月的结束日期
     */
    fun getMonthEnd(date: Date = Date()): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.time
    }
} 