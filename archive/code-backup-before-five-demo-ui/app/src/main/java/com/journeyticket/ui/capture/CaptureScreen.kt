package com.journeyticket.ui.capture

import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.journeyticket.camera.GalleryPicker
import kotlinx.coroutines.launch

/**
 * 拍摄/选图入口（开发文档 §3.1 + t24 用户 UI 调整）：
 * 单一主按钮「上传车票」→ 底部弹层两选项：
 *   📷 拍照上传 —— CameraX 拍照；
 *   🖼️ 相册上传 —— Photo Picker 单入口，纸质票照片与 12306 截图不再让用户区分，
 *      InputKind 由 CaptureViewModel 按图片 MIME 自动判定（PNG→截图预设，其余→纸质票）；
 * 选图后经预处理（EXIF 旋正/压缩/原图落盘）自动进入确认页。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    onNavigateToConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var showSourceSheet by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }

    val galleryLauncher = GalleryPicker.rememberLauncher { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                if (viewModel.onImagePicked(uri)) onNavigateToConfirm()
            }
        }
    }

    // ---- 相机拍摄页（全屏接管）----
    if (showCamera) {
        CameraCapture(
            onCaptured = { bytes ->
                showCamera = false
                if (bytes != null) {
                    scope.launch {
                        if (viewModel.onCameraCaptured(bytes)) onNavigateToConfirm()
                    }
                }
            },
            modifier = modifier,
        )
        return
    }

    // ---- 来源选择弹层（t24：三选项合并为两选项）----
    if (showSourceSheet) {
        ModalBottomSheet(onDismissRequest = { showSourceSheet = false }) {
            Text(
                "上传车票",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
            SourceOption(
                icon = Icons.Filled.PhotoCamera,
                title = "拍照上传",
                subtitle = "打开相机拍摄纸质车票正面",
                enabled = !state.processing,
                onClick = {
                    showSourceSheet = false
                    showCamera = true
                },
            )
            HorizontalDivider()
            SourceOption(
                icon = Icons.Filled.Image,
                title = "相册上传",
                subtitle = "支持纸质票照片与 12306 截图，自动识别类型",
                enabled = !state.processing,
                onClick = {
                    showSourceSheet = false
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            )
            Spacer(Modifier.height(32.dp))
        }
    }

    // ---- 主界面：标题保留 + 单一主按钮 ----
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Spacer(Modifier.height(52.dp))
        Text("旅程记录", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        Text("识别车票", style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold)
        Text("上传车票照片，提取行程信息并生成专属纪念票。",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("AI 识别", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("支持纸质车票照片与 12306 截图，系统会自动判断图片类型。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(
                    onClick = { showSourceSheet = true },
                    enabled = !state.processing,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                ) { Text("上传车票", style = MaterialTheme.typography.titleMedium) }
            }
        }

        if (state.processing) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Text("正在处理图片…", modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.weight(1f))
        Text("你的图片仅发送到已配置的 API 地址。",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 20.dp))
    }
}

/** 弹层选项行：图标 + 标题 + 说明文字（M3 ListItem 风格） */
@Composable
private fun SourceOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
    )
}
