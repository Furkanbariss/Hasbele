package com.virata.hamdele.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.virata.hamdele.R

/**
 * KVKK İlke Kararı 2026/347: aydınlatma ile açık rıza ayrı başlıkta durur, düğmeler yalnız rızayı
 * cevaplar. Kabul ve ret eşit ağırlıktadır; ret hiçbir özelliği kısıtlamaz.
 *
 * İlk açılışta ve Ayarlar'daki "Reklam tercihi" satırında aynı diyalog kullanılır. Dışarı dokunup
 * kapatmak ([onDismiss]) cevap sayılmaz: rıza durumu değişmez.
 */
@Composable
fun AdsConsentDialog(onAccept: () -> Unit, onDecline: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ads_consent_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.ads_consent_info_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.ads_consent_info),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.ads_consent_statement_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.ads_consent_statement),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAccept) { Text(stringResource(R.string.ads_consent_accept)) }
        },
        dismissButton = {
            TextButton(onClick = onDecline) { Text(stringResource(R.string.ads_consent_decline)) }
        },
    )
}
