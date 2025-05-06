package com.example.accounting.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.accounting.R
import com.example.accounting.components.AssetAccountAdapter
import com.example.accounting.models.AssetAccount
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * 资产Fragment
 * 显示资产账户和资金流动情况
 */
class AssetsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var assetAdapter: AssetAccountAdapter
    private lateinit var fabAddAccount: FloatingActionButton
    private lateinit var textTotalAssets: TextView
    
    // 示例账户数据
    private val demoAccounts = listOf(
        AssetAccount("现金账户", 1000.0, "cash", "CNY"),
        AssetAccount("微信钱包", 2500.0, "wechat", "CNY"),
        AssetAccount("支付宝", 1800.0, "alipay", "CNY"),
        AssetAccount("工商银行", 5000.0, "bank", "CNY"),
        AssetAccount("信用卡", -3000.0, "credit", "CNY")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assets, container, false)
        
        // 初始化UI组件
        recyclerView = view.findViewById(R.id.recycler_accounts)
        fabAddAccount = view.findViewById(R.id.fab_add_account)
        textTotalAssets = view.findViewById(R.id.text_total_assets)
        
        // 设置RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        assetAdapter = AssetAccountAdapter(demoAccounts)
        recyclerView.adapter = assetAdapter
        
        // 设置添加账户按钮点击事件
        fabAddAccount.setOnClickListener {
            showAddAccountDialog()
        }
        
        // 计算并显示总资产
        updateTotalAssets()
        
        return view
    }
    
    private fun updateTotalAssets() {
        val totalAssets = demoAccounts.sumByDouble { it.balance }
        textTotalAssets.text = String.format("总资产: ¥%.2f", totalAssets)
    }
    
    private fun showAddAccountDialog() {
        // 创建添加账户的对话框
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_account, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("添加账户")
            .setView(dialogView)
            .create()
        
        // 设置确定按钮点击事件
        dialogView.findViewById<Button>(R.id.button_confirm).setOnClickListener {
            // 获取输入的账户信息
            val accountName = dialogView.findViewById<android.widget.EditText>(R.id.edit_account_name).text.toString()
            val accountBalance = dialogView.findViewById<android.widget.EditText>(R.id.edit_account_balance).text.toString().toDoubleOrNull() ?: 0.0
            
            if (accountName.isNotEmpty()) {
                // 创建新账户并添加到适配器
                val newAccount = AssetAccount(accountName, accountBalance, "default", "CNY")
                assetAdapter.addAccount(newAccount)
                updateTotalAssets()
                dialog.dismiss()
            }
        }
        
        // 设置取消按钮点击事件
        dialogView.findViewById<Button>(R.id.button_cancel).setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
} 