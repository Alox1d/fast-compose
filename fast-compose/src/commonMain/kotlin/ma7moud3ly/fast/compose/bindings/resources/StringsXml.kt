package ma7moud3ly.fast.compose.bindings.resources

/**
 * Reads the `<string name="…">` entries of an Android or Compose Multiplatform `strings.xml` file, and its
 * `<plurals>` and `<string-array>` entries under [pluralKey] and [arrayKey] names.
 */
public object StringsXml {

    private val entry = Regex("""<string\s+name\s*=\s*"([^"]+)"[^>]*?(?:/>|>([\s\S]*?)</string>)""")
    private val comment = Regex("""<!--[\s\S]*?-->""")
    private val cdata = Regex("""<!\[CDATA\[([\s\S]*?)]]>""")
    private val tag = Regex("""<[^>]+>""")
    private val plurals = Regex("""<plurals\s+name\s*=\s*"([^"]+)"[^>]*>([\s\S]*?)</plurals>""")
    private val pluralItem = Regex("""<item\s+quantity\s*=\s*"(\w+)"[^>]*>([\s\S]*?)</item>""")
    private val array = Regex("""<string-array\s+name\s*=\s*"([^"]+)"[^>]*>([\s\S]*?)</string-array>""")
    private val arrayItem = Regex("""<item[^>]*>([\s\S]*?)</item>""")

    /** Key of the [quantity] form (`one`, `other`, …) of plurals [name] in the map [parse] returns. */
    public fun pluralKey(name: String, quantity: String): String = "$name#$quantity"

    /** Key of item [index] of string array [name] in the map [parse] returns. */
    public fun arrayKey(name: String, index: Int): String = "$name[$index]"

    /** Names and values of the strings in [xml], with quotes, escapes and markup tags resolved. */
    public fun parse(xml: String): Map<String, String> {
        val text = comment.replace(xml, "")
        val strings = entry.findAll(text).associateTo(LinkedHashMap()) { match -> match.groupValues[1] to value(match.groupValues[2]) }
        for (match in plurals.findAll(text)) {
            for (item in pluralItem.findAll(match.groupValues[2])) {
                strings[pluralKey(match.groupValues[1], item.groupValues[1])] = value(item.groupValues[2])
            }
        }
        for (match in array.findAll(text)) {
            arrayItem.findAll(match.groupValues[2]).forEachIndexed { index, item ->
                strings[arrayKey(match.groupValues[1], index)] = value(item.groupValues[1])
            }
        }
        return strings
    }

    private fun value(raw: String): String {
        var text = cdata.replace(raw) { it.groupValues[1] }
        text = tag.replace(text, "").trim()
        if (text.length >= 2 && text.startsWith('"') && text.endsWith('"')) text = text.substring(1, text.length - 1)
        return unescape(text)
    }

    private fun unescape(text: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '\\' && i + 1 < text.length) {
                when (val next = text[i + 1]) {
                    'n' -> out.append('\n')
                    't' -> out.append('\t')
                    else -> out.append(next)
                }
                i += 2
            } else {
                out.append(c)
                i++
            }
        }
        return out.toString()
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
    }
}
