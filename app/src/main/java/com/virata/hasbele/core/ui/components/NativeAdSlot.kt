package com.virata.hasbele.core.ui.components

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.virata.hasbele.BuildConfig
import com.virata.hasbele.R
import com.virata.hasbele.core.ads.AdsController
import com.virata.hasbele.core.ads.NativeAdPool
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Listelerin arasına konan küçük yerel reklamın yuvası. Ekran bunu `slot` numarasıyla çağırır; reklam
 * yüklenemezse yuva hiçbir şey çizmez.
 */
typealias NativeAdSlot = @Composable (slot: Int) -> Unit

/**
 * Yalnız Zikirler, Müezzinlik vakit listesi, Geçmiş ve Ayarlar'da kullanılır. Tesbih sayacına, adım adım
 * müezzin akışına, Hakkında'ya ve ilk açılışa konmaz.
 *
 * [enabled] kullanıcının KVKK açık rızasıdır. Rıza yokken null döner: onay formu açılmaz, SDK
 * başlatılmaz, reklam istenmez ve ekran reklam yuvası koymaz. Onay ve SDK hazır olana kadar da null
 * döner. Yüklenen reklamlar ekran bırakılınca yok edilir.
 *
 * [allowConsentForm] false olan ekranlarda (Zikirler, Müezzinlik) Google'ın AB onay formu hiç açılmaz;
 * form gerekiyorsa o ekranda reklam gösterilmez.
 */
@Composable
fun rememberNativeAdSlot(
    adsController: AdsController,
    enabled: Boolean,
    allowConsentForm: Boolean,
): NativeAdSlot? {
    if (!enabled) return null
    val view = LocalView.current
    LaunchedEffect(adsController) {
        adsController.prepare(view.context.findActivity(), allowConsentForm)
    }
    val canShowAds by adsController.canShowAds.collectAsState()
    if (!canShowAds) return null
    return rememberPooledSlot()
}

@Composable
private fun rememberPooledSlot(): NativeAdSlot {
    val context = LocalContext.current
    val pool = remember(context) { NativeAdPool(context, BuildConfig.ADMOB_NATIVE_UNIT_ID) }
    DisposableEffect(pool) {
        onDispose { pool.destroy() }
    }
    return remember<NativeAdSlot>(pool) { { slot -> PooledNativeAd(pool, slot) } }
}

@Composable
private fun PooledNativeAd(pool: NativeAdPool, slot: Int) {
    LaunchedEffect(pool, slot) { pool.request(slot) }
    val ad = pool.ads[slot] ?: return
    NativeAdCard(ad)
}

@Immutable
private data class AdCardColors(
    val container: Int,
    val outline: Int,
    val headline: Int,
    val body: Int,
    val badge: Int,
    val onBadge: Int,
    val action: Int,
    val onAction: Int,
)

/**
 * İçerik kartlarından ayırt edilsin diye çerçeveli, "Reklam" etiketli küçük kart. AdChoices simgesini
 * SDK sağ üst köşeye kendisi koyar.
 */
@Composable
private fun NativeAdCard(ad: NativeAd, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val colors = AdCardColors(
        container = scheme.surfaceContainerLow.toArgb(),
        outline = scheme.outlineVariant.toArgb(),
        headline = scheme.onSurface.toArgb(),
        body = scheme.onSurfaceVariant.toArgb(),
        badge = scheme.secondaryContainer.toArgb(),
        onBadge = scheme.onSecondaryContainer.toArgb(),
        action = scheme.primary.toArgb(),
        onAction = scheme.onPrimary.toArgb(),
    )
    AndroidView(
        factory = { viewContext ->
            LayoutInflater.from(viewContext).inflate(R.layout.native_ad_card, null) as NativeAdView
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        update = { adView ->
            adView.applyColors(colors)
            if (adView.tag !== ad) {
                adView.bind(ad)
                adView.tag = ad
            }
        },
    )
}

/** Politika: başlık her zaman; simge, metin ve düğme reklamda varsa gösterilir ve kaydedilir. */
private fun NativeAdView.bind(ad: NativeAd) {
    val headline = findViewById<TextView>(R.id.ad_headline)
    headline.text = ad.headline
    headlineView = headline

    val body = findViewById<TextView>(R.id.ad_body)
    body.text = ad.body
    body.visibility = if (ad.body.isNullOrBlank()) View.GONE else View.VISIBLE
    bodyView = body

    val icon = findViewById<ImageView>(R.id.ad_icon)
    val iconDrawable = ad.icon?.drawable
    icon.setImageDrawable(iconDrawable)
    icon.visibility = if (iconDrawable == null) View.GONE else View.VISIBLE
    iconView = icon

    val action = findViewById<Button>(R.id.ad_call_to_action)
    action.text = ad.callToAction
    action.visibility = if (ad.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE
    callToActionView = action

    setNativeAd(ad)
}

private fun NativeAdView.applyColors(colors: AdCardColors) {
    val density = resources.displayMetrics.density
    findViewById<View>(R.id.ad_container).background = GradientDrawable().apply {
        cornerRadius = 12 * density
        setColor(colors.container)
        setStroke((1 * density).toInt().coerceAtLeast(1), colors.outline)
    }
    findViewById<TextView>(R.id.ad_badge).apply {
        background = GradientDrawable().apply {
            cornerRadius = 4 * density
            setColor(colors.badge)
        }
        setTextColor(colors.onBadge)
    }
    findViewById<TextView>(R.id.ad_headline).setTextColor(colors.headline)
    findViewById<TextView>(R.id.ad_body).setTextColor(colors.body)
    findViewById<Button>(R.id.ad_call_to_action).apply {
        background = GradientDrawable().apply {
            cornerRadius = 18 * density
            color = ColorStateList.valueOf(colors.action)
        }
        setTextColor(colors.onAction)
    }
}
