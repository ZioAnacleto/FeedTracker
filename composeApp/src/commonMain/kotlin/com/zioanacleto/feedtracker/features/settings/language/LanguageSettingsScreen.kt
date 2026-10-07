package com.zioanacleto.feedtracker.features.settings.language

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.zioanacleto.feedtracker.domain.preferences.AppLanguage
import com.zioanacleto.feedtracker.theme.ScreenHorizontalPadding
import com.zioanacleto.feedtracker.theme.feedTrackerScreenWindowInsets
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.back
import feedtracker.composeapp.generated.resources.language
import feedtracker.composeapp.generated.resources.language_description
import feedtracker.composeapp.generated.resources.language_english
import feedtracker.composeapp.generated.resources.language_english_description
import feedtracker.composeapp.generated.resources.language_italian
import feedtracker.composeapp.generated.resources.language_italian_description
import feedtracker.composeapp.generated.resources.language_system
import feedtracker.composeapp.generated.resources.language_system_description
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LanguageSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: LanguageSettingsViewModel = koinViewModel(),
    onBackButtonClick: () -> Unit,
) {
    val language by viewModel.language.collectAsState()

    Column(
        modifier = modifier
            .feedTrackerScreenWindowInsets()
            .fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            IconButton(
                onClick = onBackButtonClick,
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack),
                    contentDescription = stringResource(Res.string.back),
                )
            }
            Text(
                text = stringResource(Res.string.language),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding)
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        ) {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.language_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                )
                Spacer(modifier = Modifier.height(4.dp))
                LanguageChoice(
                    title = stringResource(Res.string.language_system),
                    subtitle = stringResource(Res.string.language_system_description),
                    selected = language == AppLanguage.SYSTEM,
                    onClick = { viewModel.onLanguageChange(AppLanguage.SYSTEM) },
                )
                LanguageChoice(
                    title = stringResource(Res.string.language_english),
                    subtitle = stringResource(Res.string.language_english_description),
                    selected = language == AppLanguage.ENGLISH,
                    onClick = { viewModel.onLanguageChange(AppLanguage.ENGLISH) },
                )
                LanguageChoice(
                    title = stringResource(Res.string.language_italian),
                    subtitle = stringResource(Res.string.language_italian_description),
                    selected = language == AppLanguage.ITALIAN,
                    onClick = { viewModel.onLanguageChange(AppLanguage.ITALIAN) },
                )
            }
        }
    }
}

@Composable
private fun LanguageChoice(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        shape = MaterialTheme.shapes.small,
        color = if (selected) colors.surfaceContainerHighest else colors.surface.copy(alpha = 0.55f),
        contentColor = colors.onSurface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) colors.outline else colors.outline.copy(alpha = 0.45f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.75f),
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.outline,
                )
            }
        }
    }
}
