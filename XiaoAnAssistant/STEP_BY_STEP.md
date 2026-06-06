# 小安助手 - APK 构建完整步骤（Windows）

## 📋 前置条件
- Windows 10/11 64位
- 磁盘空间至少 10GB
- 网络连接

---

## 第一步：下载安装 Android Studio

1. 打开浏览器，访问 **https://developer.android.google.cn/studio** （国内可访问）
2. 点击 **"下载 Android Studio"** 按钮
3. 勾选同意协议，点击下载
4. 运行下载的 `android-studio-xxxx-windows.exe`
5. 安装过程中 **一路 Next** 即可（默认选项包含了 JDK 和 Android SDK）
6. 安装路径建议保持默认：`C:\Program Files\Android\Android Studio`

## 第二步：首次启动 Android Studio

1. 打开 Android Studio
2. 首次启动会弹出 Setup Wizard，选择 **Standard** 安装
3. 选择主题（随意），点击 Next
4. 等待 SDK 下载完成（可能需要5-10分钟）
5. 点击 Finish 完成初始设置

## 第三步：打开项目

1. Android Studio 欢迎页面，点击 **Open**
2. 浏览到文件夹：`c:\Users\韦叠凤\CodeBuddy\20260606075517\XiaoAnAssistant`
3. 点击 OK
4. 等待 Gradle Sync 完成（首次可能需要3-5分钟，右下角有进度条）
   - 如果提示下载 SDK，点击下载即可

## 第四步：构建 APK

### 方式 A：直接在平板上运行（推荐）
1. 安卓平板开启 **USB 调试**：
   - 设置 → 关于平板 → 连续点击"版本号"7次（开启开发者模式）
   - 设置 → 开发者选项 → 打开 USB 调试
2. 用 USB 线连接平板和电脑
3. 平板上弹出"允许USB调试"，点确定
4. Android Studio 顶部工具栏选择你的平板设备
5. 点击绿色 ▶ **Run** 按钮
6. 等待编译和安装，App 会自动安装到平板上

### 方式 B：生成 APK 文件
1. 菜单栏 → **Build** → **Build APK(s)**
2. 等待构建完成（右下角有进度提示）
3. 弹出通知后，点击 **locate** 打开文件夹
4. APK 文件位于：`XiaoAnAssistant\app\build\outputs\apk\debug\app-debug.apk`
5. 将这个 APK 文件传到平板上安装即可
   - 可以通过微信/QQ/邮件发送
   - 或者通过 USB 线复制到平板

## 第五步：平板安装 APK

1. 在平板上找到传过来的 `app-debug.apk` 文件
2. 点击安装
3. 如果提示"未知来源"，去 设置 → 安全 → 允许安装未知来源应用
4. 安装完成后打开 **小安助手**

---

## ⚠️ 常见问题

### Q: Gradle Sync 失败/超时
编辑 `gradle.properties`，添加国内镜像：
```
systemProp.http.proxyHost=mirrors.cloud.tencent.com
systemProp.http.proxyPort=80
```
或在项目 `build.gradle` 中添加阿里云镜像仓库。

### Q: SDK 下载慢
在 SDK Manager 中设置代理：
- HTTP Proxy: mirrors.neusoft.edu.cn
- Port: 80

### Q: 语音识别不工作
确保平板安装了语音输入法（如 Google 语音搜索、讯飞语音输入法），并允许网络访问。

### Q: 构建报错 "SDK not found"
点击工具栏的 SDK Manager 图标，下载 Android 14 (API 34) SDK。

---

## 🎯 构建成功标志

看到右下角通知：
```
BUILD SUCCESSFUL in Xs
```
APK 文件自动生成在 `app/build/outputs/apk/debug/` 目录下。
