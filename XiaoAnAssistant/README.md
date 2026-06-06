# 小安助手 - 智能语音助手 App

## 功能简介

1. **登录功能**：用户名+密码登录，不校验密码，任意输入即可进入
2. **语音唤醒**：说出"小安小安"自动唤醒 App，助手回复"你好，我在"
3. **语音对话**：支持用户语音输入，对话内容实时显示在屏幕
4. **TTS 语音合成**：助手回复通过 TTS 朗读出来
5. **ASR 语音识别**：用户语音被识别为文字显示在界面

## 环境要求

- Android Studio (Hedgehog | 2023.1.1 或更高版本)
- JDK 17
- Android SDK 34
- Gradle 8.2

## 构建步骤

### 方式一：Android Studio 构建

1. 用 Android Studio 打开本项目
2. 等待 Gradle Sync 完成
3. 连接安卓平板（开启 USB 调试）
4. 点击 Run 按钮 或 Build > Build APK(s)

### 方式二：命令行构建

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```

构建产物位于：`app/build/outputs/apk/`

## 安装到平板

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 项目结构

```
XiaoAnAssistant/
├── app/
│   ├── build.gradle                    # 应用构建配置
│   ├── src/main/
│   │   ├── AndroidManifest.xml         # 清单文件
│   │   ├── java/com/xiaoan/assistant/
│   │   │   ├── LoginActivity.java      # 登录页（无密码校验）
│   │   │   ├── MainActivity.java       # 主界面（语音唤醒+ASR+TTS）
│   │   │   ├── ChatMessage.java        # 消息数据模型
│   │   │   └── ChatAdapter.java        # 聊天列表适配器
│   │   └── res/
│   │       ├── layout/                 # 布局文件
│   │       ├── drawable/               # 图形资源
│   │       ├── values/                 # 颜色/字符串/主题
│   │       └── mipmap-*/              # 应用图标
├── build.gradle                        # 项目级构建配置
├── settings.gradle                     # 项目设置
└── README.md                           # 本文件
```

## 技术说明

- **语音唤醒**：基于 Android SpeechRecognizer 持续监听，通过文本匹配检测"小安小安"唤醒词
- **ASR**：使用 Android 系统 SpeechRecognizer API（需要 Google 语音搜索或国内语音输入法支持）
- **TTS**：使用 Android 系统 TextToSpeech API
- **注意**：ASR 和语音唤醒功能需要网络连接和语音输入法支持

## 权限说明

- `RECORD_AUDIO`：麦克风录音，用于语音识别
- `INTERNET`：网络访问，语音识别需要联网
- `ACCESS_NETWORK_STATE`：检测网络状态
