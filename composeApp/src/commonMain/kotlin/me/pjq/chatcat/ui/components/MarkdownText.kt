package me.pjq.chatcat.ui.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import com.mikepenz.markdown.compose.Markdown

/**
 * A composable that renders Markdown text using the multiplatform-markdown-renderer library.
 *
 * @param markdown The Markdown text to render
 * @param modifier The modifier to be applied to the composable
 * @param style The text style to apply to the rendered Markdown
 * @param isError Whether the text should be displayed as an error
 * @param useMarkdownRenderer Whether to use the markdown renderer or simple Text component
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    isError: Boolean = false,
    useMarkdownRenderer: Boolean = true,
    isStreaming: Boolean = false
) {
    // During streaming, use simple text to avoid repeated expensive parsing
    val effectiveUseMarkdown = useMarkdownRenderer && !isStreaming
    
    if (effectiveUseMarkdown) {
        // Create typography using DefaultMarkdownTypography with the provided style
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

        // Create colors with error handling and all required parameters
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
            content = markdown,
            modifier = modifier,
            colors = colors,
            typography = typography
        )
    } else {
        // Use simple Text component when markdown rendering is disabled
        Text(
            text = markdown,
            modifier = modifier,
            style = style,
            color = if (isError) MaterialTheme.colorScheme.error else LocalContentColor.current
        )
    }
}
