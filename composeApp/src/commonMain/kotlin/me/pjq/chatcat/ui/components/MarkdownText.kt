package me.pjq.chatcat.ui.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import com.mikepenz.markdown.compose.Markdown
import kotlinx.coroutines.delay

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    isError: Boolean = false,
    useMarkdownRenderer: Boolean = true,
    isStreaming: Boolean = false
) {
    // During streaming, throttle markdown re-renders every ~300ms for performance
    val renderedContent = if (isStreaming && useMarkdownRenderer) {
        var throttled by remember { mutableStateOf(markdown) }
        LaunchedEffect(markdown) {
            delay(300)
            throttled = markdown
        }
        throttled
    } else {
        markdown
    }

    if (useMarkdownRenderer) {
        val typography = com.mikepenz.markdown.model.DefaultMarkdownTypography(
            h1 = style,
            h2 = style,
            h3 = style,
            h4 = style,
            h5 = style,
            h6 = style,
            text = style,
            code = style,
            inlineCode = style,
            quote = style,
            paragraph = style,
            ordered = style,
            bullet = style,
            list = style,
            link = style,
            textLink = androidx.compose.ui.text.TextLinkStyles(
                style = style.toSpanStyle().copy(color = MaterialTheme.colorScheme.primary),
                hoveredStyle = style.toSpanStyle().copy(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)
            ),
            table = style
        )

        val textColor = if (isError) MaterialTheme.colorScheme.error else LocalContentColor.current
        val colors = com.mikepenz.markdown.model.DefaultMarkdownColors(
            text = textColor,
            codeText = textColor,
            inlineCodeText = textColor,
            linkText = MaterialTheme.colorScheme.primary,
            codeBackground = MaterialTheme.colorScheme.surfaceVariant,
            inlineCodeBackground = MaterialTheme.colorScheme.surfaceVariant,
            dividerColor = MaterialTheme.colorScheme.outlineVariant,
            tableText = textColor,
            tableBackground = MaterialTheme.colorScheme.surfaceVariant
        )

        Markdown(
            content = renderedContent,
            modifier = modifier,
            colors = colors,
            typography = typography
        )
    } else {
        Text(
            text = markdown,
            modifier = modifier,
            style = style,
            color = if (isError) MaterialTheme.colorScheme.error else LocalContentColor.current
        )
    }
}
