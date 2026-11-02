package ma7moud3ly.fast.compose.bindings.foundation

import androidx.compose.ui.text.intl.Locale
import ma7moud3ly.fast.compose.interpreter.Registry
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** `java.lang.Integer`. */
internal object JavaInteger

/** Kotlin's `Long` companion and `java.lang.Long`: `Long.MAX_VALUE`, `parseLong`. */
internal object JavaLong

/** `android.os.Build`. */
internal object AndroidBuild

/** `android.os.Build.VERSION`. */
internal object AndroidBuildVersion

/** `android.os.Build.VERSION_CODES`. */
internal object AndroidVersionCodes

/** `kotlin.time.Clock`. */
internal object KotlinClock

/** `java.util.Locale` and Compose's `Locale`, which share the name. */
internal object Locales

/** API level previews report in `Build.VERSION.SDK_INT`: below 31 (S), so Material You dynamic colors stay off. */
internal const val PREVIEW_SDK_INT = 30

/**
 * Platform APIs that UI code reads: `Integer`, `Build.VERSION`, `Locale`, `kotlin.time.Clock`, Ktor's
 * `getTimeMillis()`, `kotlin.random.Random`, `Regex` and `StringBuilder`.
 */
@OptIn(ExperimentalTime::class)
internal fun Registry.platform() {
    value("Integer", JavaInteger)
    constants<JavaInteger>("MAX_VALUE" to Int.MAX_VALUE, "MIN_VALUE" to Int.MIN_VALUE)
    member<JavaInteger>("parseInt(s)") { _, a -> a.string("s").trim().toInt() }
    member<JavaInteger>("valueOf(s)") { _, a -> a["s"] as? Int ?: a.string("s").trim().toInt() }
    member<JavaInteger>("toString(i)") { _, a -> a.int("i", 0).toString() }
    member<JavaInteger>("toHexString(i)") { _, a -> a.int("i", 0).toUInt().toString(16) }
    value("Long", JavaLong)
    constants<JavaLong>("MAX_VALUE" to Long.MAX_VALUE, "MIN_VALUE" to Long.MIN_VALUE)
    member<JavaLong>("parseLong(s)") { _, a -> a.string("s").trim().toLong() }

    value("Build", AndroidBuild)
    constants<AndroidBuild>(
        "VERSION" to AndroidBuildVersion,
        "VERSION_CODES" to AndroidVersionCodes,
        "MANUFACTURER" to "Preview",
        "MODEL" to "Preview",
        "BRAND" to "Preview",
        "DEVICE" to "preview",
    )
    constants<AndroidBuildVersion>("SDK_INT" to PREVIEW_SDK_INT, "RELEASE" to "11")
    constants<AndroidVersionCodes>(
        "LOLLIPOP" to 21, "LOLLIPOP_MR1" to 22, "M" to 23, "N" to 24, "N_MR1" to 25, "O" to 26, "O_MR1" to 27,
        "P" to 28, "Q" to 29, "R" to 30, "S" to 31, "S_V2" to 32, "TIRAMISU" to 33, "UPSIDE_DOWN_CAKE" to 34,
        "VANILLA_ICE_CREAM" to 35, "BAKLAVA" to 36,
    )

    value("Locale", Locales)
    type<Locale>("Locale")
    member<Locales>("current") { _, _ -> Locale.current }
    member<Locales>("getDefault()") { _, _ -> Locale.current }
    function("Locale(languageTag)") { a -> Locale(a.string("languageTag", "en")) }
    constants<Locales>("ENGLISH" to Locale("en"), "US" to Locale("en-US"), "ROOT" to Locale("und"))
    properties<Locale>(
        "language" to { it.language },
        "region" to { it.region },
        "country" to { it.region },
        "script" to { it.script },
    )
    member<Locale>("toLanguageTag()") { locale, _ -> locale.toLanguageTag() }

    value("Clock", KotlinClock)
    constants<KotlinClock>("System" to Clock.System)
    member<Clock>("now()") { clock, _ -> clock.now() }
    type<Instant>("Instant")
    member<Instant>("toEpochMilliseconds()") { instant, _ -> instant.toEpochMilliseconds() }
    properties<Instant>("epochSeconds" to { it.epochSeconds }, "nanosecondsOfSecond" to { it.nanosecondsOfSecond })
    function("getTimeMillis()") { _ -> Clock.System.now().toEpochMilliseconds() }

    value("Random", Random)
    type<Random>("Random")
    function("Random(seed)") { a -> Random(a.long("seed", 0L)) }
    member<Random>("nextInt()") { random, _ -> random.nextInt() }
    member<Random>("nextInt(until)") { random, a -> random.nextInt(a.int("until", 1)) }
    member<Random>("nextInt(from, until)") { random, a -> random.nextInt(a.int("from", 0), a.int("until", 1)) }
    member<Random>("nextLong()") { random, _ -> random.nextLong() }
    member<Random>("nextFloat()") { random, _ -> random.nextFloat() }
    member<Random>("nextDouble()") { random, _ -> random.nextDouble() }
    member<Random>("nextBoolean()") { random, _ -> random.nextBoolean() }

    type<Regex>("Regex")
    function("Regex(pattern)") { a -> Regex(a.string("pattern")) }
    member<String>("toRegex()") { text, _ -> Regex(text) }
    member<Regex>("matches(input)") { regex, a -> regex.matches(a.string("input")) }
    member<Regex>("containsMatchIn(input)") { regex, a -> regex.containsMatchIn(a.string("input")) }
    member<Regex>("find(input, startIndex)") { regex, a -> regex.find(a.string("input"), a.int("startIndex", 0)) }
    member<Regex>("findAll(input, startIndex)") { regex, a -> regex.findAll(a.string("input"), a.int("startIndex", 0)).toList() }
    member<Regex>("replace(input, replacement)") { regex, a -> regex.replace(a.string("input"), a.string("replacement")) }
    member<Regex>("split(input, limit)") { regex, a -> regex.split(a.string("input"), a.int("limit", 0)) }
    properties<Regex>("pattern" to { it.pattern })
    type<MatchResult>("MatchResult")
    properties<MatchResult>("value" to { it.value }, "groupValues" to { it.groupValues }, "range" to { it.range })

    type<StringBuilder>("StringBuilder")
    function("StringBuilder(value)") { a -> StringBuilder(a.string("value")) }
    function("buildString(builderAction)") { a ->
        val action = a.scopedAction<StringBuilder>("builderAction")
        buildString { action() }
    }
    member<StringBuilder>("append(value)") { builder, a -> builder.append(a.string("value")) }
    member<StringBuilder>("appendLine(value)") { builder, a -> builder.appendLine(a.string("value")) }
    member<StringBuilder>("insert(index, value)") { builder, a -> builder.insert(a.int("index", 0), a.string("value")) }
    member<StringBuilder>("clear()") { builder, _ -> builder.clear() }
    member<StringBuilder>("toString()") { builder, _ -> builder.toString() }
    properties<StringBuilder>("length" to { it.length })
}
