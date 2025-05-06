package com.example.accounting.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.accounting.R
import com.example.accounting.components.RecordAdapter
import com.example.accounting.utils.DateUtils
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var viewModel: CalendarViewModel
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var recordAdapter: RecordAdapter
    
    private lateinit var tvCurrentMonth: TextView
    private lateinit var tvMonthIncome: TextView
    private lateinit var tvMonthExpense: TextView
    private lateinit var tvMonthBalance: TextView
    private lateinit var recyclerCalendar: RecyclerView
    private lateinit var recyclerDayRecords: RecyclerView
    private lateinit var layoutNoRecords: View
    private lateinit var tvNoRecordsHint: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        
        // 初始化视图
        initViews(view)
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this).get(CalendarViewModel::class.java)
        
        // 设置日历RecyclerView
        setupCalendarRecyclerView()
        
        // 设置日记录RecyclerView
        setupDayRecordsRecyclerView()
        
        // 观察ViewModel数据变化
        observeViewModelData()
        
        return view
    }
    
    private fun initViews(view: View) {
        tvCurrentMonth = view.findViewById(R.id.tv_current_month)
        tvMonthIncome = view.findViewById(R.id.tv_month_income_calendar)
        tvMonthExpense = view.findViewById(R.id.tv_month_expense_calendar)
        tvMonthBalance = view.findViewById(R.id.tv_month_balance_calendar)
        recyclerCalendar = view.findViewById(R.id.recycler_calendar)
        recyclerDayRecords = view.findViewById(R.id.recycler_day_records)
        layoutNoRecords = view.findViewById(R.id.layout_no_records)
        tvNoRecordsHint = view.findViewById(R.id.tv_no_records_hint)
        
        // 设置今日按钮点击事件
        view.findViewById<TextView>(R.id.tv_today).setOnClickListener {
            viewModel.navigateToToday()
        }
        
        // 设置添加记录按钮点击事件
        view.findViewById<View>(R.id.iv_add_record).setOnClickListener {
            // TODO: 启动添加记录对话框或Activity
        }
    }
    
    private fun setupCalendarRecyclerView() {
        calendarAdapter = CalendarAdapter { date ->
            // 日历日期点击回调
            viewModel.selectDate(date)
        }
        
        recyclerCalendar.apply {
            layoutManager = GridLayoutManager(context, 7) // 7列网格
            adapter = calendarAdapter
        }
    }
    
    private fun setupDayRecordsRecyclerView() {
        recordAdapter = RecordAdapter(emptyList())
        
        recyclerDayRecords.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordAdapter
        }
    }
    
    private fun observeViewModelData() {
        // 观察当前月
        viewModel.currentMonthText.observe(viewLifecycleOwner) { monthText ->
            tvCurrentMonth.text = monthText
        }
        
        // 观察月度收支数据
        viewModel.monthlySummary.observe(viewLifecycleOwner) { summary ->
            if (summary != null) {
                tvMonthIncome.text = String.format("%.2f", summary.income)
                tvMonthExpense.text = String.format("%.2f", summary.expense)
                tvMonthBalance.text = String.format("%.2f", summary.balance)
            }
        }
        
        // 观察日历数据
        viewModel.calendarDays.observe(viewLifecycleOwner) { days ->
            if (days != null) {
                calendarAdapter.updateCalendarDays(days)
            }
        }
        
        // 观察所选日期的记录
        viewModel.selectedDayRecords.observe(viewLifecycleOwner) { records ->
            if (records == null || records.isEmpty()) {
                recyclerDayRecords.visibility = View.GONE
                layoutNoRecords.visibility = View.VISIBLE
                
                // 更新没有记录的提示文本
                viewModel.selectedDateText.observe(viewLifecycleOwner) { dateText ->
                    if (dateText != null) {
                        tvNoRecordsHint.text = "${dateText}你还没有任何记账"
                    }
                }
            } else {
                recyclerDayRecords.visibility = View.VISIBLE
                layoutNoRecords.visibility = View.GONE
                
                // 更新记录列表
                recordAdapter.updateRecords(records)
            }
        }
    }
    
    /**
     * 日历适配器
     */
    inner class CalendarAdapter(private val onDateClick: (Date) -> Unit) : 
        RecyclerView.Adapter<CalendarAdapter.CalendarDayViewHolder>() {
        
        private var calendarDays: List<CalendarDay> = emptyList()
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calendar_day, parent, false)
            return CalendarDayViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: CalendarDayViewHolder, position: Int) {
            val calendarDay = calendarDays[position]
            
            // 设置日期
            holder.tvDayNumber.text = calendarDay.dayNumber
            
            // 设置农历日期
            holder.tvLunarDay.text = calendarDay.lunarDay
            
            // 设置支出金额
            if (calendarDay.expense > 0) {
                holder.tvDayExpense.text = String.format("-%.2f", calendarDay.expense)
                holder.tvDayExpense.visibility = View.VISIBLE
            } else {
                holder.tvDayExpense.visibility = View.GONE
            }
            
            // 设置收入金额
            if (calendarDay.income > 0) {
                holder.tvDayIncome.text = String.format("+%.2f", calendarDay.income)
                holder.tvDayIncome.visibility = View.VISIBLE
            } else {
                holder.tvDayIncome.visibility = View.GONE
            }
            
            // 设置选中状态
            (holder.container as CardView).setCardBackgroundColor(
                if (calendarDay.isSelected) 
                    holder.itemView.context.getColor(R.color.primary_light)
                else 
                    holder.itemView.context.getColor(R.color.card_background)
            )
            
            // 设置日期文本颜色
            holder.tvDayNumber.setTextColor(
                if (calendarDay.isCurrentMonth) 
                    holder.itemView.context.getColor(R.color.text_primary)
                else 
                    holder.itemView.context.getColor(R.color.text_hint)
            )
            
            // 设置点击事件
            holder.container.setOnClickListener {
                onDateClick(calendarDay.date)
            }
        }
        
        override fun getItemCount(): Int = calendarDays.size
        
        fun updateCalendarDays(days: List<CalendarDay>) {
            this.calendarDays = days
            notifyDataSetChanged()
        }
        
        inner class CalendarDayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val container = itemView.findViewById<View>(R.id.cv_day_container)
            val tvDayNumber = itemView.findViewById<TextView>(R.id.tv_day_number)
            val tvLunarDay = itemView.findViewById<TextView>(R.id.tv_lunar_day)
            val tvDayExpense = itemView.findViewById<TextView>(R.id.tv_day_expense)
            val tvDayIncome = itemView.findViewById<TextView>(R.id.tv_day_income)
        }
    }
} 