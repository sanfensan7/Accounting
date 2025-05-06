package com.example.accounting.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.accounting.R
import com.example.accounting.components.RecordAdapter
import com.example.accounting.models.ExpenseRecord
import com.example.accounting.repositories.RecordRepository
import com.example.accounting.utils.DateUtils
import java.util.*

/**
 * 首页Fragment
 * 显示收支概览和最近记账记录
 */
class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var recordAdapter: RecordAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        
        // 初始化RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_recent_records)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        // 设置适配器
        recordAdapter = RecordAdapter(emptyList())
        recyclerView.adapter = recordAdapter
        
        // 观察最近记录变化
        viewModel.getRecentRecords().observe(viewLifecycleOwner) { records ->
            recordAdapter.updateRecords(records)
        }
        
        // 观察今日支出数据
        viewModel.getTodayExpense().observe(viewLifecycleOwner) { todayExpense ->
            view.findViewById<TextView>(R.id.tv_today_expense)?.text = 
                String.format("¥%.2f", todayExpense)
        }
        
        // 观察月度统计数据
        viewModel.getMonthSummary().observe(viewLifecycleOwner) { summary ->
            view.findViewById<TextView>(R.id.tv_month_expense)?.text = 
                String.format("¥%.2f", summary.totalExpense)
        }
        
        return view
    }
}