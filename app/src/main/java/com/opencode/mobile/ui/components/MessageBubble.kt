package com.opencode.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.mobile.data.model.Message
import com.opencode.mobile.data.model.MessagePart
import com.opencode.mobile.ui.theme.*

@Composable
fun MessageBubble(
    message: Message,
    isUser: Boolean
) {
    val textParts = message.parts.filter { it.type == "text" && !it.text.isNullOrBlank() }
    if (textParts.isEmpty()) return

    val fullText = textParts.joinToString("\n") { it.text!! }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Assistant avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = "Assistant",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Card(
                modifier = Modifier,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) UserBubbleLight else AssistantBubbleLight
                )
            ) {
                MarkdownText(
                    text = fullText,
                    modifier = Modifier.padding(12.dp),
                    textColor = if (isUser) androidx.compose.ui.graphics.Color.White
                        else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (isUser) {
            Spacer(Modifier.width(8.dp))
            // User avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "You",
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Simple markdown rendering using Compose spans.
 * For a full implementation, use Markwon in a WebView or Compose bridge.
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    // Basic markdown: handle code blocks, bold, italic
    val processedText = remember(text) {
        text
            .replace(Regex("```[\\s\\S]*?```"), { match ->
                // Code blocks - extract content
                match.value.removePrefix("```").removeSuffix("```")
                    .removePrefix("kotlin").removePrefix("java").removePrefix("python")
                    .removePrefix("javascript").removePrefix("bash").removePrefix("json")
                    .trimIndent()
            })
            .replace(Regex("`([^`]+)`"), "$1") // inline code
            .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1") // bold
            .replace(Regex("\\*([^*]+)\\*"), "$1") // italic
    }

    Text(
        text = processedText,
        modifier = modifier,
        color = textColor,
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 22.sp
    )
}
