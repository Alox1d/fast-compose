package ma7moud3ly.fast.compose.bindings.resources

/** Reads the `<color name="…">` entries of an Android `colors.xml` file. */
public object ColorsXml {

    private val entry = Regex("""<color\s+name\s*=\s*"([^"]+)"[^>]*>([\s\S]*?)</color>""")
    private val comment = Regex("""<!--[\s\S]*?-->""")

    /** Names and raw values of the colors in [xml], e.g. `#FF6200EE` or a reference such as `@color/purple`. */
    public fun parse(xml: String): Map<String, String> =
        entry.findAll(comment.replace(xml, "")).associate { match -> match.groupValues[1] to match.groupValues[2].trim() }
}

/** Android's own colors, used as `@android:color/…`, as `#AARRGGBB`. */
private val frameworkColors = mapOf(
    "black" to "#FF000000",
    "white" to "#FFFFFFFF",
    "transparent" to "#00000000",
    "darker_gray" to "#FFAAAAAA",
    "background_dark" to "#FF000000",
    "background_light" to "#FFFFFFFF",
    "holo_blue_light" to "#FF33B5E5",
    "holo_blue_dark" to "#FF0099CC",
    "holo_blue_bright" to "#FF00DDFF",
    "holo_green_light" to "#FF99CC00",
    "holo_green_dark" to "#FF669900",
    "holo_red_light" to "#FFFF4444",
    "holo_red_dark" to "#FFCC0000",
    "holo_orange_light" to "#FFFFBB33",
    "holo_orange_dark" to "#FFFF8800",
    "holo_purple" to "#FFAA66CC",
)

/** Black, used for colors that can't be known here, e.g. theme attributes such as `?attr/colorControlNormal`. */
private const val UNKNOWN_COLOR = "#FF000000"

/**
 * The literal color (`#…`) that a color value stands for: itself, an `@android:color/…` framework color, an
 * `@color/…` entry of [colors] (following references), or black for a theme attribute (`?attr/…`), whose value depends
 * on the app theme. Null for a `@color/…` that [colors] does not have.
 */
internal fun resolveColor(value: String, colors: Map<String, String>, depth: Int = 0): String? = when {
    value.startsWith("#") -> value
    value.startsWith("@android:color/") -> frameworkColors[value.substringAfter('/')] ?: UNKNOWN_COLOR
    value.startsWith("@color/") -> colors[value.substringAfter('/')]?.let { if (depth < 8) resolveColor(it, colors, depth + 1) else null }
    value.startsWith("?") -> UNKNOWN_COLOR
    else -> null
}

private val colorReference = Regex(""""((?:@android:color/|@color/|\?(?:android:)?attr/)[A-Za-z0-9_.]+)"""")

/**
 * [xml] of a vector drawable with color references replaced by literal colors, which the vector decoder needs. A
 * reference that can't be resolved becomes black, like an untinted icon.
 */
internal fun resolveColorReferences(xml: String, colors: Map<String, String>): String =
    colorReference.replace(xml) { match -> "\"" + (resolveColor(match.groupValues[1], colors) ?: UNKNOWN_COLOR) + "\"" }
