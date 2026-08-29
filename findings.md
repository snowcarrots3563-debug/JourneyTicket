# 调研发现

- 用户提供的 `E:\claude work place\旅程记录` 可读取，包含 `JourneyTicket` 源码、APK 发布目录和项目文档。
- `JourneyTicket/app/build.gradle.kts` 当前为 `versionCode = 14`、`versionName = "0.4.9"`，`minSdk = 29`、`targetSdk = 35`。
- `进度快照.md` 与会话归档记录不完全一致：快照称 v0.4.8 黑票未根治并暂停 t54，但实际源码配置已是 v0.4.9；需以源码和构建结果为准。
- 最高优先级仍是 600DPI 票面黑屏/巨字异常；历史方案是 300DPI 渲染后放大。
