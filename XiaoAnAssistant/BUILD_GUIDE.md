# 小安助手 - APK 构建指南

## 当前状态
源码已就绪，但本机未安装 JDK 和 Android SDK，无法直接构建。

## 方案一：安装 Android Studio（推荐）

1. 下载 Android Studio：https://developer.android.com/studio
2. 安装时勾选 Android SDK、Android SDK Platform-Tools
3. 安装完成后打开 Android Studio
4. 选择 "Open an Existing Project" → 打开 `XiaoAnAssistant` 文件夹
5. 等待 Gradle Sync 完成
6. 点击菜单 Build → Build APK(s)
7. APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`
8. 将 APK 传到安卓平板安装

## 方案二：仅安装 JDK + Android SDK 命令行工具

1. 安装 JDK 17：https://adoptium.net/
2. 下载 Android SDK Command-line Tools：https://developer.android.com/studio#command-tools
3. 设置环境变量：
   - JAVA_HOME = JDK安装路径
   - ANDROID_HOME = SDK安装路径
4. 接受许可证：`sdkmanager --licenses`
5. 安装必要组件：
   ```
   sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"
   ```
6. 构建APK：
   ```
   cd XiaoAnAssistant
   gradlew assembleDebug
   ```

## 方案三：使用 GitHub Actions 在线构建（免安装）

1. 在 GitHub 创建仓库，上传本项目代码
2. 添加 `.github/workflows/build.yml` 文件
3. 推送代码后自动构建 APK
4. 从 Actions 页面下载构建好的 APK

## 方案四：使用在线构建服务

- https://build.phonegap.com/ （仅支持 Cordova 项目）
- https://www.bitrise.io/ （CI/CD 平台，支持 Android）
