package com.virata.hasbele.feature.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.virata.hasbele.BuildConfig
import com.virata.hasbele.R
import com.virata.hasbele.core.ui.components.AdsConsentDialog
import com.virata.hasbele.core.ui.components.DetailTopBar
import com.virata.hasbele.core.ui.components.SectionTitle
import com.virata.hasbele.core.ui.components.findActivity

/** Reklam yoktur; uygulamayı tanıtan sayfa sade kalır. */
@Composable
fun AboutRoute(
    onBack: () -> Unit,
    viewModel: AboutViewModel = viewModel(factory = AboutViewModel.Factory),
) {
    val view = LocalView.current
    val address = stringResource(R.string.contact_email)
    AboutScreen(
        versionName = BuildConfig.VERSION_NAME,
        onBack = onBack,
        onRateApp = { viewModel.onRateApp(view.context.findActivity()) },
        onContact = { viewModel.onContact(view.context.findActivity(), address) },
    )
}

@Composable
fun AboutScreen(
    versionName: String,
    onBack: () -> Unit,
    onRateApp: () -> Unit,
    onContact: () -> Unit,
) {
    var showPrivacyPolicy by rememberSaveable { mutableStateOf(false) }
    var showLicenses by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { DetailTopBar(title = stringResource(R.string.screen_about), onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
        ) {
            Paragraph(stringResource(R.string.about_intro))
            SectionTitle(stringResource(R.string.welcome_muezzin_title))
            Paragraph(stringResource(R.string.about_muezzin))
            SectionTitle(stringResource(R.string.welcome_source_title))
            Paragraph(stringResource(R.string.about_religious))
            SectionTitle(stringResource(R.string.welcome_privacy_title))
            Paragraph(stringResource(R.string.welcome_privacy))
            ListItem(
                modifier = Modifier.clickable { showPrivacyPolicy = true },
                headlineContent = { Text(stringResource(R.string.settings_privacy_policy)) },
            )
            SectionTitle(stringResource(R.string.about_section_app))
            ListItem(
                modifier = Modifier.clickable(onClick = onRateApp),
                headlineContent = { Text(stringResource(R.string.settings_rate_app)) },
                supportingContent = { Text(stringResource(R.string.settings_rate_app_hint)) },
            )
            ListItem(
                modifier = Modifier.clickable(onClick = onContact),
                headlineContent = { Text(stringResource(R.string.about_contact)) },
                supportingContent = { Text(stringResource(R.string.contact_email)) },
            )
            ListItem(
                modifier = Modifier.clickable { showLicenses = true },
                headlineContent = { Text(stringResource(R.string.about_licenses)) },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.app_name)) },
                supportingContent = {
                    Text(stringResource(R.string.settings_about_version, versionName))
                },
            )
        }
    }

    if (showPrivacyPolicy) {
        TextDialog(
            title = stringResource(R.string.settings_privacy_policy),
            text = stringResource(R.string.privacy_policy_text),
            onDismiss = { showPrivacyPolicy = false },
        )
    }
    if (showLicenses) {
        TextDialog(
            title = stringResource(R.string.about_licenses),
            text = stringResource(R.string.about_licenses_text),
            onDismiss = { showLicenses = false },
        )
    }
}

@Composable
private fun Paragraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

/** Gizlilik politikası Google Play'in istediği gibi uygulama içinden de okunabilir. */
@Composable
private fun TextDialog(title: String, text: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Text(
                text = text,
                modifier = Modifier.verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        },
    )
}

@Composable
fun WelcomeRoute(
    onContinue: () -> Unit,
    viewModel: WelcomeViewModel = viewModel(factory = WelcomeViewModel.Factory),
) {
    val step by viewModel.step.collectAsState()
    val view = LocalView.current
    LaunchedEffect(step) {
        when (step) {
            WelcomeStep.INTRO -> Unit
            WelcomeStep.PREPARING_ADS -> viewModel.prepareAds(view.context.findActivity())
            WelcomeStep.DONE -> onContinue()
        }
    }
    WelcomeScreen(
        busy = step != WelcomeStep.INTRO,
        onAdsConsentAnswered = viewModel::onAdsConsentAnswered,
    )
}

/**
 * Yalnız ilk kurulumda bir kez çıkar; kısa tutulur, ayrıntı Hakkında sayfasındadır. "Anladım, başla"
 * önce reklam için KVKK açık rıza diyaloğunu açar; cevap verilmeden Tesbih ekranına geçilmez.
 * [busy] iken Google'ın onay adımı sürer, düğme bekler.
 */
@Composable
fun WelcomeScreen(busy: Boolean, onAdsConsentAnswered: (granted: Boolean) -> Unit) {
    var askAdsConsent by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        bottomBar = {
            Button(
                onClick = { askAdsConsent = true },
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.welcome_continue))
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            WelcomeItem(R.string.welcome_tesbih_title, R.string.welcome_tesbih)
            WelcomeItem(R.string.welcome_muezzin_title, R.string.welcome_muezzin)
            WelcomeItem(R.string.welcome_source_title, R.string.welcome_source)
            WelcomeItem(R.string.welcome_privacy_title, R.string.welcome_privacy)
            Text(
                text = stringResource(R.string.welcome_more),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (askAdsConsent) {
        AdsConsentDialog(
            onAccept = {
                askAdsConsent = false
                onAdsConsentAnswered(true)
            },
            onDecline = {
                askAdsConsent = false
                onAdsConsentAnswered(false)
            },
            // Dışarı dokunmak cevap değildir: bilgilendirmede kalınır, düğme yeniden sorar.
            onDismiss = { askAdsConsent = false },
        )
    }
}

@Composable
private fun WelcomeItem(title: Int, body: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = stringResource(title), style = MaterialTheme.typography.titleMedium)
        Text(text = stringResource(body), style = MaterialTheme.typography.bodyLarge)
    }
}
