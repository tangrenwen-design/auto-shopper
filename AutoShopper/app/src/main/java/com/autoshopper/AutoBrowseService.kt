package com.autoshopper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.*

class AutoBrowseService : AccessibilityService() {

    companion object {
        const val ACTION_START_AUTO_BROWSE = "com.autoshopper.START_BROWSE"
        const val ACTION_STOP_AUTO_BROWSE = "com.autoshopper.STOP_BROWSE"
        const val ACTION_SET_CONFIG = "com.autoshopper.SET_CONFIG"

        const val EXTRA_APP_PACKAGE = "app_package"
        const val EXTRA_BROWSE_DURATION = "duration"
        const val EXTRA_SCROLL_COUNT = "scroll_count"
        const val EXTRA_RANDOM_DELAY = "random_delay"

        private var instance: AutoBrowseService? = null

        fun isRunning(): Boolean = instance != null

        fun setBrowseParams(packageName: String, duration: Int, scrollCount: Int, randomDelay: Boolean) {
            instance?.apply {
                this.targetPackage = packageName
                this.totalDuration = duration.toLong()
                this.maxScrollCount = scrollCount
                this.useRandomDelay = randomDelay
            }
        }

        fun startBrowse() {
            instance?.startAutoBrowse()
        }

        fun stopBrowse() {
            instance?.stopAutoBrowse()
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val random = Random()

    private var isBrowsing = false
    private var targetPackage = ""
    private var totalDuration = 300000L // 默认5分钟
    private var maxScrollCount = 50
    private var useRandomDelay = true
    private var currentScrollCount = 0
    private var startTime = 0L

    private var scrollRunnable: Runnable? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d("AutoShopper", "无障碍服务已连接")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Log.d("AutoShopper", "当前窗口: ${event.packageName}")
        }
    }

    override fun onInterrupt() {
        stopAutoBrowse()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        stopAutoBrowse()
    }

    private fun startAutoBrowse() {
        if (isBrowsing) return

        isBrowsing = true
        currentScrollCount = 0
        startTime = System.currentTimeMillis()

        Log.d("AutoShopper", "开始自动浏览: $targetPackage")

        // 打开目标APP
        launchApp(targetPackage)

        // 开始滚动循环
        scheduleNextScroll()
    }

    private fun stopAutoBrowse() {
        isBrowsing = false
        scrollRunnable?.let { handler.removeCallbacks(it) }
        Log.d("AutoShopper", "停止自动浏览")
    }

    private fun launchApp(packageName: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(it)
                Log.d("AutoShopper", "启动应用: $packageName")
            }
        } catch (e: Exception) {
            Log.e("AutoShopper", "启动应用失败: ${e.message}")
        }
    }

    private fun scheduleNextScroll() {
        if (!isBrowsing) return

        // 检查是否达到时长限制
        val elapsed = System.currentTimeMillis() - startTime
        if (elapsed >= totalDuration) {
            Log.d("AutoShopper", "达到浏览时长限制")
            stopAutoBrowse()
            return
        }

        // 检查是否达到滚动次数限制
        if (currentScrollCount >= maxScrollCount) {
            Log.d("AutoShopper", "达到滚动次数限制")
            stopAutoBrowse()
            return
        }

        // 随机延迟 (2-8秒)
        val delay = if (useRandomDelay) {
            random.nextInt(6000) + 2000L
        } else {
            3000L
        }

        scrollRunnable = Runnable {
            performScroll()
            currentScrollCount++
            Log.d("AutoShopper", "滚动进度: $currentScrollCount/$maxScrollCount")
            scheduleNextScroll()
        }
        handler.postDelayed(scrollRunnable!!, delay)
    }

    private fun performScroll() {
        val rootNode = rootInActiveWindow ?: return

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // 创建手势路径 - 从底部向上滑动
        val path = Path()
        val startX = screenWidth / 2f
        val startY = screenHeight * 0.8f
        val endX = screenWidth / 2f
        val endY = screenHeight * 0.2f

        path.moveTo(startX, startY)
        path.lineTo(endX, endY)

        // 随机滚动距离
        val scrollDuration = random.nextInt(300) + 300L

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, scrollDuration))

        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun findClickableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        node ?: return null

        if (node.isClickable) {
            return node
        }

        for (i in 0 until node.childCount) {
            val result = findClickableNode(node.getChild(i))
            if (result != null) return result
        }

        return null
    }

    private fun clickNode(node: AccessibilityNodeInfo) {
        val path = Path()
        val rect = Rect()
        node.getBoundsInScreen(rect)

        val centerX = rect.exactCenterX()
        val centerY = rect.exactCenterY()

        path.moveTo(centerX, centerY)

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 100))

        dispatchGesture(gestureBuilder.build(), null, null)
    }
}
