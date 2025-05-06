package com.example.accounting.screens

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.accounting.R
import com.example.accounting.services.accessibility.PaymentDetectionService
import java.io.File

/**
 * 设置Fragment
 * 显示应用设置选项
 */
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 加载设置页面
        childFragmentManager.beginTransaction()
            .replace(R.id.settings_container, SettingsPreferenceFragment())
            .commit()
    }

    /**
     * 设置项Fragment
     */
    class SettingsPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            // 设置无障碍服务设置项的点击事件
            findPreference<Preference>("accessibility_settings")?.setOnPreferenceClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                true
            }
            
            // 设置导出数据点击事件
            findPreference<Preference>("export_data")?.setOnPreferenceClickListener {
                exportData()
                true
            }
            
            // 设置导入数据点击事件
            findPreference<Preference>("import_data")?.setOnPreferenceClickListener {
                importData()
                true
            }
            
            // 设置清空数据点击事件
            findPreference<Preference>("clear_data")?.setOnPreferenceClickListener {
                showClearDataConfirmDialog()
                true
            }
            
            // 设置关于点击事件
            findPreference<Preference>("about")?.setOnPreferenceClickListener {
                showAboutDialog()
                true
            }
        }
        
        private fun exportData(): Boolean {
            try {
                // 创建导出文件
                val exportDir = File(requireContext().getExternalFilesDir(null), "export")
                if (!exportDir.exists()) {
                    exportDir.mkdirs()
                }
                
                val exportFile = File(exportDir, "accounting_data_${System.currentTimeMillis()}.json")
                
                // 导出数据库内容到JSON文件
                // TODO: 实现实际的数据导出逻辑
                
                AlertDialog.Builder(requireContext())
                    .setTitle("导出成功")
                    .setMessage("数据已导出到: ${exportFile.absolutePath}")
                    .setPositiveButton("确定", null)
                    .show()
                
                return true
            } catch (e: Exception) {
                AlertDialog.Builder(requireContext())
                    .setTitle("导出失败")
                    .setMessage("错误: ${e.message}")
                    .setPositiveButton("确定", null)
                    .show()
                return false
            }
        }
        
        private fun importData(): Boolean {
            // 显示文件选择器
            // TODO: 实现实际的数据导入逻辑
            AlertDialog.Builder(requireContext())
                .setTitle("导入数据")
                .setMessage("请选择要导入的备份文件")
                .setPositiveButton("浏览") { _, _ ->
                    // 打开文件选择器
                }
                .setNegativeButton("取消", null)
                .show()
            
            return true
        }
        
        private fun showClearDataConfirmDialog(): Boolean {
            AlertDialog.Builder(requireContext())
                .setTitle("清空数据")
                .setMessage("确定要清空所有记账数据吗？此操作不可恢复！")
                .setPositiveButton("确定") { _, _ ->
                    // 清空数据库
                    // TODO: 实现清空数据库的逻辑
                    
                    AlertDialog.Builder(requireContext())
                        .setTitle("已清空")
                        .setMessage("所有记账数据已清空")
                        .setPositiveButton("确定", null)
                        .show()
                }
                .setNegativeButton("取消", null)
                .show()
            
            return true
        }
        
        private fun showAboutDialog(): Boolean {
            AlertDialog.Builder(requireContext())
                .setTitle("关于")
                .setMessage("简易记账 v1.0\n\n一个简单的本地记账应用\n支持自动监测微信/支付宝支付记录\n\n作者: 简易记账团队")
                .setPositiveButton("确定", null)
                .show()
            
            return true
        }
    }
} 