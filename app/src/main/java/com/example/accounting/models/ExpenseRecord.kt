package com.example.accounting.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * 支出记录实体类
 */
@Entity(tableName = "expense_records")
data class ExpenseRecord(
    @PrimaryKey
    val id: String, // UUID
    val amount: Double, // 支出金额
    val category: String, // 消费类别
    val merchant: String, // 商户名称
    val payMethod: String, // 支付方式
    val date: Date, // 消费日期
    val remark: String // 备注
) 