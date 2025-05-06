package com.example.accounting

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.accounting.models.ExpenseRecord
import com.example.accounting.repositories.RecordRepository
import com.example.accounting.screens.*
import com.example.accounting.services.accessibility.PaymentDetectionService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabAddRecord: FloatingActionButton
    private lateinit var recordRepository: RecordRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化数据仓库
        recordRepository = RecordRepository.getInstance(applicationContext)

        // 初始化UI组件
        bottomNav = findViewById(R.id.bottom_navigation)
        fabAddRecord = findViewById(R.id.fab_add_record)

        // 设置底部导航栏监听
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_calendar -> loadFragment(CalendarFragment())
                R.id.nav_stats -> loadFragment(StatsFragment())
                R.id.nav_assets -> loadFragment(AssetsFragment())
                R.id.nav_settings -> loadFragment(SettingsFragment())
                else -> false
            }
        }

        // 设置浮动按钮点击事件
        fabAddRecord.setOnClickListener {
            showRecordDialog()
        }

        // 默认加载首页
        loadFragment(HomeFragment())

        // 检查无障碍服务是否开启
        if (!isAccessibilityServiceEnabled()) {
            showAccessibilityServiceDialog()
        }
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }

    private fun showRecordDialog() {
        // 显示记账方式选择对话框
        val options = arrayOf(
            getString(R.string.voice_record),
            getString(R.string.text_record),
            getString(R.string.image_record)
        )

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_record))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startVoiceRecord()
                    1 -> startTextRecord()
                    2 -> startImageRecord()
                }
            }
            .show()
    }

    private fun startVoiceRecord() {
        // TODO: 实现语音记账功能
        Toast.makeText(this, getString(R.string.feature_coming_soon), Toast.LENGTH_SHORT).show()
        // 暂时通过手动输入实现记账
        startTextRecord()
    }

    private fun startTextRecord() {
        // 创建一个简单的记账输入对话框
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_record, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.et_amount)
        val etCategory = dialogView.findViewById<EditText>(R.id.et_category)
        val etRemark = dialogView.findViewById<EditText>(R.id.et_remark)
        val chipGroup = dialogView.findViewById<ChipGroup>(R.id.chip_group_categories)

        // 设置类别选择芯片的点击监听
        setupCategoryChips(chipGroup, etCategory)

        // 创建并显示对话框
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_record))
            .setView(dialogView)
            .setPositiveButton(getString(android.R.string.ok), null) // 我们会在下面重新设置该按钮
            .setNegativeButton(getString(android.R.string.cancel), null)
            .create()

        dialog.show()

        // 重新设置"确认"按钮，避免自动关闭
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            try {
                val amountStr = etAmount.text.toString()
                if (amountStr.isEmpty()) {
                    Toast.makeText(this, getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val amount = amountStr.toDouble()
                val category = etCategory.text.toString()
                val remark = etRemark.text.toString()

                if (amount <= 0) {
                    Toast.makeText(this, getString(R.string.amount_must_greater_than_zero), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (category.isEmpty()) {
                    Toast.makeText(this, "请选择或输入分类", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // 创建记账记录
                val record = ExpenseRecord(
                    id = UUID.randomUUID().toString(), // 生成唯一ID
                    amount = -amount, // 默认为支出，使用负值
                    category = category,
                    merchant = "", // 空商户名称
                    remark = remark,
                    date = Date(),
                    payMethod = getString(R.string.pay_method_cash) // 默认为现金
                )

                // 保存记录
                recordRepository.addRecord(record)
                Toast.makeText(this, getString(R.string.record_added), Toast.LENGTH_SHORT).show()

                // 关闭对话框
                dialog.dismiss()

                // 刷新当前页面
                refreshCurrentFragment()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCategoryChips(chipGroup: ChipGroup, etCategory: EditText) {
        // 设置各个类别标签的点击事件
        val chipIds = listOf(
            R.id.chip_food, 
            R.id.chip_transport, 
            R.id.chip_shopping, 
            R.id.chip_other
        )
        
        for (chipId in chipIds) {
            val chip = chipGroup.findViewById<Chip>(chipId)
            chip.setOnClickListener {
                etCategory.setText(chip.text)
            }
        }
    }

    private fun startImageRecord() {
        // TODO: 实现截图记账功能
        Toast.makeText(this, getString(R.string.feature_coming_soon), Toast.LENGTH_SHORT).show()
        // 暂时通过手动输入实现记账
        startTextRecord()
    }
    
    /**
     * 刷新当前Fragment
     */
    private fun refreshCurrentFragment() {
        // 获取当前显示的Fragment
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        
        // 根据当前Fragment类型创建新实例并加载
        when (currentFragment) {
            is HomeFragment -> loadFragment(HomeFragment())
            is CalendarFragment -> loadFragment(CalendarFragment())
            is StatsFragment -> loadFragment(StatsFragment())
            is AssetsFragment -> loadFragment(AssetsFragment())
            is SettingsFragment -> loadFragment(SettingsFragment())
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityEnabled = try {
            Settings.Secure.getInt(
                contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            0
        }

        if (accessibilityEnabled == 1) {
            val serviceString = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return serviceString?.contains("${packageName}/${PaymentDetectionService::class.java.name}") == true
        }
        return false
    }

    private fun showAccessibilityServiceDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.accessibility_guide_title))
            .setMessage(getString(R.string.accessibility_guide_content))
            .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton(getString(android.R.string.cancel), null)
            .show()
    }
} 