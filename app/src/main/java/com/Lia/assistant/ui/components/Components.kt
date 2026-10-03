package com.Lia.assistant.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.Lia.assistant.ui.theme.LocalNovaReducedMotion
import com.Lia.assistant.ui.theme.NovaShapes
import com.Lia.assistant.ui.theme.NovaSpacing
import com.Lia.assistant.ui.theme.depthSurface
import com.Lia.assistant.ui.theme.novaColors
import androidx.compose.material3.MaterialTheme

@Composable
fun NovaGlassCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val base = modifier.depthSurface(NovaShapes.medium, 6.dp).clip(NovaShapes.medium)
    val m = if (onClick != null) base.clickable(onClick = onClick) else base
    Box(m.padding(NovaSpacing.lg)) { content() }
}

enum class NovaButtonStyle { PRIMARY, SECONDARY, TEXT }

@Composable
fun NovaButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    style: NovaButtonStyle = NovaButtonStyle.PRIMARY, enabled: Boolean = true,
) {
    val c = novaColors
    val reduced = LocalNovaReducedMotion.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && !reduced) 0.96f else 1f, tween(150), label = "press")
    val (bg, fg) = when (style) {
        NovaButtonStyle.PRIMARY -> c.accent to c.onAccent
        NovaButtonStyle.SECONDARY -> c.surfaceGlass to c.textPrimary
        NovaButtonStyle.TEXT -> Color.Transparent to c.accent
    }
    Box(
        modifier
            .scale(scale)
            .alpha(if (enabled) 1f else 0.5f)
            .heightIn(min = 48.dp)
            .clip(NovaShapes.pill)
            .background(bg)
            .let { if (style == NovaButtonStyle.SECONDARY) it.depthSurface(NovaShapes.pill, 4.dp) else it }
            .clickable(interactionSource = source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = fg, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun NovaTextField(
    value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier,
    secret: Boolean = false, singleLine: Boolean = true,
) {
    val c = novaColors
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        singleLine = singleLine, modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
        shape = NovaShapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = c.accent, unfocusedBorderColor = c.surfaceBorder,
            focusedTextColor = c.textPrimary, unfocusedTextColor = c.textPrimary,
            focusedLabelColor = c.accent, unfocusedLabelColor = c.textSecondary, cursorColor = c.accent,
        ),
    )
}

@Composable
fun NovaTopBar(title: String, onBack: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = novaColors.textPrimary)
            }
        } else Spacer(Modifier.width(NovaSpacing.lg))
        Text(title, style = MaterialTheme.typography.headlineSmall, color = novaColors.textPrimary)
    }
}

@Composable
fun NovaSectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), modifier.padding(top = NovaSpacing.xl, bottom = NovaSpacing.sm),
        style = MaterialTheme.typography.labelMedium, color = novaColors.textTertiary, fontWeight = FontWeight.SemiBold)
}

@Composable
fun NovaSettingsRow(
    icon: ImageVector, title: String, subtitle: String? = null, value: String? = null,
    checked: Boolean? = null, onCheckedChange: ((Boolean) -> Unit)? = null, onClick: (() -> Unit)? = null,
) {
    val c = novaColors
    val m = Modifier.fillMaxWidth().heightIn(min = 56.dp).let {
        if (onClick != null) it.clickable(onClick = onClick) else if (checked != null && onCheckedChange != null)
            it.clickable { onCheckedChange(!checked) } else it
    }.padding(vertical = 6.dp)
    Row(m, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(c.accentGlow), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = c.accent)
        }
        Spacer(Modifier.width(NovaSpacing.md))
        Column(Modifier.weight(1f)) {
            Text(title, color = c.textPrimary, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) Text(subtitle, color = c.textSecondary, style = MaterialTheme.typography.bodySmall)
        }
        if (value != null) Text(value, color = c.textSecondary, style = MaterialTheme.typography.labelLarge)
        if (checked != null) Switch(
            checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = c.accent, checkedThumbColor = c.onAccent),
        )
    }
}

data class ChatMessage(
    val id: Long, val text: String, val isUser: Boolean,
    val isStreaming: Boolean = false, val isError: Boolean = false,
)

@Composable
fun NovaMessageBubble(message: ChatMessage, modifier: Modifier = Modifier, actions: @Composable (() -> Unit)? = null) {
    val c = novaColors
    val shape = NovaShapes.medium
    Column(modifier.fillMaxWidth(), horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start) {
        Box(
            Modifier.widthIn(max = 320.dp)
                .let { if (message.isUser) it.background(c.userBubble, shape) else it.depthSurface(shape, 3.dp) }
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(message.text, color = if (message.isError) c.error else c.textPrimary, style = MaterialTheme.typography.bodyLarge)
        }
        if (!message.isUser && !message.isStreaming && actions != null) Row { actions() }
    }
}

@Composable
fun NovaTypingDots(modifier: Modifier = Modifier) {
    val reduced = LocalNovaReducedMotion.current
    val t = androidx.compose.animation.core.rememberInfiniteTransition(label = "dots")
    Row(modifier.semantics { contentDescription = "Typing" }, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { i ->
            val a by t.animateFloat(
                0.3f, 1f,
                androidx.compose.animation.core.infiniteRepeatable(
                    androidx.compose.animation.core.keyframes { durationMillis = 900; 0.3f at (i * 150); 1f at (i * 150 + 300); 0.3f at (i * 150 + 600) },
                ), label = "dot$i")
            Box(Modifier.size(7.dp).clip(CircleShape).background(novaColors.textSecondary.copy(alpha = if (reduced) 0.7f else a)))
        }
    }
}

@Composable
fun NovaEmptyState(title: String, actionText: String? = null, onAction: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.Inbox, null, tint = novaColors.textTertiary, modifier = Modifier.size(48.dp))
        Spacer(Modifier.size(12.dp))
        Text(title, color = novaColors.textSecondary, style = MaterialTheme.typography.titleMedium)
        if (actionText != null && onAction != null) {
            Spacer(Modifier.size(16.dp))
            NovaButton(actionText, onAction)
        }
    }
}
