package com.virata.hasbele.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.virata.hasbele.R
import com.virata.hasbele.core.domain.model.AppSettings
import com.virata.hasbele.core.domain.model.ThemeMode
import com.virata.hasbele.core.ui.BeadStyles
import com.virata.hasbele.core.ui.components.AdsConsentDialog
import com.virata.hasbele.core.ui.components.DetailTopBar
import com.virata.hasbele.core.ui.components.NativeAdSlot
import com.virata.hasbele.core.ui.components.SectionTitle
import com.virata.hasbele.core.ui.components.findActivity
import com.virata.hasbele.core.ui.components.rememberNativeAdSlot

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val settings by viewModel.settings.collectAsState()
    val privacyOptionsRequired by viewModel.privacyOptionsRequired.collectAsState()
    val view = LocalView.current
    SettingsScreen(
        settings = settings,
        privacyOptionsRequired = privacyOptionsRequired,
        onBack = onBack,
        onSoundChange = viewModel::onSoundChange,
        onHapticsChange = viewModel::onHapticsChange,
        onVolumeKeysChange = viewModel::onVolumeKeysChange,
        onKeepScreenOnChange = viewModel::onKeepScreenOnChange,
        onThemeChange = viewModel::onThemeChange,
        onBeadStyleChange = viewModel::onBeadStyleChange,
        onAdsConsentGiven = viewModel::onAdsConsentGiven,
        onAdsConsentDenied = viewModel::onAdsConsentDenied,
        onPrivacyOptions = { viewModel.onPrivacyOptions(view.context.findActivity()) },
        adSlot = rememberNativeAdSlot(
            adsController = viewModel.adsController,
            enabled = settings.adsAllowed,
            allowConsentForm = true,
        ),
    )
}

@Composable
fun SettingsScreen(
    settings: AppSettings,
    privacyOptionsRequired: Boolean,
    onBack: () -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onVolumeKeysChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onBeadStyleChange: (String) -> Unit,
    onAdsConsentGiven: () -> Unit,
    onAdsConsentDenied: () -> Unit,
    onPrivacyOptions: () -> Unit,
    adSlot: NativeAdSlot? = null,
) {
    var askAdsConsent by rememberSaveable { mutableStateOf(false) }
    // Gizlilik politikası, değerlendirme ve sürüm Hakkında sayfasındadır; burada yalnız ayarlar durur.
    Scaffold(
        topBar = {
            DetailTopBar(title = stringResource(R.string.screen_settings), onBack = onBack)
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item { SectionTitle(stringResource(R.string.settings_section_feedback)) }
            item {
                SwitchRow(
                    title = stringResource(R.string.settings_sound),
                    checked = settings.soundEnabled,
                    onChange = onSoundChange,
                )
            }
            item {
                SwitchRow(
                    title = stringResource(R.string.settings_vibration),
                    checked = settings.hapticsEnabled,
                    onChange = onHapticsChange,
                )
            }
            item { SectionTitle(stringResource(R.string.settings_section_counting)) }
            item {
                SwitchRow(
                    title = stringResource(R.string.settings_volume_keys),
                    hint = stringResource(R.string.settings_volume_keys_hint),
                    checked = settings.volumeKeysEnabled,
                    onChange = onVolumeKeysChange,
                )
            }
            item {
                SwitchRow(
                    title = stringResource(R.string.settings_keep_screen_on),
                    hint = stringResource(R.string.settings_keep_screen_on_hint),
                    checked = settings.keepScreenOn,
                    onChange = onKeepScreenOnChange,
                )
            }
            item { SectionTitle(stringResource(R.string.settings_section_appearance)) }
            item {
                ChoiceRow(
                    title = stringResource(R.string.settings_theme),
                    options = listOf(
                        ThemeMode.SYSTEM to stringResource(R.string.settings_theme_system),
                        ThemeMode.LIGHT to stringResource(R.string.settings_theme_light),
                        ThemeMode.DARK to stringResource(R.string.settings_theme_dark),
                    ),
                    selected = settings.theme,
                    onSelect = onThemeChange,
                )
            }
            item {
                ChoiceRow(
                    title = stringResource(R.string.settings_bead_style),
                    options = BeadStyles.all.map { it.id to stringResource(it.nameRes) },
                    selected = BeadStyles.find(settings.beadStyleId).id,
                    onSelect = onBeadStyleChange,
                )
            }
            item { SectionTitle(stringResource(R.string.settings_section_ads)) }
            item {
                // Açma-kapama anahtarı yoktur: reklam uygulamanın parçasıdır. Satır yalnız KVKK
                // rızasını gösterir ve aynı diyalogla verdirir ya da geri aldırır.
                ListItem(
                    modifier = Modifier.clickable { askAdsConsent = true },
                    headlineContent = { Text(stringResource(R.string.settings_ads_preference)) },
                    supportingContent = {
                        Column {
                            Text(
                                stringResource(
                                    if (settings.adsAllowed) {
                                        R.string.settings_ads_consent_given
                                    } else {
                                        R.string.settings_ads_consent_not_given
                                    },
                                ),
                            )
                            Text(stringResource(R.string.settings_ads_hint))
                        }
                    },
                )
            }
            // Rıza yokken Google onayı hiçbir şey için kullanılmadığından giriş gizlenir.
            if (privacyOptionsRequired && settings.adsAllowed) {
                item {
                    ListItem(
                        modifier = Modifier.clickable(onClick = onPrivacyOptions),
                        headlineContent = {
                            Text(stringResource(R.string.settings_privacy_options))
                        },
                    )
                }
            }
            // Tek reklam listenin sonunda, anahtarlardan boşlukla ayrılmış durur; yanlışlıkla
            // tıklanmasın diye bir anahtarın ya da seçim düğmesinin hemen yanına konmaz.
            if (adSlot != null) {
                item(key = SETTINGS_AD_KEY, contentType = SETTINGS_AD_KEY) {
                    Box(modifier = Modifier.padding(top = 24.dp)) { adSlot(0) }
                }
            }
        }
    }
    if (askAdsConsent) {
        AdsConsentDialog(
            onAccept = {
                askAdsConsent = false
                onAdsConsentGiven()
            },
            // Rıza verilmişse ret onu geri almaktır.
            onDecline = {
                askAdsConsent = false
                onAdsConsentDenied()
            },
            onDismiss = { askAdsConsent = false },
        )
    }
}

private const val SETTINGS_AD_KEY = "ad"

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    hint: String? = null,
) {
    ListItem(
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        headlineContent = { Text(title) },
        supportingContent = hint?.let { text -> { Text(text) } },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ChoiceRow(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, (value, label) ->
                SegmentedButton(
                    selected = value == selected,
                    onClick = { onSelect(value) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                ) {
                    Text(label)
                }
            }
        }
    }
}
