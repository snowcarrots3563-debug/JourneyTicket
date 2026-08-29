package com.journeyticket.ui.confirm

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.journeyticket.data.local.PassengerIdentity
import com.journeyticket.domain.usecase.ArchiveTripUseCase
import com.journeyticket.util.AppLogger

/**
 * 字段确认页（开发文档 §3.2.4/§5.1）：
 * 左侧票照缩略图 + 右侧逐字段表单预填；Suspicious 红框高亮、Missing 横幅；
 * 底部三操作：重新识别 / 仅生成纪念票（写入会话→Preview 渲染）/ 保存并存档（行程选择弹层→存档链路）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmScreen(
    onGenerateOnly: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecognizeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val archiveState by viewModel.archiveState.collectAsStateWithLifecycle()
    val trips by viewModel.trips.collectAsStateWithLifecycle()
    val passengerIdentities by viewModel.passengerIdentities.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showTripSheet by remember { mutableStateOf(false) }

    // 离开本屏（返回/跳转后不再回来）时清空会话——评审决策第 5 点
    DisposableEffect(Unit) {
        onDispose { viewModel.onScreenDisposed() }
    }

    LaunchedEffect(archiveState.successMessage) {
        archiveState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeArchiveFeedback()
        }
    }

    // ---- 行程选择弹层（新建 / 已有行程）----
    if (showTripSheet) {
        ModalBottomSheet(onDismissRequest = { showTripSheet = false }) {
            Text(
                "保存到行程",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            ListItem(
                headlineContent = { Text("新建行程") },
                supportingContent = { Text("自动按出发月份命名") },
                leadingContent = { Text("➕", style = MaterialTheme.typography.titleMedium) },
                modifier = Modifier
                    .clickable {
                        showTripSheet = false
                        viewModel.archiveTo(ArchiveTripUseCase.Target.New())
                    },
            )
            HorizontalDivider()
            trips.forEach { trip ->
                ListItem(
                    headlineContent = { Text(trip.title ?: "未命名行程") },
                    leadingContent = { Text("🧳") },
                    modifier = Modifier
                        .clickable {
                            showTripSheet = false
                            viewModel.archiveTo(ArchiveTripUseCase.Target.Existing(trip.id))
                        },
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
    // 缩略图解码一次（压缩后 JPEG ≤1600 长边，直接解码安全）
    val thumbnail = remember(state.thumbnailBytes) {
        state.thumbnailBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(28.dp))
        Text("确认车票信息", style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold)
        Text("检查识别结果，确认无误后生成纪念票。",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Missing 横幅（兜底层）
        state.missingBanner?.let { banner ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = MaterialTheme.shapes.large) {
                Text(
                    banner,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large,
        ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            // 左：票照缩略图
            thumbnail?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "票照缩略图",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.width(140.dp),
                )
                Spacer(Modifier.width(16.dp))
            }

            // 右：逐字段表单
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.loading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.height(20.dp).width(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("识别中…", style = MaterialTheme.typography.bodySmall)
                    }
                }
                state.fields.forEach { fieldUi ->
                    if (fieldUi.label == "idNumber") {
                        // t33 需求 B：证件号字段带历史下拉（证件号夹）
                        IdNumberFieldWithHistory(
                            value = fieldUi.value,
                            suspicious = fieldUi.suspicious,
                            history = passengerIdentities,
                            onValueChange = { viewModel.onFieldChange(fieldUi.label, it) },
                            onPick = viewModel::onPickPassengerIdentity,
                            onClearHistory = viewModel::clearPassengerHistory,
                        )
                    } else {
                        OutlinedTextField(
                            value = fieldUi.value,
                            onValueChange = { viewModel.onFieldChange(fieldUi.label, it) },
                            label = {
                                Text(
                                    (FIELD_LABELS[fieldUi.label] ?: fieldUi.label)
                                        + if (fieldUi.suspicious) " ⚠ 请核对" else "",
                                )
                            },
                            isError = fieldUi.suspicious,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // 底部三操作（§3.2.4）
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = viewModel::autoRecognize, enabled = !state.loading) {
                Text("重新识别")
            }
            AssistChip(
                onClick = {
                    // 写入会话供 Preview 单次消费，再跳转渲染
                    viewModel.confirmForRender()
                    onGenerateOnly()
                },
                label = { Text("仅生成纪念票") },
                enabled = !state.loading && state.fields.isNotEmpty(),
            )
            AssistChip(
                onClick = { showTripSheet = true },
                label = {
                    if (archiveState.inProgress) CircularProgressIndicator(
                        Modifier.height(16.dp).width(16.dp),
                    ) else Text("保存并存档")
                },
                enabled = !state.loading && state.fields.isNotEmpty() && !archiveState.inProgress,
            )
        }

        archiveState.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(24.dp))
        }
    }
}

/** Missing 横幅用中文字段名 */
private val FIELD_LABELS = mapOf(
    "fromStation" to "始发站", "toStation" to "终到站", "trainNo" to "车次号",
    "datetime" to "开车时间", "arrivalTime" to "到达时间", "coachSeat" to "车厢座位",
    "seatClass" to "席别", "price" to "票价（元，实付价）", "discount" to "优惠",
    "ticketStatus" to "票据状态", "orderNo" to "订单号", "passengerName" to "姓名",
    "idNumber" to "证件号（脱敏）", "checkGate" to "检票口", "saleStation" to "售票站",
)

/**
 * 证件号输入框 + 历史下拉（t33 需求 B「证件号夹」）：
 * - 聚焦/点击尾图标展开历史列表（脱敏展示「E123***4567 任陈烨」，最近优先）；
 * - 选中回填证件号 + 姓名两字段；底部提供「清空历史」；
 * - 历史仅存本地 DataStore，不进日志、不随日志导出。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IdNumberFieldWithHistory(
    value: String,
    suspicious: Boolean,
    history: List<PassengerIdentity>,
    onValueChange: (String) -> Unit,
    onPick: (PassengerIdentity) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && history.isNotEmpty(),
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                if (!expanded) expanded = true
            },
            label = { Text("证件号（脱敏）" + if (suspicious) " ⚠ 请核对" else "") },
            isError = suspicious,
            singleLine = true,
            trailingIcon = {
                if (history.isNotEmpty()) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Filled.KeyboardArrowUp
                            else Icons.Filled.KeyboardArrowDown,
                            contentDescription = "历史证件号",
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded && history.isNotEmpty(),
            onDismissRequest = { expanded = false },
        ) {
            history.forEach { identity ->
                DropdownMenuItem(
                    text = { Text(displayMasked(identity)) },
                    onClick = {
                        onPick(identity)
                        expanded = false
                    },
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("清空历史", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    onClearHistory()
                    expanded = false
                },
            )
        }
    }
}

/** 历史项脱敏展示：E123456789***4567 任陈烨（仅显示层，原文仍在本地存储） */
private fun displayMasked(identity: PassengerIdentity): String {
    val id = identity.idNumber
    val maskedId =
        if (id.length > 13) id.take(9) + "***" + id.takeLast(4)
        else AppLogger.mask(id)
    return listOf(maskedId, identity.name).filter { it.isNotBlank() }.joinToString(" ")
}
