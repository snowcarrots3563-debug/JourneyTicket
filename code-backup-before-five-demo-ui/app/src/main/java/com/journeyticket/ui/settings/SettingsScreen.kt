package com.journeyticket.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.journeyticket.util.AppLogger

/**
 * 设置页（开发文档 §3.5）：API 三配置 + JSON mode 开关 + 默认 DPI + 保存/连接测试。
 * apiKey 密码态显示 + 可见性切换；测试结果错误文案复用 AppError 分类映射。
 */
@OptIn(ExperimentalMaterial3Api::class)   // ExposedDropdownMenuBox / menuAnchor
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(form.saved) {
        if (form.saved) {
            snackbarHostState.showSnackbar("已保存")
            viewModel.consumeSaved()
        }
    }

    // ---- t33：日志 zip 就绪 → FileProvider URI + 系统分享面板 ----
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(form.exportReadyFile) {
        form.exportReadyFile?.let { path ->
            val file = java.io.File(path)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", file,
            )
            val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            runCatching {
                context.startActivity(android.content.Intent.createChooser(send, "导出日志"))
            }.onFailure { e ->
                AppLogger.e("Settings", "share intent failed", e)
            }
            viewModel.consumeExportReady()
            viewModel.refreshLogSize()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 28.dp))
            Text("设置", style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold)
            Text("管理识图服务、票面导出和本地日志。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Text("识图服务", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = form.baseUrl,
                onValueChange = viewModel::onBaseUrlChange,
                label = { Text("baseUrl") },
                placeholder = { Text("https://api.openai.com/v1") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.apiKey,
                onValueChange = viewModel::onApiKeyChange,
                label = { Text("apiKey") },
                singleLine = true,
                visualTransformation =
                    if (form.apiKeyVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = viewModel::onToggleApiKeyVisibility) {
                        Icon(
                            imageVector = if (form.apiKeyVisible) Icons.Filled.VisibilityOff
                            else Icons.Filled.Visibility,
                            contentDescription = if (form.apiKeyVisible) "隐藏 apiKey" else "显示 apiKey",
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            // ---- model：可搜索下拉 + 自定义手输（用户反馈需求） ----
            var modelMenuExpanded by remember { androidx.compose.runtime.mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = modelMenuExpanded,
                onExpandedChange = { modelMenuExpanded = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                val filtered = remember(form.model, form.modelOptions) {
                    if (form.model.isBlank()) form.modelOptions
                    else form.modelOptions.filter { it.contains(form.model, ignoreCase = true) }
                }
                OutlinedTextField(
                    value = form.model,
                    onValueChange = {
                        viewModel.onModelChange(it)
                        modelMenuExpanded = true     // 输入即展开候选（支持搜索/过滤）
                    },
                    label = { Text("model") },
                    placeholder = { Text("下拉选择或手动输入自定义模型名") },
                    supportingText = {
                        Text(
                            if (form.modelOptionsFromRemote) "候选来自 GET /models"
                            else "候选为内置常见多模态模型清单",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable),
                )
                ExposedDropdownMenu(
                    expanded = modelMenuExpanded && filtered.isNotEmpty(),
                    onDismissRequest = { modelMenuExpanded = false },
                ) {
                    filtered.take(8).forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.onModelChange(option)
                                modelMenuExpanded = false
                            },
                        )
                    }
                }
            }
            }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("JSON 结构化输出模式", fontWeight = FontWeight.SemiBold)
                    Text(
                        "部分端点不支持时将自动降级重试一次",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = form.useJsonMode,
                    onCheckedChange = viewModel::onUseJsonModeChange,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ---- 导出默认 DPI ----
            Text("纪念票导出分辨率", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(600 to "标准 600 DPI", 1200 to "高清 1200 DPI").forEach { (dpi, label) ->
                    FilterChip(
                        selected = form.defaultDpi == dpi,
                        onClick = { viewModel.onDefaultDpiChange(dpi) },
                        label = { Text(label) },
                    )
                }
            }
            }

            HorizontalDivider()

            // ---- t33：日志导出 ----
            Text("日志", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
            val logSizeText = remember(form.logSizeBytes) {
                val kb = form.logSizeBytes / 1024.0
                if (kb >= 1024) "%.1f MB".format(kb / 1024) else "%.1f KB".format(kb)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("当前日志大小：$logSizeText")
                    Text(
                        "仅记录运行状态与耗时，不含 apiKey、证件号、照片等个人信息",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(
                    onClick = viewModel::exportLogs,
                    enabled = !form.exportingLogs,
                ) {
                    if (form.exportingLogs) CircularProgressIndicator(Modifier.padding(end = 8.dp))
                    Text(if (form.exportingLogs) "打包中…" else "导出")
                }
            }
            form.exportMessage?.let { msg ->
                Text(msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            }

            HorizontalDivider()

            // ---- 操作区 ----
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = viewModel::save,
                    enabled = !form.saving && !form.testing && form.loaded,
                    modifier = Modifier.weight(1f),
                ) {
                    if (form.saving) CircularProgressIndicator(Modifier.padding(end = 8.dp))
                    Text(if (form.saving) "保存中…" else "保存")
                }
                OutlinedButton(
                    onClick = viewModel::testConnection,
                    enabled = !form.saving && !form.testing && form.loaded,
                    modifier = Modifier.weight(1f),
                ) {
                    if (form.testing) CircularProgressIndicator(Modifier.padding(end = 8.dp))
                    Text(if (form.testing) "测试中…" else "连接测试")
                }
            }

            form.testResult?.let { result ->
                val (text, color) = when (result) {
                    is SettingsViewModel.TestResult.Success ->
                        result.detail to MaterialTheme.colorScheme.primary
                    is SettingsViewModel.TestResult.Failure ->
                        result.userMessage to MaterialTheme.colorScheme.error
                }
                Text(text, color = color, style = MaterialTheme.typography.bodyMedium)
            }

            // 隐私提示（§6）
            Text(
                "apiKey 经设备安全芯片加密存储，不会进入任何日志；" +
                    "车票图片仅发送到你配置的 API 地址。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
