package com.virata.hasbele.core.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import java.util.WeakHashMap

private const val DIM_BRIGHTNESS = 0.01f

/** Pencere başına ekranı açık tutmak isteyen ekran sayısı; yalnız ana thread'den değişir. */
private val keepScreenOnHolders = WeakHashMap<Window, Int>()

/**
 * Bu composable ekrandayken ekranı açık tutar; ekrandan çıkınca bayrak kalkar. Bayrak doğrudan
 * pencereye yazılır: `View.keepScreenOn` bir sonraki kareyi bekler, durgun ekranda kalkmayabilir.
 *
 * Geçişte yeni ekran eskisi kapanmadan açılır; sayaç olmasaydı Tesbih ekranı kapanırken müezzin
 * ekranının istediği bayrağı da silerdi.
 */
@Composable
fun KeepScreenOnEffect(enabled: Boolean) {
    val window = rememberWindow()
    DisposableEffect(window, enabled) {
        if (enabled) acquireKeepScreenOn(window)
        onDispose { if (enabled) releaseKeepScreenOn(window) }
    }
}

private fun acquireKeepScreenOn(window: Window) {
    val holders = (keepScreenOnHolders[window] ?: 0) + 1
    keepScreenOnHolders[window] = holders
    if (holders == 1) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

private fun releaseKeepScreenOn(window: Window) {
    val holders = (keepScreenOnHolders[window] ?: 1) - 1
    if (holders > 0) {
        keepScreenOnHolders[window] = holders
    } else {
        keepScreenOnHolders.remove(window)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

/** Ekranı en düşük parlaklığa alır; ekrandan çıkınca sistem parlaklığına döner. */
@Composable
fun DimWindowEffect() {
    val window = rememberWindow()
    DisposableEffect(window) {
        window.attributes = window.attributes.apply { screenBrightness = DIM_BRIGHTNESS }
        onDispose {
            window.attributes = window.attributes.apply {
                screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
        }
    }
}

@Composable
private fun rememberWindow(): Window {
    val view = LocalView.current
    return remember(view) { view.context.findActivity().window }
}

internal tailrec fun Context.findActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> error("Pencere etkisi bir Activity dışında kullanılamaz")
}
