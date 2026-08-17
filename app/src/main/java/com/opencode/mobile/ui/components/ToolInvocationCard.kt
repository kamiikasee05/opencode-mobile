package com.opencode.mobile.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.mobile.data.model.ToolInvocation
import com.opencode.mobile.ui.theme.*
import kotlinx.serialization.json.*

@Composable
fun ToolInvocationCard(tool: ToolInvocation) {
    val isRunning = tool.state == "call" || tool.state == "running"
    val isComplete = tool.state == "result" || tool.state == "completed"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 40.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = ToolBg
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, ToolBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    when {
                        tool.toolName?.contains("bash") == true -> Icons.Default.Terminal
                        tool.toolName?.contains("read") == true -> Icons.Default.Visibility
                        tool.toolName?.contains("write") == true -> Icons.Default.Edit
                        tool.toolName?.contains("edit") == true -> Icons.Default.Edit
                        tool.toolName?.contains("glob") == true -> Icons.Default.Folder
                        tool.toolName?.contains("grep") == true -> Icons.Default.Search
                        tool.toolName?.contains("web") == true -> Icons.Default.Language
                        else -> Icons.Default.Build
                    },
                    contentDescription = null,
                    tint = when {
                        isRunning -> StatusYellow
                        isComplete -> StatusGreen
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    tool.toolName ?: "tool",
                    style = MaterialTheme.typography.labelLarge,
                    color = CodeBlockText,
                    fontSize = 12.sp
                )

                Spacer(Modifier.weight(1f))

                if (isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = StatusYellow
                    )
                } else if (isComplete) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Complete",
                        tint = StatusGreen,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Args preview (for running tools)
            if (isRunning && tool.args != null) {
                Spacer(Modifier.height(8.dp))
                val argsText = try {
                    when (tool.args) {
                        is JsonObject -> {
                            val obj = tool.args as JsonObject
                            obj.entries.joinToString(" ") { "${it.key}: ${it.value.jsonPrimitive.contentOrNull?.take(50) ?: ""}" }
                        }
                        else -> tool.args.toString().take(100)
                    }
                } catch (_: Exception) {
                    tool.args.toString().take(100)
                }

                Text(
                    argsText,
                    style = MaterialTheme.typography.labelLarge,
                    color = CodeBlockText.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Result preview (for completed tools)
            if (isComplete && tool.result != null) {
                Spacer(Modifier.height(8.dp))
                val resultText = try {
                    when (tool.result) {
                        is JsonPrimitive -> tool.result.jsonPrimitive.contentOrNull?.take(200)
                        is JsonArray -> "[${tool.result.jsonArray.size} items]"
                        is JsonObject -> tool.result.toString().take(200)
                        else -> tool.result.toString().take(200)
                    }
                } catch (_: Exception) {
                    "Result available"
                }

                if (resultText != null) {
                    Text(
                        resultText,
                        style = MaterialTheme.typography.labelLarge,
                        color = CodeBlockText.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
