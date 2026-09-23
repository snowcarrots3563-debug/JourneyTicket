# JourneyTicket

JourneyTicket 是一款 Android 旅行票据管理应用，用来识别车票、整理行程，并生成可保存的纪念票面。

## 功能

- 拍摄或从相册导入车票图片，支持单张和批量识别。
- 使用设备端中文 OCR，或在设置中配置兼容 OpenAI API 的视觉模型。
- 检查并编辑识别结果，将车票保存到行程时间线。
- 管理常用乘车人信息，减少重复填写。
- 预览并导出纪念票图片，支持二维码生成。
- 按行程归档记录，并检测重复车票。

本地 OCR 在设备上识别图片。选择远程视觉模型时，车票图片会发送到你配置的 API 服务；应用不会自带 API 服务或密钥。

## 技术栈

- Kotlin、Jetpack Compose、Material 3
- Android Gradle Plugin 8.7.3、Kotlin 2.0.21、Gradle 8.9
- Room、DataStore、Hilt、Navigation Compose
- CameraX、ML Kit 中文文字识别
- Retrofit、OkHttp、kotlinx.serialization

## 环境要求

- Android Studio，或安装 Android SDK 的本地开发环境
- JDK 17
- Android SDK Platform 35
- 最低支持 Android 10（API 29）

Gradle Wrapper 会使用仓库配置下载 Gradle 8.9。首次构建还需要联网下载 Android Gradle Plugin 和依赖。

## 构建与运行

Windows PowerShell：

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

macOS 或 Linux：

```bash
./gradlew assembleDebug
./gradlew installDebug
```

Debug APK 输出路径：

```text
app/build/outputs/apk/debug/app-debug.apk
```

运行 JVM 单元测试：

```powershell
.\gradlew.bat testDebugUnitTest
```

macOS 或 Linux 将命令替换为 `./gradlew testDebugUnitTest`。

## 目录结构

```text
.
├── app/                       # Android 应用模块
│   └── src/
│       ├── main/java/com/journeyticket/
│       │   ├── camera/        # 拍摄、选图与图像预处理
│       │   ├── data/          # 本地数据、识别服务与仓库
│       │   ├── di/            # 依赖注入
│       │   ├── domain/        # 领域模型与业务用例
│       │   ├── render/        # 票面绘制与图片导出
│       │   ├── ui/            # Compose 页面
│       │   └── util/          # 通用工具
│       └── test/              # JVM 单元测试
├── design/                    # 设计说明与 HTML 演示稿
├── docs/
│   ├── handoffs/              # 开发交接记录
│   └── maestro/               # Maestro 自动化相关资料
├── artifacts/
│   ├── screenshots/           # 项目截图
│   └── test-inputs/           # 测试输入文件
├── archive/                   # 历史源码备份
└── gradle/                    # Gradle Wrapper 与依赖版本目录
```

## 许可证

仓库目前未附许可证。使用或再分发前，请先与项目维护者确认授权。
