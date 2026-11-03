package ma7moud3ly.fast.compose.bindings.foundation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ma7moud3ly.fast.compose.interpreter.Args
import ma7moud3ly.fast.compose.interpreter.Registry

/** Size of the preview frame, which previews use as the window size; null outside a preview. */
public val LocalPreviewWindowSize: ProvidableCompositionLocal<DpSize?> = staticCompositionLocalOf { null }

/**
 * A window size class for previews, imitating material3-adaptive's `WindowSizeClass` (androidx.window) and Material 3's
 * `WindowSizeClass`, which aren't part of the engine: `windowWidthSizeClass`, `widthSizeClass`,
 * `isWidthAtLeastBreakpoint`, and the same for heights.
 */
internal class PreviewWindowSizeClass(val widthDp: Float, val heightDp: Float) {
    val width: SizeClass = when {
        widthDp >= 840 -> SizeClass.Expanded
        widthDp >= 600 -> SizeClass.Medium
        else -> SizeClass.Compact
    }
    val height: SizeClass = when {
        heightDp >= 900 -> SizeClass.Expanded
        heightDp >= 480 -> SizeClass.Medium
        else -> SizeClass.Compact
    }
}

/** `COMPACT` / `Compact`, `MEDIUM` / `Medium` and `EXPANDED` / `Expanded` of both window size class libraries. */
internal enum class SizeClass { Compact, Medium, Expanded }

/** `currentWindowAdaptiveInfo()` in previews: only the window size class. */
internal class PreviewAdaptiveInfo(val windowSizeClass: PreviewWindowSizeClass)

/** `WindowWidthSizeClass`, `WindowHeightSizeClass` and `WindowSizeClass` (for its breakpoint constants). */
private object SizeClasses
private object WindowSizeClasses

/**
 * `LocalLifecycleOwner` in previews: a lifecycle that is always resumed. The lifecycle library isn't part of the
 * engine, so these imitate `LifecycleOwner`, `Lifecycle`, `Lifecycle.State` and `Lifecycle.Event`.
 */
internal object PreviewLifecycleOwner
internal object PreviewLifecycle
internal enum class LifecycleState { DESTROYED, INITIALIZED, CREATED, STARTED, RESUMED }
internal enum class LifecycleEvent { ON_CREATE, ON_START, ON_RESUME, ON_PAUSE, ON_STOP, ON_DESTROY, ON_ANY }
private object LifecycleNames
private object LifecycleStates
private object LifecycleEvents

/** Scope of `LifecycleResumeEffect` and `LifecycleStartEffect`, whose `onPauseOrDispose` / `onStopOrDispose` run on dispose. */
internal object PreviewLifecycleEffectScope
internal class PreviewEffectResult(val onDispose: () -> Unit)

/**
 * Window size classes from the preview frame's size, an always resumed lifecycle, and lazy staggered grids: APIs of
 * material3-adaptive and lifecycle-runtime-compose imitated without those libraries, and foundation's staggered grid.
 */
internal fun Registry.adaptiveExtras() {
    windowSizeClasses()
    lifecycle()
    staggeredGrids()
}

private fun Registry.windowSizeClasses() {
    composable("currentWindowAdaptiveInfo()") { PreviewAdaptiveInfo(previewSizeClass()) }
    composable("calculateWindowSizeClass(activity)") { previewSizeClass() }
    properties<PreviewAdaptiveInfo>("windowSizeClass" to { it.windowSizeClass })
    properties<PreviewWindowSizeClass>(
        "windowWidthSizeClass" to { it.width },
        "windowHeightSizeClass" to { it.height },
        "widthSizeClass" to { it.width },
        "heightSizeClass" to { it.height },
        "minWidthDp" to { it.widthDp.toInt() },
        "minHeightDp" to { it.heightDp.toInt() },
    )
    member<PreviewWindowSizeClass>("isWidthAtLeastBreakpoint(widthDpBreakpoint)") { size, a -> size.widthDp >= a.int("widthDpBreakpoint", 0) }
    member<PreviewWindowSizeClass>("isHeightAtLeastBreakpoint(heightDpBreakpoint)") { size, a -> size.heightDp >= a.int("heightDpBreakpoint", 0) }
    for (name in listOf("WindowWidthSizeClass", "WindowHeightSizeClass")) value(name, SizeClasses)
    constants<SizeClasses>(
        "COMPACT" to SizeClass.Compact, "MEDIUM" to SizeClass.Medium, "EXPANDED" to SizeClass.Expanded,
        "Compact" to SizeClass.Compact, "Medium" to SizeClass.Medium, "Expanded" to SizeClass.Expanded,
    )
    value("WindowSizeClass", WindowSizeClasses)
    constants<WindowSizeClasses>(
        "WIDTH_DP_MEDIUM_LOWER_BOUND" to 600, "WIDTH_DP_EXPANDED_LOWER_BOUND" to 840,
        "HEIGHT_DP_MEDIUM_LOWER_BOUND" to 480, "HEIGHT_DP_EXPANDED_LOWER_BOUND" to 900,
    )
}

/** Size class of the preview frame, or of the window outside a preview. */
@Composable
private fun previewSizeClass(): PreviewWindowSizeClass {
    val size = LocalPreviewWindowSize.current ?: with(LocalDensity.current) {
        val window = LocalWindowInfo.current.containerSize
        DpSize(window.width.toDp(), window.height.toDp())
    }
    return PreviewWindowSizeClass(size.width.value, size.height.value)
}

private fun Registry.lifecycle() {
    value("LocalLifecycleOwner", PreviewLifecycleOwner)
    composableMember<PreviewLifecycleOwner>("current") { owner, _ -> owner }
    properties<PreviewLifecycleOwner>("lifecycle" to { PreviewLifecycle })
    properties<PreviewLifecycle>("currentState" to { LifecycleState.RESUMED })
    // Observers would only ever see the resumed state, so they are not called.
    member<PreviewLifecycle>("addObserver(observer)") { _, _ -> Unit }
    member<PreviewLifecycle>("removeObserver(observer)") { _, _ -> Unit }
    member<LifecycleState>("isAtLeast(state)") { state, a ->
        state >= (a["state"] as? LifecycleState ?: LifecycleState.RESUMED)
    }
    value("Lifecycle", LifecycleNames)
    constants<LifecycleNames>("State" to LifecycleStates, "Event" to LifecycleEvents)
    constants<LifecycleStates>(*LifecycleState.entries.map { it.name to it }.toTypedArray())
    constants<LifecycleEvents>(*LifecycleEvent.entries.map { it.name to it }.toTypedArray())

    composable("LifecycleResumeEffect(vararg keys, effects)") { a -> lifecycleEffect(a) }
    composable("LifecycleStartEffect(vararg keys, effects)") { a -> lifecycleEffect(a) }
    member<PreviewLifecycleEffectScope>("onPauseOrDispose(onPauseOrDisposeEffect)") { _, a ->
        PreviewEffectResult(a.action("onPauseOrDisposeEffect"))
    }
    member<PreviewLifecycleEffectScope>("onStopOrDispose(onStopOrDisposeEffect)") { _, a ->
        PreviewEffectResult(a.action("onStopOrDisposeEffect"))
    }
    // The preview lifecycle reaches CREATED, STARTED and RESUMED once, so only those events are delivered.
    composable("LifecycleEventEffect(event, onEvent)") { a ->
        val event = a["event"] as? LifecycleEvent
        val onEvent = a.action("onEvent")
        LaunchedEffect(event) {
            if (event == LifecycleEvent.ON_CREATE || event == LifecycleEvent.ON_START || event == LifecycleEvent.ON_RESUME) onEvent()
        }
    }
}

/** Runs the effect of `LifecycleResumeEffect` or `LifecycleStartEffect` once, since previews are always resumed. */
@Composable
private fun lifecycleEffect(a: Args) {
    val effects = a.scopedCalculation<PreviewLifecycleEffectScope>("effects")
    DisposableEffect(*a.varargs("keys").toTypedArray()) {
        val result = PreviewLifecycleEffectScope.effects() as? PreviewEffectResult
        onDispose { result?.onDispose?.invoke() }
    }
}

private fun Registry.staggeredGrids() {
    type<LazyStaggeredGridScope>("LazyStaggeredGridScope")
    type<LazyStaggeredGridState>("LazyStaggeredGridState")
    value("StaggeredGridCells", StaggeredCells)
    member<StaggeredCells>("Fixed(count)") { _, a -> StaggeredGridCells.Fixed(a.int("count", 1).coerceAtLeast(1)) }
    member<StaggeredCells>("Adaptive(minSize)") { _, a -> StaggeredGridCells.Adaptive(a.dp("minSize", 100.dp)) }
    member<StaggeredCells>("FixedSize(size)") { _, a -> StaggeredGridCells.FixedSize(a.dp("size", 100.dp)) }
    value("StaggeredGridItemSpan", StaggeredSpans)
    constants<StaggeredSpans>("FullLine" to StaggeredGridItemSpan.FullLine, "SingleLane" to StaggeredGridItemSpan.SingleLane)
    composable("rememberLazyStaggeredGridState(initialFirstVisibleItemIndex, initialFirstVisibleItemScrollOffset)") { a ->
        rememberLazyStaggeredGridState(a.int("initialFirstVisibleItemIndex", 0), a.int("initialFirstVisibleItemScrollOffset", 0))
    }
    composable("LazyVerticalStaggeredGrid(columns, modifier, state, contentPadding, reverseLayout, verticalItemSpacing, horizontalArrangement, userScrollEnabled, content)") { a ->
        val content = a.scopedAction<LazyStaggeredGridScope>()
        LazyVerticalStaggeredGrid(
            columns = a.typed<StaggeredGridCells>("columns", StaggeredGridCells.Fixed(2)),
            modifier = a.modifier(),
            state = a.typed("state", rememberLazyStaggeredGridState()),
            contentPadding = a.typed<PaddingValues>("contentPadding", PaddingValues(0.dp)),
            reverseLayout = a.boolean("reverseLayout", false),
            verticalItemSpacing = a.dp("verticalItemSpacing", 0.dp),
            horizontalArrangement = a.typed<Arrangement.Horizontal>("horizontalArrangement", Arrangement.spacedBy(0.dp)),
            userScrollEnabled = a.boolean("userScrollEnabled", true),
        ) { content() }
    }
    member<LazyStaggeredGridScope>("item(key, contentType, span, content)") { scope, a ->
        val content = a.scopedContent<LazyStaggeredGridItemScope>()
        scope.item(key = a["key"]?.let(::lazyKey), contentType = a["contentType"], span = a.typedOrNull<StaggeredGridItemSpan>("span")) {
            content()
        }
    }
    member<LazyStaggeredGridScope>("items(items, key, contentType, span, itemContent, *, count)") { scope, a ->
        val content = a.scopedContent1<LazyStaggeredGridItemScope>("itemContent")
        val list = a["items"] as? List<*>
        val count = list?.size ?: a["items"] as? Int ?: a["count"] as? Int
        if (count == null) {
            if (!a.isStandIn("items") && !a.isStandIn("count")) a.report("items needs a count or a list")
        } else {
            val span = a.itemFunction("span", list)
            scope.items(
                count = count,
                key = a.itemFunction("key", list)?.keys(),
                contentType = a.itemFunction("contentType", list) ?: { null },
                span = span?.let { fn -> { index -> fn(index) as? StaggeredGridItemSpan ?: StaggeredGridItemSpan.SingleLane } },
            ) { content(if (list != null) list[it] else it) }
        }
    }
}

/** `StaggeredGridCells.Fixed`, `Adaptive` and `FixedSize`. */
private object StaggeredCells

/** `StaggeredGridItemSpan.FullLine` and `SingleLane`. */
private object StaggeredSpans
