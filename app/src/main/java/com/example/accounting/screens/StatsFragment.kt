package com.example.accounting.screens

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.accounting.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.tabs.TabLayout
import java.util.*
import kotlin.math.abs

/**
 * 统计Fragment
 * 显示月度消费统计图表和分析
 */
class StatsFragment : Fragment() {

    private lateinit var viewModel: StatsViewModel
    
    // UI组件
    private lateinit var tabLayout: TabLayout
    private lateinit var tvYear: TextView
    private lateinit var tvCurrentMonth: TextView
    private lateinit var tvLastMonth: TextView
    private lateinit var tvMonthlyExpense: TextView
    private lateinit var tvMonthlyIncome: TextView
    private lateinit var tvMonthlyBalance: TextView
    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var tvExpenseTab: TextView
    private lateinit var tvIncomeTab: TextView
    private lateinit var tvPrimaryCategory: TextView
    private lateinit var tvAllCategory: TextView

    // 是否显示支出数据(vs收入)
    private var showExpense = true
    
    // 是否显示一级分类(vs全部分类)
    private var showPrimaryCategory = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_monthly_stats, container, false)
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this).get(StatsViewModel::class.java)
        
        // 初始化UI组件
        initViews(view)
        
        // 设置图表基本属性
        setupCharts()
        
        // 观察数据变化
        observeViewModelData()
        
        return view
    }
    
    private fun initViews(view: View) {
        // Tab布局
        tabLayout = view.findViewById(R.id.tab_layout)
        
        // 日期选择器
        tvYear = view.findViewById(R.id.tv_year)
        tvCurrentMonth = view.findViewById(R.id.tv_current_month)
        tvLastMonth = view.findViewById(R.id.tv_last_month)
        
        // 月度金额摘要
        tvMonthlyExpense = view.findViewById(R.id.tv_monthly_expense)
        tvMonthlyIncome = view.findViewById(R.id.tv_monthly_income)
        tvMonthlyBalance = view.findViewById(R.id.tv_monthly_balance)
        
        // 图表
        barChart = view.findViewById(R.id.bar_chart)
        lineChart = view.findViewById(R.id.line_chart)
        pieChart = view.findViewById(R.id.pie_chart)
        
        // 切换按钮
        tvExpenseTab = view.findViewById(R.id.tv_expense_tab)
        tvIncomeTab = view.findViewById(R.id.tv_income_tab)
        tvPrimaryCategory = view.findViewById(R.id.tv_primary_category)
        tvAllCategory = view.findViewById(R.id.tv_all_category)
        
        // 设置点击事件
        setupClickListeners()
        
        // 更新月份文本
        updateYearMonthText()
    }
    
    private fun setupClickListeners() {
        // 月份选择
        tvCurrentMonth.setOnClickListener {
            updateMonthSelection(0)
            viewModel.selectCurrentMonth()
        }
        
        tvLastMonth.setOnClickListener {
            updateMonthSelection(1)
            viewModel.selectLastMonth()
        }
        
        // 收支切换
        tvExpenseTab.setOnClickListener {
            if (!showExpense) {
                showExpense = true
                updateExpenseIncomeSelection()
                viewModel.setShowExpense(true)
            }
        }
        
        tvIncomeTab.setOnClickListener {
            if (showExpense) {
                showExpense = false
                updateExpenseIncomeSelection()
                viewModel.setShowExpense(false)
            }
        }
        
        // 分类层级切换
        tvPrimaryCategory.setOnClickListener {
            if (!showPrimaryCategory) {
                showPrimaryCategory = true
                updateCategoryLevelSelection()
                viewModel.setShowPrimaryCategory(true)
            }
        }
        
        tvAllCategory.setOnClickListener {
            if (showPrimaryCategory) {
                showPrimaryCategory = false
                updateCategoryLevelSelection()
                viewModel.setShowPrimaryCategory(false)
            }
        }
    }
    
    private fun updateYearMonthText() {
        val calendar = Calendar.getInstance()
        tvYear.text = calendar.get(Calendar.YEAR).toString()
    }
    
    private fun updateMonthSelection(selectedIndex: Int) {
        // 重置所有月份样式
        tvCurrentMonth.setBackgroundResource(0)
        tvCurrentMonth.setTextColor(resources.getColor(R.color.text_secondary, null))
        
        tvLastMonth.setBackgroundResource(0)
        tvLastMonth.setTextColor(resources.getColor(R.color.text_secondary, null))
        
        // 设置选中月份样式
        when (selectedIndex) {
            0 -> {
                tvCurrentMonth.setBackgroundResource(R.drawable.bg_month_selected)
                tvCurrentMonth.setTextColor(resources.getColor(R.color.white, null))
            }
            1 -> {
                tvLastMonth.setBackgroundResource(R.drawable.bg_month_selected)
                tvLastMonth.setTextColor(resources.getColor(R.color.white, null))
            }
        }
    }
    
    private fun updateExpenseIncomeSelection() {
        if (showExpense) {
            tvExpenseTab.setBackgroundResource(R.drawable.bg_pill)
            tvExpenseTab.setTextColor(resources.getColor(R.color.white, null))
            tvIncomeTab.setBackgroundResource(0)
            tvIncomeTab.setTextColor(resources.getColor(R.color.text_secondary, null))
        } else {
            tvIncomeTab.setBackgroundResource(R.drawable.bg_pill)
            tvIncomeTab.setTextColor(resources.getColor(R.color.white, null))
            tvExpenseTab.setBackgroundResource(0)
            tvExpenseTab.setTextColor(resources.getColor(R.color.text_secondary, null))
        }
    }
    
    private fun updateCategoryLevelSelection() {
        if (showPrimaryCategory) {
            tvPrimaryCategory.setBackgroundResource(R.drawable.bg_pill)
            tvPrimaryCategory.setTextColor(resources.getColor(R.color.white, null))
            tvAllCategory.setBackgroundResource(0)
            tvAllCategory.setTextColor(resources.getColor(R.color.text_secondary, null))
        } else {
            tvAllCategory.setBackgroundResource(R.drawable.bg_pill)
            tvAllCategory.setTextColor(resources.getColor(R.color.white, null))
            tvPrimaryCategory.setBackgroundResource(0)
            tvPrimaryCategory.setTextColor(resources.getColor(R.color.text_secondary, null))
        }
    }
    
    private fun setupCharts() {
        setupBarChart()
        setupLineChart()
        setupPieChart()
    }
    
    private fun setupBarChart() {
        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)
            
            val xAxis = xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            xAxis.textColor = resources.getColor(R.color.text_secondary, null)
            
            axisLeft.apply {
                textColor = resources.getColor(R.color.text_secondary, null)
                granularity = 10f
                setDrawGridLines(true)
                setDrawAxisLine(false)
            }
            
            axisRight.apply {
                isEnabled = false
            }
            
            legend.apply {
                form = Legend.LegendForm.CIRCLE
                textColor = resources.getColor(R.color.text_primary, null)
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }
        }
    }
    
    private fun setupLineChart() {
        lineChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setPinchZoom(false)
            setScaleEnabled(false)
            
            val xAxis = xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            xAxis.textColor = resources.getColor(R.color.text_secondary, null)
            
            axisLeft.apply {
                textColor = resources.getColor(R.color.text_secondary, null)
                granularity = 100f
                setDrawGridLines(true)
                setDrawAxisLine(false)
            }
            
            axisRight.apply {
                isEnabled = false
            }
            
            legend.apply {
                form = Legend.LegendForm.LINE
                textColor = resources.getColor(R.color.text_primary, null)
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }
        }
    }
    
    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(resources.getColor(R.color.card_background, null))
            setTransparentCircleColor(resources.getColor(R.color.card_background, null))
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            centerText = "支出分类"
            setCenterTextSize(16f)
            setCenterTextColor(resources.getColor(R.color.text_primary, null))
            
            // 设置图例
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                textColor = resources.getColor(R.color.text_secondary, null)
                setDrawInside(false)
                xEntrySpace = 7f
                yEntrySpace = 0f
                yOffset = 0f
            }
        }
    }
    
    private fun observeViewModelData() {
        // 观察月度统计数据
        viewModel.monthSummaryData.observe(viewLifecycleOwner) { summary ->
            if (summary != null) {
                tvMonthlyExpense.text = String.format("%.2f", summary.expense)
                tvMonthlyIncome.text = String.format("%.2f", summary.income)
                tvMonthlyBalance.text = String.format("%.2f", summary.balance)
            }
        }
        
        // 观察日收支数据，用于柱状图
        viewModel.dailyData.observe(viewLifecycleOwner) { dailyData ->
            if (dailyData != null) {
                updateBarChart(dailyData)
            }
        }
        
        // 观察资产趋势数据，用于折线图
        viewModel.assetTrendData.observe(viewLifecycleOwner) { trendData ->
            if (trendData != null) {
                updateLineChart(trendData)
            }
        }
        
        // 观察类别占比数据，用于饼图
        viewModel.categoryData.observe(viewLifecycleOwner) { categoryData ->
            if (categoryData != null) {
                updatePieChart(categoryData)
            }
        }
    }
    
    private fun updateBarChart(dataList: List<BarChartData>) {
        if (dataList.isEmpty()) {
            barChart.setNoDataText("没有记录")
            barChart.invalidate()
            return
        }
        
        val entries = dataList.mapIndexed { index, data ->
            if (showExpense) {
                BarEntry(index.toFloat(), abs(data.expense).toFloat())
            } else {
                BarEntry(index.toFloat(), data.income.toFloat())
            }
        }
        
        val label = if (showExpense) "日支出" else "日收入"
        val color = if (showExpense) 
            resources.getColor(R.color.expense, null) 
        else 
            resources.getColor(R.color.income, null)
        
        val dataSet = BarDataSet(entries, label)
        dataSet.apply {
            setColor(color)
            valueTextColor = resources.getColor(R.color.text_primary, null)
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value < 1) "" else String.format("%.0f", value)
                }
            }
        }
        
        val xAxisLabels = dataList.map { data -> data.day }
        
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
        
        val barData = BarData(dataSet)
        barData.barWidth = 0.7f
        
        barChart.data = barData
        barChart.animateY(1000)
        barChart.invalidate()
    }
    
    private fun updateLineChart(dataList: List<LineChartData>) {
        if (dataList.isEmpty()) {
            lineChart.setNoDataText("没有记录")
            lineChart.invalidate()
            return
        }
        
        val entries = dataList.mapIndexed { index, data ->
            Entry(index.toFloat(), data.balance.toFloat())
        }
        
        val dataSet = LineDataSet(entries, "资产走势")
        dataSet.apply {
            color = resources.getColor(R.color.primary, null)
            lineWidth = 2f
            circleRadius = 3f
            setCircleColor(resources.getColor(R.color.primary, null))
            circleHoleColor = resources.getColor(R.color.white, null)
            valueTextColor = resources.getColor(R.color.text_primary, null)
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = resources.getColor(R.color.primary_light, null)
            fillAlpha = 50
            mode = LineDataSet.Mode.CUBIC_BEZIER
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    // 只在部分点显示数值，避免拥挤
                    return String.format("%.0f", value)
                }
            }
        }
        
        val xAxisLabels = dataList.map { data -> data.date }
        
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
        lineChart.xAxis.labelCount = minOf(5, xAxisLabels.size)
        
        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.animateX(1000)
        lineChart.invalidate()
    }
    
    private fun updatePieChart(categories: Map<String, Double>) {
        if (categories.isEmpty()) {
            pieChart.setNoDataText("没有记录")
            pieChart.invalidate()
            return
        }
        
        // 转换为饼图数据
        val entries = categories.map { (category, amount) ->
            PieEntry(abs(amount.toFloat()), category)
        }
        
        // 设置数据集
        val dataSet = PieDataSet(entries, "支出类别")
        dataSet.apply {
            sliceSpace = 3f
            selectionShift = 5f
            valueTextColor = resources.getColor(R.color.white, null)
            valueTextSize = 12f
            
            // 设置自定义颜色
            val colorsArray = intArrayOf(
                resources.getColor(R.color.primary, null),
                resources.getColor(R.color.expense, null),
                resources.getColor(R.color.income, null),
                resources.getColor(R.color.highlight, null),
                Color.rgb(255, 183, 77),
                Color.rgb(77, 182, 172),
                Color.rgb(129, 199, 132),
                Color.rgb(229, 115, 115)
            )
            colors = colorsArray.toList()
        }
        
        // 设置饼图数据
        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(pieChart))
            setValueTextSize(11f)
            setValueTextColor(Color.WHITE)
        }
        
        pieChart.data = pieData
        pieChart.highlightValues(null)
        pieChart.animateY(1000)
        pieChart.invalidate()
    }
} 