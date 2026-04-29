package com.serenade.app.feature.you.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.serenade.app.ui.design.SrEyebrow
import com.serenade.app.ui.design.SrScreenBackground
import com.serenade.app.ui.design.SrSurfaceCard
import com.serenade.app.ui.theme.SerenadeThemeChoice
import com.serenade.app.ui.theme.SrBgDeep
import com.serenade.app.ui.theme.SrCoral
import com.serenade.app.ui.theme.SrLineHi
import com.serenade.app.ui.theme.SrOnPrimary
import com.serenade.app.ui.theme.SrPlum
import com.serenade.app.ui.theme.SrPrimary
import com.serenade.app.ui.theme.SrSurfaceHi
import com.serenade.app.ui.theme.SrText
import com.serenade.app.ui.theme.SrTextDim
import com.serenade.app.ui.theme.SrTextMute
import com.serenade.app.ui.theme.colorsFor

@Composable
fun YouScreen(
    selectedTheme: SerenadeThemeChoice,
    onThemeSelected: (SerenadeThemeChoice) -> Unit,
    onDownloadsClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SrScreenBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProfileHeader()
            ThemeSelector(
                selectedTheme = selectedTheme,
                onThemeSelected = onThemeSelected,
            )
            SrSurfaceCard {
                SettingRow(
                    icon = Icons.Default.CloudDownload,
                    title = "Downloads",
                    subtitle = "Offline tracks and cached HLS",
                    onClick = onDownloadsClick,
                )
                SettingRow(
                    icon = Icons.Default.Security,
                    title = "Security",
                    subtitle = "Tokens stay in Android Keystore",
                )
                SettingRow(
                    icon = Icons.Default.Storage,
                    title = "Storage",
                    subtitle = "Private app storage only",
                )
            }
            SrSurfaceCard {
                SettingRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Sign out",
                    subtitle = "Return to sign in",
                    destructive = true,
                    onClick = onLogout,
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ProfileHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(SrPrimary, SrPlum))),
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = SrOnPrimary)
        }
        Column(modifier = Modifier.weight(1f)) {
            SrEyebrow("You")
            Text(
                text = "Your listening room",
                style = MaterialTheme.typography.displaySmall,
                color = SrText,
            )
            Text(
                text = "Theme, downloads, and local settings",
                style = MaterialTheme.typography.bodySmall,
                color = SrTextDim,
            )
        }
    }
}

@Composable
private fun ThemeSelector(
    selectedTheme: SerenadeThemeChoice,
    onThemeSelected: (SerenadeThemeChoice) -> Unit,
) {
    SrSurfaceCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Default.Palette, contentDescription = null, tint = SrPrimary)
            Column {
                Text("Theme", style = MaterialTheme.typography.titleMedium, color = SrText)
                Text("Saved on this device", style = MaterialTheme.typography.bodySmall, color = SrTextDim)
            }
        }
        Spacer(Modifier.height(12.dp))
        SerenadeThemeChoice.entries.forEach { choice ->
            ThemeOption(
                choice = choice,
                selected = choice == selectedTheme,
                onClick = { onThemeSelected(choice) },
            )
        }
    }
}

@Composable
private fun ThemeOption(
    choice: SerenadeThemeChoice,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val previewColors = colorsFor(choice)
    val swatches = listOf(previewColors.primary, previewColors.plum, previewColors.coral)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(if (selected) SrSurfaceHi else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) SrPrimary else Color.Transparent,
                shape = MaterialTheme.shapes.medium,
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
            swatches.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(1.dp, SrLineHi, CircleShape),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(choice.label, style = MaterialTheme.typography.titleSmall, color = SrText)
            Text(choice.subtitle, style = MaterialTheme.typography.bodySmall, color = SrTextDim)
        }
        if (selected) {
            Text(
                text = "On",
                style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                color = SrPrimary,
            )
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    destructive: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (destructive) SrCoral.copy(alpha = 0.16f) else SrSurfaceHi),
        ) {
            Icon(icon, contentDescription = null, tint = if (destructive) SrCoral else SrPrimary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = if (destructive) SrCoral else SrText)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SrTextMute)
        }
    }
}
