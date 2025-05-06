package com.example.accounting.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.accounting.R
import com.example.accounting.models.ExpenseRecord
import com.example.accounting.utils.DateUtils
import java.util.*

/**
 * 记账记录列表适配器，支持日期分组
 */
class RecordAdapter(private var records: List<ExpenseRecord>) : 
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_RECORD = 1
    }
    
    private var processedItems: List<Any> = processRecords(records)
    
    // 类别图标映射
    private val categoryIcons = mapOf(
        "餐饮" to R.drawable.ic_category_food,
        "交通" to R.drawable.ic_category_transport,
        "购物" to R.drawable.ic_category_shopping,
        "娱乐" to R.drawable.ic_category_entertainment,
        "医疗" to R.drawable.ic_category_medical,
        "住房" to R.drawable.ic_category_house,
        "通讯" to R.drawable.ic_category_communication,
        "其他" to R.drawable.ic_category_other
    )
    
    /**
     * 日期分组标题ViewHolder
     */
    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textDate: TextView = itemView.findViewById(R.id.tv_date_header)
        val textWeekday: TextView = itemView.findViewById(R.id.tv_weekday)
        val textDayIncome: TextView = itemView.findViewById(R.id.tv_day_income_total)
        val textDayExpense: TextView = itemView.findViewById(R.id.tv_day_expense_total)
    }
    
    /**
     * 记录ViewHolder
     */
    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageCategory: ImageView = itemView.findViewById(R.id.image_category)
        val textCategory: TextView = itemView.findViewById(R.id.text_category)
        val textAmount: TextView = itemView.findViewById(R.id.text_amount)
        val textTime: TextView = itemView.findViewById(R.id.text_date)
        val textMerchant: TextView = itemView.findViewById(R.id.text_merchant)
        val textPayMethod: TextView = itemView.findViewById(R.id.text_pay_method)
    }

    override fun getItemViewType(position: Int): Int {
        return when (processedItems[position]) {
            is DateHeader -> VIEW_TYPE_HEADER
            else -> VIEW_TYPE_RECORD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_record, parent, false)
                RecordViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = processedItems[position]) {
            is DateHeader -> {
                if (holder is DateHeaderViewHolder) {
                    // 设置日期标题
                    holder.textDate.text = item.dateText
                    holder.textWeekday.text = item.weekdayText
                    
                    // 设置日总收支
                    holder.textDayIncome.text = if (item.income > 0) String.format("收 %.2f", item.income) else ""
                    holder.textDayExpense.text = if (item.expense > 0) String.format("支 %.2f", item.expense) else ""
                }
            }
            is ExpenseRecord -> {
                if (holder is RecordViewHolder) {
                    // 设置类别图标
                    val iconResId = categoryIcons[item.category] ?: R.drawable.ic_category_other
                    holder.imageCategory.setImageResource(iconResId)
                    
                    // 设置类别文本
                    holder.textCategory.text = item.category
                    
                    // 设置金额
                    holder.textAmount.text = String.format("-¥%.2f", item.amount)
                    
                    // 设置时间 - 只显示时间部分，因为日期已经在Header中显示
                    holder.textTime.text = DateUtils.formatTime(item.date)
                    
                    // 设置商户
                    holder.textMerchant.text = item.merchant
                    
                    // 设置支付方式
                    holder.textPayMethod.text = item.payMethod
                }
            }
        }
    }

    override fun getItemCount(): Int = processedItems.size
    
    /**
     * 更新记录列表
     */
    fun updateRecords(newRecords: List<ExpenseRecord>) {
        this.records = newRecords
        this.processedItems = processRecords(newRecords)
        notifyDataSetChanged()
    }
    
    /**
     * 处理记录列表，添加日期分组标题
     */
    private fun processRecords(records: List<ExpenseRecord>): List<Any> {
        if (records.isEmpty()) return emptyList()
        
        val result = mutableListOf<Any>()
        
        // 按日期分组
        val groupedByDate = records.groupBy { record -> 
            DateUtils.formatDate(record.date)
        }
        
        // 对日期进行排序（倒序）
        val sortedDates = groupedByDate.keys.sortedByDescending { dateStr ->
            try {
                val parts = dateStr.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                year * 10000 + month * 100 + day
            } catch (e: Exception) {
                0
            }
        }
        
        // 为每个日期组添加标题和记录
        sortedDates.forEach { dateStr ->
            val recordsInDate = groupedByDate[dateStr] ?: return@forEach
            
            // 计算日期组的总收支
            var dayIncome = 0.0
            var dayExpense = 0.0
            recordsInDate.forEach { record ->
                if (record.amount > 0) {
                    dayIncome += record.amount
                } else {
                    dayExpense += record.amount
                }
            }
            
            // 获取日期样式
            val firstRecord = recordsInDate.firstOrNull() ?: return@forEach
            val weekday = getWeekdayText(firstRecord.date)
            
            val isToday = DateUtils.isToday(firstRecord.date)
            val isYesterday = DateUtils.isYesterday(firstRecord.date)
            
            val dateText = when {
                isToday -> "今天"
                isYesterday -> "昨天"
                else -> dateStr
            }
            
            // 添加日期标题
            result.add(DateHeader(dateText, weekday, dayIncome, Math.abs(dayExpense)))
            
            // 添加当日所有记录
            result.addAll(recordsInDate)
        }
        
        return result
    }
    
    /**
     * 获取星期几文本
     */
    private fun getWeekdayText(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "周日"
            Calendar.MONDAY -> "周一"
            Calendar.TUESDAY -> "周二"
            Calendar.WEDNESDAY -> "周三"
            Calendar.THURSDAY -> "周四"
            Calendar.FRIDAY -> "周五"
            Calendar.SATURDAY -> "周六"
            else -> ""
        }
    }
    
    /**
     * 日期分组标题数据类
     */
    data class DateHeader(
        val dateText: String,
        val weekdayText: String,
        val income: Double,
        val expense: Double
    )
} 