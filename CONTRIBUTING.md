# 贡献指南

感谢参与 JourneyTicket 的开发。

## 提交前检查

- 确认没有把 API Key、密码、证书或本地配置加入提交。
- 确认截图、构建产物和临时目录没有被意外追踪。
- 对相关改动运行可用的编译或手动验证；当前仓库保存的是 UI 源码快照，尚未包含完整 Gradle 工程。

## 分支和提交

日常开发建议从 `master` 创建短生命周期分支，例如：

```text
feat/ticket-recognition
fix/timeline-layout
docs/update-readme
```

每个提交只表达一个逻辑变更，提交信息使用简洁的 Conventional Commits 风格，例如：

```text
feat: add ticket confirmation flow
fix: adjust timeline spacing
docs: update project status
```

完成验证后推送分支，再通过 GitHub Pull Request 合并。

## 问题反馈

提交 Issue 时请说明复现步骤、预期结果、实际结果，以及相关设备或 Android 版本。涉及票据照片、API Key 等隐私信息时，请先脱敏。
