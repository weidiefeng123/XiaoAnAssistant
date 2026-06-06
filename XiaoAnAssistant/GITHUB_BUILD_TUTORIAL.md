# GitHub 在线自动构建 APK - 详细图文教程

## 📌 原理说明
GitHub Actions 提供免费的云端服务器，我们预先配置好的 `.github/workflows/build.yml` 会在你推送代码后自动在云端编译 Android 项目，生成 APK 文件供你下载。整个过程不需要在本机安装任何开发工具。

---

## 第一步：注册 GitHub 账号

1. 打开 https://github.com/signup
2. 填写用户名、邮箱、密码
3. 完成验证（puzzle 验证码）
4. 到邮箱中点击验证链接
5. 选择 Free 免费计划

> 如果已有账号，直接登录 https://github.com/login 即可

---

## 第二步：创建新仓库

1. 登录 GitHub 后，点击右上角 **+** 号 → **New repository**
2. 填写仓库信息：
   - **Repository name**：`XiaoAnAssistant`
   - **Description**：`智能语音助手 - Android App`
   - **可见性**：选择 **Public**（公开，才能使用免费 Actions）
   - **不要勾选** "Add a README file"、"Add .gitignore"、"Choose a license"
3. 点击 **Create repository**

---

## 第三步：上传项目代码

### 方式 A：网页直接上传（最简单，推荐新手）

1. 打开你刚创建的仓库页面 `https://github.com/你的用户名/XiaoAnAssistant`
2. 点击 **"uploading an existing file"** 链接
3. 把 `XiaoAnAssistant` 文件夹里的**所有文件和文件夹**拖拽到上传区域
4. 在底部 Commit 信息填写：`初始化小安助手项目`
5. 点击 **Commit changes**

**⚠️ 重要：必须上传的文件列表**

```
XiaoAnAssistant/
├── .github/
│   └── workflows/
│       └── build.yml              ← 这个是关键！自动构建配置
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/xiaoan/assistant/
│           │   ├── ChatAdapter.java
│           │   ├── ChatMessage.java
│           │   ├── LoginActivity.java
│           │   └── MainActivity.java
│           └── res/
│               ├── drawable/       (9个xml文件)
│               ├── layout/         (4个xml文件)
│               ├── mipmap-anydpi-v26/ (2个xml文件)
│               ├── values/         (3个xml文件)
│               └── xml/            (1个xml文件)
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew
└── gradlew.bat
```

> 注意：以下文件**不需要上传**（与构建无关）：
> - download.html
> - qrcode.png / qrcode_download.png
> - README.md / BUILD_GUIDE.md / STEP_BY_STEP.md

### 方式 B：使用 Git 命令上传（如果已安装 Git）

打开终端，依次执行：

```bash
cd c:\Users\韦叠凤\CodeBuddy\20260606075517\XiaoAnAssistant

git init
git add .github/ app/ gradle/ build.gradle settings.gradle gradle.properties gradlew gradlew.bat
git commit -m "初始化小安助手项目"

# 将下面的"你的用户名"替换为你的 GitHub 用户名
git remote add origin https://github.com/你的用户名/XiaoAnAssistant.git
git branch -M main
git push -u origin main
```

---

## 第四步：等待自动构建

1. 代码推送后，GitHub Actions 会**自动开始构建**
2. 进入仓库页面，点击顶部 **"Actions"** 标签页
3. 左侧可以看到工作流名称 **"Build Android APK"**
4. 点击最新的运行记录，查看实时构建日志
5. 整个过程大约需要 **3-8 分钟**

### 构建状态说明

| 图标 | 状态 | 说明 |
|------|------|------|
| 🟡 黄色圆圈 | 正在构建 | 等待即可 |
| 🟢 绿色勾 | 构建成功 | 可以下载 APK 了！ |
| 🔴 红色叉 | 构建失败 | 点击查看错误日志 |

> 首次推送可能需要手动启用 Actions：在 Actions 页面点击 **"I understand my workflows, go ahead and enable them"**

---

## 第五步：下载 APK

1. 构建成功后（绿色勾），点击该次运行记录
2. 页面滚动到下方 **"Artifacts"** 区域
3. 可以看到两个文件：
   - **app-debug** — Debug 版 APK（推荐，直接可安装）
   - **app-release-unsigned** — Release 版 APK（未签名）
4. 点击 **app-debug** 下载 ZIP 文件
5. 解压 ZIP，里面就是 `app-debug.apk`
6. 将 APK 传到安卓平板安装

---

## 📱 传输 APK 到平板的方法

| 方法 | 操作 |
|------|------|
| 微信/QQ | 电脑端发送文件给"文件传输助手"，平板端接收 |
| 邮件 | 以附件形式发给自己，平板端下载 |
| USB线 | 复制到平板存储 |
| 网盘 | 上传到百度网盘/阿里云盘，平板端下载 |

---

## ⚠️ 常见问题

### Q: 推送后 Actions 没有自动运行？
在仓库的 Actions 页面，点击黄色提示条中的 **"Enable Actions"** 按钮。

### Q: 构建失败怎么办？
1. 点击失败的运行记录
2. 展开 **"Build Debug APK"** 步骤
3. 查看红色错误信息
4. 常见原因：文件缺失、build.gradle 配置错误

### Q: 网页上传文件太多很麻烦？
可以用 Git 命令行上传更方便，或者把项目压缩后用 GitHub CLI 工具上传。

### Q: 下载的 APK 安装时提示"未知来源"？
在平板上：设置 → 安全 → 允许安装未知来源应用 → 打开开关

### Q: APK 安装后语音识别不工作？
需要确保平板安装了语音输入法（Google 语音搜索/讯飞语音输入法），并联网使用。

---

## 🔄 后续更新代码

如果修改了代码，只需要重新推送，GitHub Actions 会自动重新构建生成新的 APK：

```bash
git add .
git commit -m "更新代码"
git push
```

然后去 Actions 页面下载新的 APK 即可。
