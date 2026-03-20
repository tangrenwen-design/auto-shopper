# 自动浏览助手

一个Android应用，可以自动在购物APP上浏览商品，生成浏览数据。

## 功能特性

- 支持8个主流购物平台：淘宝、京东、拼多多、天猫、苏宁易购、抖音商城、快手商城、美团优选
- 自定义浏览时长（1-60分钟）
- 自定义滚动次数（10-200次）
- 随机延迟模拟真实用户行为（2-8秒）
- 基于无障碍服务，无需root权限
- 简洁易用的中文界面

## 项目结构

```
AutoShopper/
├── app/
│   ├── src/main/
│   │   ├── java/com/autoshopper/
│   │   │   ├── MainActivity.kt          # 主界面
│   │   │   └── AutoBrowseService.kt     # 无障碍服务（核心逻辑）
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml    # 界面布局
│   │   │   ├── xml/
│   │   │   │   └── accessibility_service_config.xml
│   │   │   └── values/
│   │   ├── AndroidManifest.xml
│   │   └── build.gradle.kts
│   └── build.gradle.kts
├── settings.gradle.kts
├── build.gradle.kts
└── README.md
```

## 环境要求

- **Android Studio** (最新版本，建议 Koala 或更新)
- **JDK 11 或更高版本**
- **Android SDK** (API Level 26+)

## 安装步骤

### 1. 安装 Android Studio

从官网下载并安装 Android Studio：
https://developer.android.com/studio

### 2. 安装 JDK

Android Studio 安装时会自动包含 JDK，或可单独安装：
https://www.oracle.com/java/technologies/downloads/

### 3. 导入项目

1. 启动 Android Studio
2. 选择 "Open" 或 "打开现有项目"
3. 选择 `AutoShopper` 文件夹
4. 等待 Gradle 同步完成（首次同步可能需要几分钟）

### 4. 连接设备

**方式一：使用真机**
1. 开启手机的开发者选项
2. 开启 USB 调试
3. 用 USB 线连接电脑
4. 手机上确认允许调试

**方式二：使用模拟器**
1. 在 Android Studio 中点击 "Device Manager"
2. 创建新的虚拟设备
3. 选择 Pixel 模拟器，API 34

### 5. 运行应用

1. 点击工具栏的绿色播放按钮 ▶️
2. 或按快捷键 `Shift + F10`
3. 等待 APK 安装到设备

## 使用说明

### 首次使用

1. 打开"自动浏览助手"应用
2. 系统会提示开启无障碍服务
3. 点击"去设置"
4. 在设置中找到"自动浏览助手"
5. 开启服务开关

### 日常使用

1. 选择要浏览的APP（如淘宝）
2. 设置浏览时长（如 5 分钟）
3. 设置滚动次数（如 50 次）
4. 开启随机延迟（推荐）
5. 点击"开始自动浏览"

### 停止浏览

- 点击应用内的"停止浏览"按钮
- 或直接关闭应用

## 构建发布版 APK

1. 在 Android Studio 中选择 `Build > Generate Signed Bundle / APK`
2. 选择 "APK"
3. 创建或选择密钥库
4. 选择 "release" 构建类型
5. 生成的 APK 位于 `app/release/app-release.apk`

## 技术原理

### 无障碍服务

应用通过 Android 的 AccessibilityService 获取屏幕内容并模拟手势操作：

- `dispatchGesture()` - 执行滑动、点击等手势
- `rootInActiveWindow` - 获取当前窗口的节点树
- 通过路径(Path)定义滑动轨迹

### 防检测机制

- 随机延迟（2-8秒不等）
- 随机滑动速度
- 模拟真实用户操作模式

## 添加新的购物APP

在 `MainActivity.kt` 中添加新的APP信息：

```kotlin
private val apps = listOf(
    "淘宝", "京东", "拼多多", "天猫", "苏宁易购",
    "抖音商城", "快手商城", "美团优选",
    "新的APP名称"  // 添加这里
)
private val packages = listOf(
    "com.taobao.taobao",
    "com.jingdong.app.mall",
    // ...
    "com.newapp.package"  // 添加这里
)
```

## 注意事项

1. **权限问题**：首次使用必须开启无障碍服务
2. **电池优化**：建议在系统设置中将本应用加入电池优化白名单
3. **后台运行**：部分系统会杀后台进程，可设置自启动权限
4. **风险提示**：请合理使用，避免违反平台服务条款

## 常见问题

**Q: 为什么无法开始浏览？**
A: 请先在系统设置中开启无障碍服务。

**Q: 浏览过程中停止了怎么办？**
A: 检查电池优化设置，确保应用不被系统杀死。

**Q: 可以同时浏览多个APP吗？**
A: 目前每次只能选择一个APP进行浏览。

**Q: 如何查看浏览进度？**
A: 应用运行时会实时显示滚动次数进度。

## 版本历史

- **v1.0** - 初始版本
  - 支持8个主流购物平台
  - 自动滑动浏览功能
  - 随机延迟防检测

## 许可证

本项目仅供学习和研究使用。请合理使用，遵守相关法律法规和平台服务条款。
