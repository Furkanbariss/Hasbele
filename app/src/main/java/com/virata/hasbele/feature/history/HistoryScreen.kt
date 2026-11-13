package com.virata.hasbele.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.virata.hasbele.R
import com.virata.hasbele.core.ui.components.DetailTopBar
import com.virata.hasbele.core.ui.components.NativeAdSlot
import com.virata.hasbele.core.ui.components.SectionTitle
import com.virata.hasbele.core.ui.components.findActivity
import com.virata.hasbele.core.ui.components.rememberNativeAdSlot
import com.virata.hasbele.core.ui.displayTransliteration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HistoryRoute(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val reviewDue by viewModel.reviewDue.collectAsState()
    val view = LocalView.current
    LaunchedEffect(reviewDue) {
        if (reviewDue) {
            viewModel.reviewController.requestReview(view.context.findActivity())
            viewModel.onReviewRequested()
        }
    }
    HistoryScreen(
        uiState = uiState,
        onBack = onBack,
        adSlot = rememberNativeAdSlot(
            adsController = viewModel.adsController,
            enabled = settings.adsAllowed,
            allowConsentForm = true,
        ),
    )
}

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onBack: () -> Unit,
    adSlot: NativeAdSlot? = null,
) {
    Scaffold(
        topBar = {
            DetailTopBar(title = stringResource(R.string.screen_history), onBack = onBack)
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item { TodayCard(uiState) }
            // Reklam yuvaları yalnız kartların arasına girer; yerleri HistoryAdSlots.kt'dedir.
            if (adSlot != null) {
                item(key = "ad-$HISTORY_TODAY_AD_SLOT", contentType = AD_CONTENT_TYPE) {
                    adSlot(HISTORY_TODAY_AD_SLOT)
                }
            }
            if (uiState.pastDays.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.history_last_30_days)) }
                uiState.pastDays.forEachIndexed { index, day ->
                    item(key = day.date.toString()) { DayCard(day) }
                    val slot = historyAdSlotAfterDay(index, uiState.pastDays.size)
                    if (adSlot != null && slot != null) {
                        item(key = "ad-$slot", contentType = AD_CONTENT_TYPE) { adSlot(slot) }
                    }
                }
            }
        }
    }
}

private const val AD_CONTENT_TYPE = "ad"

@Composable
private fun TodayCard(uiState: HistoryUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.history_today),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = uiState.todayTotal.toString(),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (uiState.todayEntries.isEmpty()) {
                Text(
                    text = stringResource(R.string.history_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Entries(uiState.todayEntries)
            }
        }
    }
}

@Composable
private fun DayCard(day: HistoryDay) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {},
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatDate(day.date),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = day.total.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Entries(day.entries)
        }
    }
}

@Composable
private fun Entries(entries: List<HistoryEntry>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        entries.forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {},
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = entry.zikir?.displayTransliteration()
                        ?: stringResource(R.string.history_deleted_zikir),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = entry.count.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun formatDate(date: LocalDate): String {
    val locale = LocalConfiguration.current.locales[0]
    return remember(date, locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale).format(date)
    }
}
