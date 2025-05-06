package com.example.accounting.services.accessibility

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.example.accounting.R
import com.example.accounting.models.ExpenseRecord
import com.example.accounting.repositories.RecordRepository
import com.example.accounting.services.nlp.CategoryClassifier
import com.example.accounting.utils.DateUtils
import com.example.accounting.utils.LogUtils
import java.util.*

/**
 * 悬浮窗记账服务
 * 在检测到支付后弹出记账窗口
 */
class FloatingRecordService : Service() {

    companion object {
        private const val TAG = "FloatingRecordService"
    }

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private var amount: String? = null
    private var merchant: String? = null
    private var payMethod: String? = null
    private var time: Long = 0

    override fun onCreate() {
        super.onCreate()
        LogUtils.d(TAG, "FloatingRecordService onCreate")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            amount = it.getStringExtra("amount")
            merchant = it.getStringExtra("merchant")
            payMethod = it.getStringExtra("payMethod")
            time = it.getLongExtra("time", System.currentTimeMillis())
            
            showFloatingWindow()
        }
        
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized && floatingView.isAttachedToWindow) {
            windowManager.removeView(floatingView)
        }
    }

    /**
     * 显示悬浮窗
     */
    private fun showFloatingWindow() {
        // 创建悬浮窗布局
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_record_window, null)
        
        // 设置窗口参数
        val params = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP
            format = PixelFormat.TRANSLUCENT
            
            // 根据Android版本设置不同的窗口类型
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        }
        
        // 初始化视图内容
        initializeViewContent()
        
        // 添加到窗口管理器
        windowManager.addView(floatingView, params)
    }

    /**
     * 初始化视图内容
     */
    private fun initializeViewContent() {
        // 获取视图控件
        val tvAmount = floatingView.findViewById<TextView>(R.id.tv_amount)
        val tvMerchant = floatingView.findViewById<TextView>(R.id.tv_merchant)
        val tvCategory = floatingView.findViewById<TextView>(R.id.tv_category)
        val tvDate = floatingView.findViewById<TextView>(R.id.tv_date)
        val btnConfirm = floatingView.findViewById<Button>(R.id.btn_confirm)
        val btnCancel = floatingView.findViewById<ImageButton>(R.id.btn_cancel)
        
        // 设置金额
        tvAmount.text = amount
        
        // 设置商户名称
        tvMerchant.text = merchant
        
        // 根据商户名称预测消费类别
        val category = CategoryClassifier.predictCategory(merchant ?: "")
        tvCategory.text = category
        
        // 设置日期时间
        tvDate.text = DateUtils.formatDateTime(time)
        
        // 确认按钮
        btnConfirm.setOnClickListener {
            saveRecord()
            stopSelf()
        }
        
        // 取消按钮
        btnCancel.setOnClickListener {
            stopSelf()
        }
        
        // 设置自动关闭计时器（10秒后自动关闭）
        floatingView.postDelayed({
            if (::floatingView.isInitialized && floatingView.isAttachedToWindow) {
                stopSelf()
            }
        }, 10000)
    }

    /**
     * 保存记账记录
     */
    private fun saveRecord() {
        try {
            val record = ExpenseRecord(
                id = UUID.randomUUID().toString(),
                amount = amount?.toDoubleOrNull() ?: 0.0,
                category = CategoryClassifier.predictCategory(merchant ?: ""),
                merchant = merchant ?: "",
                payMethod = payMethod ?: "",
                date = Date(time),
                remark = ""
            )
            
            // 保存到数据库
            RecordRepository.getInstance(this).addRecord(record)
            
            LogUtils.d(TAG, "记账记录已保存: $record")
        } catch (e: Exception) {
            LogUtils.e(TAG, "保存记账记录失败", e)
        }
    }
} 