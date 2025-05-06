package com.example.accounting.services.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.accounting.utils.LogUtils
import java.util.regex.Pattern

/**
 * 支付监测无障碍服务
 * 用于监测微信和支付宝的支付行为，检测到支付成功后弹出记账窗口
 */
class PaymentDetectionService : AccessibilityService() {

    companion object {
        private const val TAG = "PaymentDetectionService"
        
        // 微信支付成功页面特征
        private const val WECHAT_PACKAGE = "com.tencent.mm"
        private const val WECHAT_PAY_SUCCESS_TEXT = "支付成功"
        private const val WECHAT_PAY_AMOUNT_REGEX = "¥([0-9]+\\.?[0-9]*)"
        
        // 支付宝支付成功页面特征
        private const val ALIPAY_PACKAGE = "com.eg.android.AlipayGphone"
        private const val ALIPAY_PAY_SUCCESS_TEXT = "支付成功"
        private const val ALIPAY_PAY_AMOUNT_REGEX = "([0-9]+\\.?[0-9]*)元"
    }
    
    private var lastDetectedTime = 0L
    private val coolDownTime = 3000L // 防止重复触发的冷却时间
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        LogUtils.d(TAG, "无障碍服务已连接")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // 只处理窗口状态变化和内容变化事件
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && 
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return
        }
        
        // 获取当前包名
        val packageName = event.packageName?.toString() ?: return
        
        // 根据包名分发处理逻辑
        when (packageName) {
            WECHAT_PACKAGE -> handleWechatPayment(event)
            ALIPAY_PACKAGE -> handleAlipayPayment(event)
        }
    }
    
    override fun onInterrupt() {
        LogUtils.d(TAG, "无障碍服务已中断")
    }
    
    /**
     * 处理微信支付
     */
    private fun handleWechatPayment(event: AccessibilityEvent) {
        // 防止短时间内重复触发
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDetectedTime < coolDownTime) {
            return
        }
        
        // 获取根节点
        val rootNode = rootInActiveWindow ?: return
        
        try {
            // 检查是否包含支付成功文本
            val successNodes = rootNode.findAccessibilityNodeInfosByText(WECHAT_PAY_SUCCESS_TEXT)
            if (successNodes.isEmpty()) {
                return
            }
            
            // 提取支付金额
            val allText = getAllText(rootNode)
            val amountMatcher = Pattern.compile(WECHAT_PAY_AMOUNT_REGEX).matcher(allText)
            if (amountMatcher.find()) {
                val amount = amountMatcher.group(1)
                
                // 提取商户信息，微信支付页面通常会显示商户名称
                val merchantName = extractMerchantName(rootNode)
                
                // 触发记账弹窗
                showAccountingWindow(amount, merchantName, "微信支付")
                lastDetectedTime = currentTime
            }
        } finally {
            rootNode.recycle()
        }
    }
    
    /**
     * 处理支付宝支付
     */
    private fun handleAlipayPayment(event: AccessibilityEvent) {
        // 防止短时间内重复触发
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDetectedTime < coolDownTime) {
            return
        }
        
        // 获取根节点
        val rootNode = rootInActiveWindow ?: return
        
        try {
            // 检查是否包含支付成功文本
            val successNodes = rootNode.findAccessibilityNodeInfosByText(ALIPAY_PAY_SUCCESS_TEXT)
            if (successNodes.isEmpty()) {
                return
            }
            
            // 提取支付金额
            val allText = getAllText(rootNode)
            val amountMatcher = Pattern.compile(ALIPAY_PAY_AMOUNT_REGEX).matcher(allText)
            if (amountMatcher.find()) {
                val amount = amountMatcher.group(1)
                
                // 提取商户信息，支付宝支付页面通常会显示商户名称
                val merchantName = extractMerchantName(rootNode)
                
                // 触发记账弹窗
                showAccountingWindow(amount, merchantName, "支付宝")
                lastDetectedTime = currentTime
            }
        } finally {
            rootNode.recycle()
        }
    }
    
    /**
     * 递归获取节点及其子节点的所有文本
     */
    private fun getAllText(node: AccessibilityNodeInfo): String {
        val stringBuilder = StringBuilder()
        
        if (node.text != null) {
            stringBuilder.append(node.text)
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            stringBuilder.append(getAllText(child))
            child.recycle()
        }
        
        return stringBuilder.toString()
    }
    
    /**
     * 提取商户名称
     * 注：这是一个简化的实现，实际场景中需要更精确的提取逻辑
     */
    private fun extractMerchantName(rootNode: AccessibilityNodeInfo): String {
        // 微信和支付宝的支付页面上通常会有商户名称
        // 这里只是一个示例实现，实际应用中需要根据页面结构进行更精确的提取
        
        // 尝试常见的商户名称文本模式
        val merchantTextPatterns = listOf("商户:", "收款方:", "商家:", "店铺:")
        
        for (pattern in merchantTextPatterns) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(pattern)
            for (node in nodes) {
                // 商户名称通常在标识文本的旁边或下一个节点
                val parent = node.parent ?: continue
                for (i in 0 until parent.childCount) {
                    val child = parent.getChild(i) ?: continue
                    if (child != node && child.text != null) {
                        return child.text.toString()
                    }
                }
            }
        }
        
        // 如果没有找到明确的商户名称，尝试使用一些启发式规则
        // 例如，检查支付页面中可能包含店铺名称的文本
        
        return "未知商户" // 如果无法确定，返回默认值
    }
    
    /**
     * 显示记账悬浮窗
     */
    private fun showAccountingWindow(amount: String?, merchantName: String, payMethod: String) {
        LogUtils.d(TAG, "检测到支付: 金额=$amount, 商户=$merchantName, 方式=$payMethod")
        
        // 启动悬浮窗服务
        val intent = Intent(this, FloatingRecordService::class.java).apply {
            putExtra("amount", amount)
            putExtra("merchant", merchantName)
            putExtra("payMethod", payMethod)
            putExtra("time", System.currentTimeMillis())
        }
        startService(intent)
    }
} 