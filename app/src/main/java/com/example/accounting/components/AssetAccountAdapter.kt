package com.example.accounting.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.accounting.R
import com.example.accounting.models.AssetAccount

/**
 * 资产账户适配器
 */
class AssetAccountAdapter(private var accounts: List<AssetAccount>) : 
    RecyclerView.Adapter<AssetAccountAdapter.AccountViewHolder>() {
    
    // 账户类型图标映射
    private val accountTypeIcons = mapOf(
        "cash" to R.drawable.ic_account_cash,
        "bank" to R.drawable.ic_account_bank,
        "alipay" to R.drawable.ic_account_alipay,
        "wechat" to R.drawable.ic_account_wechat,
        "credit" to R.drawable.ic_account_credit,
        "default" to R.drawable.ic_account_default
    )
    
    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageAccountType: ImageView = itemView.findViewById(R.id.image_account_type)
        val textAccountName: TextView = itemView.findViewById(R.id.text_account_name)
        val textAccountBalance: TextView = itemView.findViewById(R.id.text_account_balance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accounts[position]
        
        // 设置账户类型图标
        val iconResId = accountTypeIcons[account.type] ?: R.drawable.ic_account_default
        holder.imageAccountType.setImageResource(iconResId)
        
        // 设置账户名称
        holder.textAccountName.text = account.name
        
        // 设置账户余额
        val balancePrefix = if (account.balance >= 0) "¥" else "-¥"
        val balanceText = String.format("%s%.2f", balancePrefix, Math.abs(account.balance))
        holder.textAccountBalance.text = balanceText
        
        // 设置账户余额颜色
        val textColor = if (account.balance >= 0) 
            holder.itemView.context.getColor(R.color.positive_amount) 
        else 
            holder.itemView.context.getColor(R.color.negative_amount)
        holder.textAccountBalance.setTextColor(textColor)
    }

    override fun getItemCount(): Int = accounts.size
    
    /**
     * 添加新账户
     */
    fun addAccount(account: AssetAccount) {
        val newAccounts = accounts.toMutableList()
        newAccounts.add(account)
        accounts = newAccounts
        notifyItemInserted(accounts.size - 1)
    }
    
    /**
     * 更新账户列表
     */
    fun updateAccounts(newAccounts: List<AssetAccount>) {
        accounts = newAccounts
        notifyDataSetChanged()
    }
    
    /**
     * 更新单个账户
     */
    fun updateAccount(position: Int, account: AssetAccount) {
        if (position >= 0 && position < accounts.size) {
            val newAccounts = accounts.toMutableList()
            newAccounts[position] = account
            accounts = newAccounts
            notifyItemChanged(position)
        }
    }
    
    /**
     * 删除账户
     */
    fun removeAccount(position: Int) {
        if (position >= 0 && position < accounts.size) {
            val newAccounts = accounts.toMutableList()
            newAccounts.removeAt(position)
            accounts = newAccounts
            notifyItemRemoved(position)
        }
    }
} 