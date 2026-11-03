package ma7moud3ly.fast.compose.bindings.material3

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.Typography
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ma7moud3ly.fast.compose.interpreter.Registry

/**
 * More Material 3 APIs seen in real apps: `BasicAlertDialog`, top app bar state, defaults of buttons, cards, radio
 * buttons, switches, sliders, tabs and sheets, `contentColorFor`, tonal elevation, `FilledIconToggleButton`,
 * `Typography.copy` and the dynamic color schemes.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal fun Registry.materialMore() {
    pullToRefresh()
    composable("BasicAlertDialog(onDismissRequest, modifier, properties, content)") { a ->
        BasicAlertDialog(
            onDismissRequest = a.action("onDismissRequest"),
            modifier = a.modifier(),
            properties = a.typed("properties", DialogProperties()),
            content = a.content(),
        )
    }

    type<TopAppBarState>("TopAppBarState")
    composable("rememberTopAppBarState(initialHeightOffsetLimit, initialHeightOffset, initialContentOffset)") { a ->
        rememberTopAppBarState(
            a.float("initialHeightOffsetLimit", -Float.MAX_VALUE),
            a.float("initialHeightOffset", 0f),
            a.float("initialContentOffset", 0f),
        )
    }
    properties<TopAppBarState>(
        "collapsedFraction" to { it.collapsedFraction },
        "overlappedFraction" to { it.overlappedFraction },
        "heightOffset" to { it.heightOffset },
        "heightOffsetLimit" to { it.heightOffsetLimit },
        "contentOffset" to { it.contentOffset },
    )

    member<ButtonDefaults>("IconSize") { _, _ -> ButtonDefaults.IconSize }
    member<ButtonDefaults>("IconSpacing") { _, _ -> ButtonDefaults.IconSpacing }
    member<ButtonDefaults>("MinHeight") { _, _ -> ButtonDefaults.MinHeight }
    member<ButtonDefaults>("MinWidth") { _, _ -> ButtonDefaults.MinWidth }
    composableMember<ButtonDefaults>("buttonElevation(defaultElevation, pressedElevation, focusedElevation, hoveredElevation, disabledElevation)") { _, a ->
        ButtonDefaults.buttonElevation(
            defaultElevation = a.dp("defaultElevation", 0.dp),
            pressedElevation = a.dp("pressedElevation", 0.dp),
            focusedElevation = a.dp("focusedElevation", 0.dp),
            hoveredElevation = a.dp("hoveredElevation", 1.dp),
            disabledElevation = a.dp("disabledElevation", 0.dp),
        )
    }
    composableMember<ButtonDefaults>("outlinedButtonColors(containerColor, contentColor, disabledContainerColor, disabledContentColor)") { _, a ->
        ButtonDefaults.outlinedButtonColors(
            containerColor = a.color("containerColor"),
            contentColor = a.color("contentColor"),
            disabledContainerColor = a.color("disabledContainerColor"),
            disabledContentColor = a.color("disabledContentColor"),
        )
    }
    composableMember<ButtonDefaults>("textButtonColors(containerColor, contentColor, disabledContainerColor, disabledContentColor)") { _, a ->
        ButtonDefaults.textButtonColors(
            containerColor = a.color("containerColor"),
            contentColor = a.color("contentColor"),
            disabledContainerColor = a.color("disabledContainerColor"),
            disabledContentColor = a.color("disabledContentColor"),
        )
    }
    composableMember<CardDefaults>("outlinedCardColors(containerColor, contentColor, disabledContainerColor, disabledContentColor)") { _, a ->
        CardDefaults.outlinedCardColors(
            containerColor = a.color("containerColor"),
            contentColor = a.color("contentColor"),
            disabledContainerColor = a.color("disabledContainerColor"),
            disabledContentColor = a.color("disabledContentColor"),
        )
    }
    composableMember<CardDefaults>("elevatedCardColors(containerColor, contentColor, disabledContainerColor, disabledContentColor)") { _, a ->
        CardDefaults.elevatedCardColors(
            containerColor = a.color("containerColor"),
            contentColor = a.color("contentColor"),
            disabledContainerColor = a.color("disabledContainerColor"),
            disabledContentColor = a.color("disabledContentColor"),
        )
    }

    value("RadioButtonDefaults", RadioButtonDefaults)
    composableMember<RadioButtonDefaults>("colors(selectedColor, unselectedColor, disabledSelectedColor, disabledUnselectedColor)") { _, a ->
        androidx.compose.material3.RadioButtonDefaults.colors(
            selectedColor = a.color("selectedColor"),
            unselectedColor = a.color("unselectedColor"),
            disabledSelectedColor = a.color("disabledSelectedColor"),
            disabledUnselectedColor = a.color("disabledUnselectedColor"),
        )
    }
    value("SwitchDefaults", SwitchDefaults)
    composableMember<SwitchDefaults>("colors(checkedThumbColor, checkedTrackColor, checkedBorderColor, checkedIconColor, uncheckedThumbColor, uncheckedTrackColor, uncheckedBorderColor, uncheckedIconColor)") { _, a ->
        androidx.compose.material3.SwitchDefaults.colors(
            checkedThumbColor = a.color("checkedThumbColor"),
            checkedTrackColor = a.color("checkedTrackColor"),
            checkedBorderColor = a.color("checkedBorderColor"),
            checkedIconColor = a.color("checkedIconColor"),
            uncheckedThumbColor = a.color("uncheckedThumbColor"),
            uncheckedTrackColor = a.color("uncheckedTrackColor"),
            uncheckedBorderColor = a.color("uncheckedBorderColor"),
            uncheckedIconColor = a.color("uncheckedIconColor"),
        )
    }
    value("SliderDefaults", SliderDefaults)
    composableMember<SliderDefaults>("colors(thumbColor, activeTrackColor, activeTickColor, inactiveTrackColor, inactiveTickColor)") { _, a ->
        androidx.compose.material3.SliderDefaults.colors(
            thumbColor = a.color("thumbColor"),
            activeTrackColor = a.color("activeTrackColor"),
            activeTickColor = a.color("activeTickColor"),
            inactiveTrackColor = a.color("inactiveTrackColor"),
            inactiveTickColor = a.color("inactiveTickColor"),
        )
    }
    value("TabRowDefaults", TabRowDefaults)
    composableMember<TabRowDefaults>("SecondaryIndicator(modifier, height, color)") { _, a ->
        androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
            modifier = a.modifier(),
            height = a.dp("height", 2.dp),
            color = a.color("color", androidx.compose.material3.TabRowDefaults.secondaryContentColor),
        )
    }
    composableMember<TabRowDefaults>("PrimaryIndicator(modifier, width, height, color, shape)") { _, a ->
        androidx.compose.material3.TabRowDefaults.PrimaryIndicator(
            modifier = a.modifier(),
            width = a.dp("width", 24.dp),
            height = a.dp("height", 3.dp),
            color = a.color("color", androidx.compose.material3.TabRowDefaults.primaryContentColor),
            shape = a.typed<Shape>("shape", androidx.compose.foundation.shape.RoundedCornerShape(3.dp)),
        )
    }
    value("BottomSheetDefaults", BottomSheetDefaults)
    composableMember<BottomSheetDefaults>("DragHandle(modifier, width, height, shape, color)") { _, a ->
        androidx.compose.material3.BottomSheetDefaults.DragHandle(
            modifier = a.modifier(),
            width = a.dp("width", 32.dp),
            height = a.dp("height", 4.dp),
            shape = a.typed<Shape>("shape", androidx.compose.foundation.shape.RoundedCornerShape(50)),
            color = a.color("color", androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant),
        )
    }

    composable("contentColorFor(backgroundColor)") { a -> contentColorFor(a.color("backgroundColor")) }
    member<ColorScheme>("contentColorFor(backgroundColor)") { scheme, a -> scheme.contentColorFor(a.color("backgroundColor")) }
    member<ColorScheme>("surfaceColorAtElevation(elevation)") { scheme, a -> scheme.surfaceColorAtElevation(a.dp("elevation", 0.dp)) }
    value("LocalAbsoluteTonalElevation", LocalAbsoluteTonalElevation)
    // Dynamic colors come from the device wallpaper; previews use the baseline scheme, like Android Studio without one.
    function("dynamicLightColorScheme(context)") { _ -> lightColorScheme() }
    function("dynamicDarkColorScheme(context)") { _ -> darkColorScheme() }

    composable("FilledIconToggleButton(checked, onCheckedChange, modifier, enabled, shape, content)") { a ->
        FilledIconToggleButton(
            checked = a.boolean("checked", false),
            onCheckedChange = a.action1("onCheckedChange"),
            modifier = a.modifier(),
            enabled = a.boolean("enabled", true),
            shape = a.typed<Shape>("shape", IconButtonDefaults.filledShape),
            content = a.content(),
        )
    }

    member<Typography>(
        "copy(displayLarge, displayMedium, displaySmall, headlineLarge, headlineMedium, headlineSmall, titleLarge, " +
            "titleMedium, titleSmall, bodyLarge, bodyMedium, bodySmall, labelLarge, labelMedium, labelSmall)",
    ) { t, a ->
        t.copy(
            displayLarge = a.typed<TextStyle>("displayLarge", t.displayLarge),
            displayMedium = a.typed<TextStyle>("displayMedium", t.displayMedium),
            displaySmall = a.typed<TextStyle>("displaySmall", t.displaySmall),
            headlineLarge = a.typed<TextStyle>("headlineLarge", t.headlineLarge),
            headlineMedium = a.typed<TextStyle>("headlineMedium", t.headlineMedium),
            headlineSmall = a.typed<TextStyle>("headlineSmall", t.headlineSmall),
            titleLarge = a.typed<TextStyle>("titleLarge", t.titleLarge),
            titleMedium = a.typed<TextStyle>("titleMedium", t.titleMedium),
            titleSmall = a.typed<TextStyle>("titleSmall", t.titleSmall),
            bodyLarge = a.typed<TextStyle>("bodyLarge", t.bodyLarge),
            bodyMedium = a.typed<TextStyle>("bodyMedium", t.bodyMedium),
            bodySmall = a.typed<TextStyle>("bodySmall", t.bodySmall),
            labelLarge = a.typed<TextStyle>("labelLarge", t.labelLarge),
            labelMedium = a.typed<TextStyle>("labelMedium", t.labelMedium),
            labelSmall = a.typed<TextStyle>("labelSmall", t.labelSmall),
        )
    }
}

/** Material 3 pull to refresh: the box, its state, the default indicator and the modifier. */
@OptIn(ExperimentalMaterial3Api::class)
private fun Registry.pullToRefresh() {
    type<PullToRefreshState>("PullToRefreshState")
    composable("rememberPullToRefreshState()") { rememberPullToRefreshState() }
    composable("PullToRefreshBox(isRefreshing, onRefresh, modifier, state, contentAlignment, indicator, enabled, content)") { a ->
        val isRefreshing = a.boolean("isRefreshing", false)
        val state = a.typedOrNull<PullToRefreshState>("state") ?: rememberPullToRefreshState()
        val indicator = a.scopedContentOrNull<BoxScope>("indicator")
        val content = a.scopedContent<BoxScope>()
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = a.action("onRefresh"),
            modifier = a.modifier(),
            state = state,
            contentAlignment = a.typed("contentAlignment", Alignment.TopStart),
            indicator = {
                if (indicator != null) {
                    indicator()
                } else {
                    PullToRefreshDefaults.Indicator(
                        state = state,
                        isRefreshing = isRefreshing,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            },
            enabled = a.boolean("enabled", true),
        ) { content() }
    }
    value("PullToRefreshDefaults", PullToRefreshDefaults)
    composableMember<PullToRefreshDefaults>("Indicator(state, isRefreshing, modifier, containerColor, color)") { _, a ->
        val state = a.typedOrNull<PullToRefreshState>("state") ?: rememberPullToRefreshState()
        PullToRefreshDefaults.Indicator(
            state = state,
            isRefreshing = a.boolean("isRefreshing", false),
            modifier = a.modifier(),
            containerColor = a.color("containerColor", PullToRefreshDefaults.indicatorContainerColor),
            color = a.color("color", PullToRefreshDefaults.indicatorColor),
        )
    }
    member<Modifier>("pullToRefresh(isRefreshing, state, enabled, onRefresh)") { m, a ->
        val state = a.typedOrNull<PullToRefreshState>("state")
        if (state == null) m else m.pullToRefresh(a.boolean("isRefreshing", false), state, a.boolean("enabled", true), onRefresh = a.action("onRefresh"))
    }
}

/**
 * Markers registered for Material `*Defaults` objects whose initialization needs Skia (e.g. `SliderDefaults` builds a
 * `Path`), so installing the bindings doesn't load it. They have the same names for the binding check.
 */
private object RadioButtonDefaults
private object SwitchDefaults
private object SliderDefaults
private object TabRowDefaults
private object BottomSheetDefaults
