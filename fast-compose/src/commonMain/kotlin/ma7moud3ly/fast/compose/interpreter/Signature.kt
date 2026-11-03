package ma7moud3ly.fast.compose.interpreter

/**
 * Parameters of a binding, written like a Kotlin call, e.g. `"Text(text, modifier, color, fontSize)"`.
 *
 * - Positional arguments fill parameters in order.
 * - A trailing lambda fills the last parameter before `*`.
 * - `vararg name` collects the remaining positional arguments as a list, e.g. `"LaunchedEffect(vararg keys, block)"`.
 * - Parameters after `*` can only be passed by name, e.g. `"Text(text, modifier, *, fontWeight)"`.
 * - `name: Type` restricts a parameter to a type, used to choose between overloads with the same shape,
 *   e.g. `"verticalGradient(colors: List)"` and `"verticalGradient(vararg colorStops: Pair)"`.
 * - A name without parentheses, e.g. `"value"`, takes no arguments.
 */
public class Signature private constructor(
    public val name: String,
    public val params: List<String>,
    public val namedOnly: List<String>,
    public val varargParam: String?,
    public val types: Map<String, String>,
    private val lenient: Boolean,
) {
    /** Arguments of one call matched to parameter names, and problems such as unknown names. */
    public class Match(public val values: Map<String, Any?>, public val problems: List<String>)

    /** Every parameter name, for quick checks of named arguments. */
    private val known: Set<String> = (params + namedOnly).toHashSet()

    /** Matches call arguments to parameter names. */
    public fun match(positional: List<Any?>, named: Map<String, Any?>, trailing: Any?): Match {
        val values = LinkedHashMap<String, Any?>()
        if (lenient) {
            values.putAll(named)
            return Match(values, emptyList())
        }
        val problems = ArrayList<String>(0)
        var next = 0
        for (param in params) {
            if (param == varargParam) {
                values[param] = positional.drop(next)
                next = positional.size
            } else if (next < positional.size) {
                values[param] = positional[next++]
            }
        }
        if (next < positional.size) problems += "Too many arguments for $name"
        for ((argName, value) in named) {
            when (argName) {
                !in known -> problems += unknownParameter(argName)
                in values if argName != varargParam -> problems += "Parameter '$argName' of $name is passed twice"
                else -> values[argName] = value
            }
        }
        if (trailing != null) {
            when (val last = params.lastOrNull()) {
                null, varargParam -> problems += "$name does not take a trailing lambda"
                in values -> problems += "Too many arguments for $name"
                else -> values[last] = trailing
            }
        }
        return Match(values, problems)
    }

    /** Matches evaluated source arguments without losing the positions of named arguments. */
    internal fun match(call: CallShape): Match {
        val arguments = call.arguments
        if (arguments == null || lenient) return match(call.positional, call.named, call.trailing)
        val values = LinkedHashMap<String, Any?>()
        val problems = mutableListOf<String>()
        val varargs = mutableListOf<Any?>()
        var next = 0
        var positionalAllowed = true
        for ((argName, value) in arguments) {
            if (argName != null) {
                when {
                    argName !in known -> problems += unknownParameter(argName)
                    argName in values -> problems += "Parameter '$argName' of $name is passed twice"
                    else -> values[argName] = value
                }
                if (argName == params.getOrNull(next)) {
                    next++
                } else {
                    positionalAllowed = false
                }
                // Parameters after a vararg must be named (or supplied as a trailing lambda).
                if (argName == varargParam) positionalAllowed = false
            } else if (!positionalAllowed) {
                problems += "Positional argument is not allowed here for $name"
            } else {
                val param = params.getOrNull(next)
                when {
                    param == null -> problems += "Too many arguments for $name"
                    param == varargParam -> {
                        varargs += value
                        values[param] = varargs
                    }
                    else -> {
                        values[param] = value
                        next++
                    }
                }
            }
        }
        if (varargParam != null && varargParam !in values) values[varargParam] = emptyList<Any?>()
        if (call.trailing != null) {
            when (val last = params.lastOrNull()) {
                null, varargParam -> problems += "$name does not take a trailing lambda"
                in values -> problems += "Too many arguments for $name"
                else -> values[last] = call.trailing
            }
        }
        return Match(values, problems)
    }

    /**
     * True when [call] matches without problems and its arguments fit the parameter [types], checked with [isA];
     * used to choose between overloads.
     */
    public fun accepts(call: CallShape, isA: (value: Any?, type: String) -> Boolean = { _, _ -> true }): Boolean {
        if (lenient) return true
        val match = match(call)
        if (match.problems.isNotEmpty()) return false
        val values = match.values
        return types.all { (param, type) ->
            if (param !in values) return@all true
            val value = values[param]
            if (param == varargParam) (value as? List<*>)?.all { isA(it, type) } == true else isA(value, type)
        }
    }

    private fun unknownParameter(argName: String): String {
        val suggestion = (params + namedOnly).filter { editDistance(it, argName) <= 2 }.minByOrNull { editDistance(it, argName) }
        return "Unknown parameter '$argName' for $name" + (suggestion?.let { ". Did you mean '$it'?" } ?: "")
    }

    override fun toString(): String {
        if (params.isEmpty() && namedOnly.isEmpty()) return name
        fun describe(param: String) = (if (param == varargParam) "vararg " else "") + param + (types[param]?.let { ": $it" } ?: "")
        val all = params.map(::describe) + (if (namedOnly.isEmpty()) emptyList() else listOf("*") + namedOnly.map(::describe))
        return "$name(${all.joinToString(", ")})"
    }

    public companion object {
        internal fun forParameters(name: String, params: List<String>, varargParam: String?): Signature =
            Signature(name, params, emptyList(), varargParam, emptyMap(), lenient = false)

        /** Parses a signature like `"Button(onClick, modifier, enabled, *, shape, content)"`. */
        public fun parse(text: String): Signature {
            val open = text.indexOf('(')
            if (open < 0) return Signature(text.trim(), emptyList(), emptyList(), null, emptyMap(), lenient = false)
            require(text.trimEnd().endsWith(")")) { "Invalid signature '$text'" }
            val name = text.substring(0, open).trim()
            val params = mutableListOf<String>()
            val namedOnly = mutableListOf<String>()
            val types = mutableMapOf<String, String>()
            var varargParam: String? = null
            var namedSection = false
            for (part in text.substring(open + 1, text.trimEnd().length - 1).split(',').map { it.trim() }.filter { it.isNotEmpty() }) {
                if (part == "*") {
                    namedSection = true
                    continue
                }
                val isVararg = part.startsWith("vararg ")
                val declaration = part.removePrefix("vararg ")
                val param = declaration.substringBefore(':').trim()
                if (':' in declaration) types[param] = declaration.substringAfter(':').trim()
                when {
                    namedSection -> namedOnly += param
                    isVararg -> {
                        varargParam = param
                        params += param
                    }
                    else -> params += param
                }
            }
            return Signature(name, params, namedOnly, varargParam, types, lenient = false)
        }

        /** Accepts any arguments without checking them; used for fallback members. */
        public fun lenient(name: String): Signature = Signature(name, emptyList(), emptyList(), null, emptyMap(), lenient = true)
    }
}

/** An evaluated source argument; a null [name] denotes a positional argument. */
internal data class EvaluatedArgument(val name: String?, val value: Any?)

/** Arguments of a call, used to choose between overloads before they are matched. */
public class CallShape private constructor(
    public val positional: List<Any?>,
    public val named: Map<String, Any?>,
    public val trailing: Any?,
    internal val arguments: List<EvaluatedArgument>?,
) {
    public constructor(positional: List<Any?>, named: Map<String, Any?>, trailing: Any?) : this(positional, named, trailing, null)

    internal constructor(arguments: List<EvaluatedArgument>, trailing: Any?) : this(
        arguments.filter { it.name == null }.map { it.value },
        buildMap { for (argument in arguments) argument.name?.let { put(it, argument.value) } },
        trailing,
        arguments,
    )

    public val hasTrailing: Boolean get() = trailing != null

    public companion object {
        public val None: CallShape = CallShape(emptyList(), emptyMap(), null)
    }
}

/** Levenshtein distance ignoring case, for "did you mean" suggestions. */
internal fun editDistance(a: String, b: String): Int {
    val dp = IntArray(b.length + 1) { it }
    for (i in 1..a.length) {
        var prev = dp[0]
        dp[0] = i
        for (j in 1..b.length) {
            val temp = dp[j]
            dp[j] = minOf(dp[j] + 1, dp[j - 1] + 1, prev + if (a[i - 1].equals(b[j - 1], ignoreCase = true)) 0 else 1)
            prev = temp
        }
    }
    return dp[b.length]
}
