package com.example.accounting.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 资产账户实体类
 */
@Entity(tableName = "asset_accounts")
data class AssetAccount(
    val name: String,         // 账户名称
    var balance: Double,      // 账户余额
    val type: String,         // 账户类型: cash, bank, alipay, wechat, credit等
    val currency: String,     // 货币类型: CNY, USD等
    val icon: String = type,  // 账户图标，默认使用类型名称
    val isDefault: Boolean = false, // 是否为默认账户
    val order: Int = 0,       // 排序顺序
    @PrimaryKey
    val id: String = UUID.randomUUID().toString() // 唯一标识
) 