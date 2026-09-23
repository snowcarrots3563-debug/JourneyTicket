package com.journeyticket.ui.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 纪念票预览（开发文档 §3.3/§5.2）：
 * 大图缩放适配展示 + 「保存到相册」+「重新生成」；渲染 loading 态。
 * 票面恒为蓝票配色，与深色主题无关。
 *
 * t45 显示链路审计结论：
 * - 数据源：仅加载 images 表 kind=RENDERED 的文件（时间线列表不内嵌图片，点击进本屏）；
 *   THUMBNAIL 当前显示链路不经过（留作未来列表缩略图）；
 * - 解码：BitmapFactory.decodeFile 全尺寸解码（600DPI ≈ 2001×1276 ARGB_8888 ≈10MB，
 *   单张可接受；后续做列表缩略图时需加 inSampleSize）；
 * - 显示：ContentScale.FillWidth + aspectRatio(85/54) 与位图比例一致，无二次拉伸失真；
 *   Compose Image 绘制不修改位图内容；
 * - 300/600 混用风险：t39 单飞渲染 + 键控去重后，State 位图只会被「同键刷新」或
 *   「保存时全分辨率重渲」（与 lastFields 同源）覆盖，不存在跨票错配；
 * - 触发链：LaunchedEffect(Unit)→onEnter() 一次；渲染由 VM 内请求键去重（t39），
 *   本屏重组不会引发重渲。
 */
@Composable
fun TicketPreviewScreen(
    modifier: Modifier = Modifier,
    viewModel: TicketPreviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // 进入即分发预览入口（t30 修正：必须走 onEnter() 双入口分发——
    // 确认页 pendingRenderFields 生成 + 时间线 pendingViewRecordId 回看；
    // 直接调 generateFromSession() 会绕过回看路径，时间线点卡片将报"无可渲染信息"）
    LaunchedEffect(Unit) { viewModel.onEnter() }
    DisposableEffect(Unit) {
        onDispose { viewModel.onScreenDisposed() }
    }

    LaunchedEffect(state.savedMessage) {
        state.savedMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeSavedMessage()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 28.dp))
            Text("纪念票预览", style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold)
            Text("确认这张属于你的旅程纪念。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            val bitmap = state.bitmap
            if (state.rendering) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(Modifier.padding(end = 4.dp))
                    Text("正在渲染纪念票…")
                }
            } else if (bitmap != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "蓝色磁介质风格纪念票",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth().aspectRatio(85f / 54f),
                    )
                }
            } else {
                Text("暂无可预览的纪念票")
            }

            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { viewModel.saveToGallery(context) },
                    enabled = !state.saving && !state.rendering && state.bitmap != null,
                    modifier = Modifier.weight(1f),
                ) {
                    if (state.saving) CircularProgressIndicator(Modifier.padding(end = 6.dp))
                    Text(if (state.saving) "保存中…" else "保存到相册")
                }
                OutlinedButton(
                    onClick = viewModel::regenerate,
                    enabled = !state.saving && !state.rendering && state.bitmap != null,
                    modifier = Modifier.weight(1f),
                ) { Text("重新生成") }
            }

            Text(
                "本图为个人纪念收藏之美术再创作，不作为乘车凭证",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp),
            )
        }
    }
}
