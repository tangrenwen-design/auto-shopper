package com.autoshopper

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerApp: Spinner
    private lateinit var seekBarDuration: SeekBar
    private lateinit var textDuration: TextView
    private lateinit var seekBarScrollCount: SeekBar
    private lateinit var textScrollCount: TextView
    private lateinit var switchRandomDelay: Switch
    private lateinit var buttonStart: Button
    private lateinit var buttonStop: Button
    private lateinit var textStatus: TextView

    private val apps = listOf(
        "淘宝", "京东", "拼多多", "天猫", "苏宁易购",
        "抖音商城", "快手商城", "美团优选"
    )
    private val packages = listOf(
        "com.taobao.taobao",
        "com.jingdong.app.mall",
        "com.xunmeng.pinduoduo",
        "com.tmall.wireless",
        "com.suning.shop",
        "com.ss.android.ugc.aweme",
        "com.smile.gifmaker",
        "com.sankuai.meituan.takeoutnew"
    )

    private var selectedPackage = packages[0]
    private var durationMinutes = 5
    private var scrollCount = 50
    private var useRandomDelay = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
        checkAccessibilityPermission()
    }

    private fun initViews() {
        spinnerApp = findViewById(R.id.spinnerApp)
        seekBarDuration = findViewById(R.id.seekBarDuration)
        textDuration = findViewById(R.id.textDuration)
        seekBarScrollCount = findViewById(R.id.seekBarScrollCount)
        textScrollCount = findViewById(R.id.textScrollCount)
        switchRandomDelay = findViewById(R.id.switchRandomDelay)
        buttonStart = findViewById(R.id.buttonStart)
        buttonStop = findViewById(R.id.buttonStop)
        textStatus = findViewById(R.id.textStatus)

        // 设置APP选择器
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, apps)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerApp.adapter = adapter
        spinnerApp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                selectedPackage = packages[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 设置时长滑块
        seekBarDuration.max = 60
        seekBarDuration.progress = 5
        textDuration.text = "5 分钟"
        seekBarDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                durationMinutes = if (progress == 0) 1 else progress
                textDuration.text = "$durationMinutes 分钟"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 设置滚动次数滑块
        seekBarScrollCount.max = 200
        seekBarScrollCount.progress = 50
        textScrollCount.text = "50 次"
        seekBarScrollCount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                scrollCount = if (progress == 0) 10 else progress
                textScrollCount.text = "$scrollCount 次"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 设置随机延迟开关
        switchRandomDelay.isChecked = true
        useRandomDelay = true
        switchRandomDelay.setOnCheckedChangeListener { _, isChecked ->
            useRandomDelay = isChecked
        }
    }

    private fun setupListeners() {
        buttonStart.setOnClickListener {
            if (isAccessibilityServiceEnabled()) {
                startAutoBrowse()
            } else {
                showAccessibilityDialog()
            }
        }

        buttonStop.setOnClickListener {
            stopAutoBrowse()
        }
    }

    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            textStatus.text = "需要开启无障碍服务"
            textStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains("${packageName}/.AutoBrowseService") == true
    }

    private fun showAccessibilityDialog() {
        AlertDialog.Builder(this)
            .setTitle("开启无障碍服务")
            .setMessage("自动浏览需要无障碍服务权限，请去设置中开启\n\n1. 点击\"去设置\"\n2. 找到\"自动浏览助手\"\n3. 开启服务")
            .setPositiveButton("去设置") { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun startAutoBrowse() {
        // 设置参数
        AutoBrowseService.setBrowseParams(
            selectedPackage,
            durationMinutes * 60,
            scrollCount,
            useRandomDelay
        )

        // 开始浏览
        AutoBrowseService.startBrowse()

        // 更新UI
        textStatus.text = "正在自动浏览 ${apps[packages.indexOf(selectedPackage)]}"
        textStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        buttonStart.isEnabled = false
        buttonStop.isEnabled = true

        Toast.makeText(this, "开始自动浏览", Toast.LENGTH_SHORT).show()
    }

    private fun stopAutoBrowse() {
        AutoBrowseService.stopBrowse()

        textStatus.text = "已停止"
        textStatus.setTextColor(resources.getColor(android.R.color.darker_gray))
        buttonStart.isEnabled = true
        buttonStop.isEnabled = false

        Toast.makeText(this, "已停止自动浏览", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        checkAccessibilityPermission()
    }
}
