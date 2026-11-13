package com.virata.hasbele.feature.muezzin

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.virata.hasbele.R
import com.virata.hasbele.core.domain.muezzin.MuezzinPreferences
import com.virata.hasbele.core.domain.muezzin.PrayerFlowId
import com.virata.hasbele.core.domain.muezzin.Recitation
import com.virata.hasbele.core.domain.muezzin.StepKind
import com.virata.hasbele.core.ui.components.DetailTopBar
import com.virata.hasbele.core.ui.components.KeepScreenOnEffect
import com.virata.hasbele.core.ui.components.NativeAdSlot
import com.virata.hasbele.core.ui.components.SectionTitle
import com.virata.hasbele.core.ui.components.rememberNativeAdSlot

/**
 * Vakit listesinde, rıza verildiyse, seçeneklerin altında tek küçük reklam olur; AB onay formu burada
 * açılmaz. Adım adım akış ibadetin kendisidir: orada reklam ve reklamla ilgili kod yoktur, akışa
 * geçilince listenin reklamı da yok edilir. Akış açıkken ekran kapanmaz.
 */
@Composable
fun MuezzinRoute(
    onBack: () -> Unit,
    onOpenCounter: () -> Unit,
    viewModel: MuezzinViewModel = viewModel(factory = MuezzinViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val inFlow = uiState is MuezzinUiState.Flow
    DisposableEffect(viewModel, inFlow, settings.volumeKeysEnabled) {
        if (inFlow && settings.volumeKeysEnabled) viewModel.attachVolumeKeys()
        onDispose { viewModel.detachVolumeKeys() }
    }
    when (val state = uiState) {
        MuezzinUiState.Loading -> Box(modifier = Modifier.fillMaxSize())
        is MuezzinUiState.Home -> MuezzinHomeScreen(
            state = state,
            onBack = onBack,
            onSelectFlow = viewModel::onSelectFlow,
            onResume = viewModel::onResume,
            onTesrikChange = viewModel::onTesrikChange,
            onBayramMorningChange = viewModel::onBayramMorningChange,
            // Yalnız bu dalda çağrılır: akışa geçince yuva bırakılır, reklam yok edilir.
            adSlot = rememberNativeAdSlot(
                adsController = viewModel.adsController,
                enabled = settings.adsAllowed,
                allowConsentForm = false,
            ),
        )
        is MuezzinUiState.Flow -> {
            KeepScreenOnEffect(enabled = true)
            BackHandler(onBack = viewModel::onCloseFlow)
            MuezzinFlowScreen(
                state = state,
                onClose = viewModel::onCloseFlow,
                onNext = viewModel::onNext,
                onPrevious = viewModel::onPrevious,
                onRestart = viewModel::onRestart,
                onCountTesbihat = {
                    viewModel.onCountTesbihat()
                    onOpenCounter()
                },
            )
        }
    }
}

@StringRes
internal fun PrayerFlowId.nameRes(): Int = when (this) {
    PrayerFlowId.SABAH -> R.string.prayer_sabah
    PrayerFlowId.OGLE -> R.string.prayer_ogle
    PrayerFlowId.IKINDI -> R.string.prayer_ikindi
    PrayerFlowId.AKSAM -> R.string.prayer_aksam
    PrayerFlowId.YATSI -> R.string.prayer_yatsi
    PrayerFlowId.CUMA -> R.string.prayer_cuma
    PrayerFlowId.TERAVIH -> R.string.prayer_teravih
    PrayerFlowId.BAYRAM -> R.string.prayer_bayram
}

@StringRes
private fun StepKind.labelRes(): Int = when (this) {
    StepKind.RECITE -> R.string.muezzin_kind_recite
    StepKind.PRAY -> R.string.muezzin_kind_pray
    StepKind.WAIT -> R.string.muezzin_kind_wait
    StepKind.LISTEN -> R.string.muezzin_kind_listen
    StepKind.TESBIHAT -> R.string.muezzin_kind_tesbihat
}

@Composable
fun MuezzinHomeScreen(
    state: MuezzinUiState.Home,
    onBack: () -> Unit,
    onSelectFlow: (PrayerFlowId) -> Unit,
    onResume: () -> Unit,
    onTesrikChange: (Boolean) -> Unit,
    onBayramMorningChange: (Boolean) -> Unit,
    adSlot: NativeAdSlot? = null,
) {
    Scaffold(
        topBar = { DetailTopBar(title = stringResource(R.string.muezzin_title), onBack = onBack) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            state.resume?.let { resume ->
                item {
                    Card(
                        onClick = onResume,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = stringResource(
                                R.string.muezzin_resume,
                                stringResource(resume.flowId.nameRes()),
                                resume.stepNumber,
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
            item { SectionTitle(stringResource(R.string.muezzin_choose)) }
            items(PrayerFlowId.entries) { id ->
                ListItem(
                    modifier = Modifier.clickable { onSelectFlow(id) },
                    headlineContent = { Text(stringResource(id.nameRes())) },
                    supportingContent = if (id == PrayerFlowId.CUMA && state.isFriday) {
                        { Text(stringResource(R.string.muezzin_today_friday)) }
                    } else {
                        null
                    },
                )
            }
            item { SectionTitle(stringResource(R.string.muezzin_section_options)) }
            item {
                OptionRow(
                    title = stringResource(R.string.muezzin_tesrik),
                    hint = stringResource(R.string.muezzin_tesrik_hint),
                    checked = state.preferences.tesrik,
                    onChange = onTesrikChange,
                )
            }
            item {
                OptionRow(
                    title = stringResource(R.string.muezzin_bayram_morning),
                    hint = stringResource(R.string.muezzin_bayram_morning_hint),
                    checked = state.preferences.bayramMorning,
                    onChange = onBayramMorningChange,
                )
            }
            // Tek reklam listenin sonunda durur: vakit satırlarının arasına girmez, anahtarlardan da
            // yanlışlıkla tıklanmasın diye boşlukla ayrılır.
            if (adSlot != null) {
                item(key = MUEZZIN_AD_KEY, contentType = MUEZZIN_AD_KEY) {
                    Box(modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)) { adSlot(0) }
                }
            }
        }
    }
}

private const val MUEZZIN_AD_KEY = "ad"

@Composable
private fun OptionRow(title: String, hint: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        headlineContent = { Text(title) },
        supportingContent = { Text(hint) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
    )
}

@Composable
fun MuezzinFlowScreen(
    state: MuezzinUiState.Flow,
    onClose: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onRestart: () -> Unit,
    onCountTesbihat: () -> Unit,
) {
    val flowName = stringResource(state.flowId.nameRes())
    Scaffold(
        topBar = { DetailTopBar(title = flowName, onBack = onClose) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.finished) {
                    OutlinedButton(onClick = onRestart, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.action_restart))
                    }
                    Button(onClick = onClose, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.muezzin_back_to_list))
                    }
                } else {
                    OutlinedButton(
                        onClick = onPrevious,
                        enabled = state.stepIndex > 0,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.muezzin_previous))
                    }
                    val isLast = state.stepIndex == state.steps.lastIndex
                    Button(onClick = onNext, modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(
                                if (isLast) R.string.muezzin_finish else R.string.muezzin_next,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        // Her adımda kaydırma başa döner.
        key(state.stepIndex, state.finished) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.finished) {
                    Text(
                        text = stringResource(R.string.muezzin_finished, flowName),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                } else {
                    StepContent(state, onCountTesbihat)
                }
            }
        }
    }
}

@Composable
private fun StepContent(state: MuezzinUiState.Flow, onCountTesbihat: () -> Unit) {
    val step = state.step
    Text(
        text = stringResource(R.string.muezzin_step_progress, state.stepIndex + 1, state.steps.size),
        style = MaterialTheme.typography.labelLarge,
    )
    LinearProgressIndicator(
        progress = { (state.stepIndex + 1f) / state.steps.size },
        modifier = Modifier.fillMaxWidth(),
    )
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
        Text(
            text = stringResource(step.kind.labelRes()),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
    // Adım değişince TalkBack yeni yönlendirmeyi kendiliğinden okur.
    Text(
        text = step.instruction,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
    if (step.kind == StepKind.TESBIHAT) {
        Button(onClick = onCountTesbihat, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.muezzin_count_tesbihat))
        }
        Text(
            text = stringResource(R.string.muezzin_count_tesbihat_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    step.recitation?.let { RecitationContent(it) }
}

@Composable
private fun RecitationContent(recitation: Recitation) {
    var showMeaning by rememberSaveable(recitation.id) { mutableStateOf(false) }
    val arabic = recitation.arabic?.lines()
    val readings = recitation.transliteration.lines()
    val meanings = recitation.meaning?.lines()

    HorizontalDivider()
    readings.forEachIndexed { index, reading ->
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            arabic?.getOrNull(index)?.let { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        textDirection = TextDirection.Rtl,
                        fontSize = 26.sp,
                        lineHeight = 46.sp,
                    ),
                    textAlign = TextAlign.Right,
                    // TalkBack Arapçayı Türkçe sesle bozuk okur; hemen altındaki okunuşu okusun.
                    modifier = Modifier
                        .fillMaxWidth()
                        .clearAndSetSemantics {},
                )
            }
            Text(text = reading, style = MaterialTheme.typography.bodyLarge)
            if (showMeaning) {
                meanings?.getOrNull(index)?.let { meaning ->
                    Text(
                        text = meaning,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
    if (meanings != null) {
        TextButton(onClick = { showMeaning = !showMeaning }) {
            Text(
                stringResource(
                    if (showMeaning) R.string.muezzin_hide_meaning else R.string.muezzin_show_meaning,
                ),
            )
        }
    } else {
        recitation.meaningRef?.let { ref ->
            Text(
                text = stringResource(R.string.muezzin_meaning_ref, ref),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    Text(
        text = stringResource(R.string.muezzin_source, recitation.source),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
