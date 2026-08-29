# JourneyTicket

JourneyTicket 是一款 Android 旅行票据管理应用，帮助用户识别、整理和回顾旅途中的车票信息。

## 当前能力

- 票据图片选择与识别流程
- 票据信息确认与编辑
- 行程时间线展示
- 票面预览与个性化视觉设计
- 设置页面与主题样式

## 项目状态

当前仓库保存的是 JourneyTicket 的 UI 开发源码快照和交互演示稿，仍处于持续开发阶段。Android 工程的完整 Gradle 配置和业务数据层尚未整理到本仓库中，因此暂不能直接通过本仓库构建 APK。

## 目录说明

```text
code-backup-before-five-demo-ui/
└── app/src/main/java/com/journeyticket/ui/
    ├── capture/       # 票据采集
    ├── confirm/       # 信息确认
    ├── home/          # 首页
    ├── navigation/    # 页面导航
    ├── preview/       # 票面预览
    ├── settings/      # 设置
    ├── theme/         # 主题与颜色
    └── timeline/      # 行程时间线
```

根目录中的 HTML 文件是各版本的界面演示稿，Markdown 文件记录设计决策、开发进度和交接事项。

## 技术方向

- Kotlin
- Jetpack Compose
- Material 3

## 开发说明

后续整理完整 Android 工程时，建议补充 `settings.gradle.kts`、模块级 `build.gradle.kts`、AndroidManifest 和资源目录，再接入真实的 OCR 与本地数据存储。

## License

项目许可证尚未确定。
