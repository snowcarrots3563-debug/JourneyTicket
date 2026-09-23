package com.journeyticket.ui.timeline

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.journeyticket.data.local.TicketRecordEntity
import com.journeyticket.ui.theme.TimelineBackground
import com.journeyticket.ui.theme.trainAccentColor
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 时间线（开发文档 §3.4.2 设计规范）：
 * 「旅途」标题区 + 深色统计卡（次数/里程/城市数）+ ISO 日期分组头 +
 * 白卡三列布局（车级 accent bar）+ 点击进详情 + 长按删除 + 页脚提示。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineScreen(
    onOpenTicket: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TimelineViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingDelete by remember { mutableStateOf<Long?>(null) }
    pendingDelete?.let { recordId ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除行程记录") },
            text = { Text("删除后关联的图片数据将一并清除，且不可恢复。确定删除？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTicket(recordId)
                    pendingDelete = null
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TimelineBackground)
            .padding(horizontal = 20.dp, vertical = 28.dp),
    ) {
        // ---- 标题区 ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("我的旅途", style = MaterialTheme.typography.displaySmall)
                Text(
                    "${state.totalCount} 趟行程，按时间收好。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            IconButton(onClick = { }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "更多选项")
            }
        }
        Spacer(Modifier.height(24.dp))

        // ---- 深色统计卡（次数实时；里程/城市 M3；点击 Toast 占位）----
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .combinedClickable(onClick = {
                    android.widget.Toast.makeText(
                        context, "统计详情页将在 M3 版本上线", android.widget.Toast.LENGTH_SHORT,
                    ).show()
                }),
        ) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
                StatItem("次数", state.totalCount.toString(), Modifier.weight(1f))
                StatItem("里程", "--", Modifier.weight(1f))
                StatItem("城市数", "--", Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(6.dp))

        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        // ---- 分组列表 ----
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            state.groups.forEach { group ->
                item(key = "header_${group.dateLabel}") {
                    // 分组标题：左对齐纯文本日期头（ISO、常规字重、小字号）
                    Text(
                        group.dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp),
                    )
                }
                items(group.rows.size, key = { idx -> group.rows[idx].record.id }) { idx ->
                    TicketCard(
                        record = group.rows[idx].record,
                        onClick = {
                            viewModel.openTicket(group.rows[idx].record.id)
                            onOpenTicket()
                        },
                        onLongClick = { pendingDelete = group.rows[idx].record.id },
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
            item(key = "footer") {
                Text(
                    "长按可删除数据",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 24.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

/** 白色圆角卡片（12dp 圆角轻阴影）：左侧 4dp 车级配色条 + 三列内容结构 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TicketCard(
    record: TicketRecordEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val zone = ZoneId.systemDefault()
    val depTime = LocalTime.ofInstant(Instant.ofEpochMilli(record.departureAt), zone)
    val arrTime = record.arrivalAt?.let { LocalTime.ofInstant(Instant.ofEpochMilli(it), zone) }
    val accent = trainAccentColor(record.trainNo)

    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Row {
            // 左侧竖向彩色条（accent bar，约 4dp）
            Box(
                Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accent),
            )

            // 三列内容
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 左列：出发时间大号粗体 + 出发站名
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1.1f)) {
                    Text(
                        "%02d:%02d".format(depTime.hour, depTime.minute),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF222B38),
                    )
                    Text(record.fromStation, style = MaterialTheme.typography.bodyMedium)
                }
                // 中列：车次号（accent 同色加粗）+ 日期小字 + ticketStatus 状态文字
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1.2f),
                ) {
                    Text(record.trainNo, color = accent, fontWeight = FontWeight.Bold)
                    Text(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            .format(Instant.ofEpochMilli(record.departureAt).atZone(zone)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9AA3B0),
                    )
                    record.ticketStatus?.let { status ->
                        Text(status, style = MaterialTheme.typography.labelSmall, color = accent)
                    }
                }
                // 右列：到达时间（null → --:--）+ 到达站名
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.1f)) {
                    Text(
                        arrTime?.let { "%02d:%02d".format(it.hour, it.minute) } ?: "--:--",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF222B38),
                    )
                    Text(record.toStation, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
