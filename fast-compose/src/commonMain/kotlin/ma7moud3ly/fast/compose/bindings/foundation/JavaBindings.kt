package ma7moud3ly.fast.compose.bindings.foundation

import ma7moud3ly.fast.compose.interpreter.Args
import ma7moud3ly.fast.compose.interpreter.Registry
import ma7moud3ly.fast.compose.interpreter.StandIn
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

/** Value of a JVM system property such as `java.version`; null where there are none, e.g. on the web and iOS. */
internal expect fun systemProperty(key: String): String?

/** `java.lang.System`, which Kotlin code on the JVM uses without an import. */
internal object JavaSystem

/** `java.lang.Math`. */
internal object JavaMath

private val start = TimeSource.Monotonic.markNow()

/**
 * The parts of `java.lang.System` and `java.lang.Math` that platform code reads, e.g. in an `actual` that builds a
 * name from `System.getProperty("java.version")`. Other members of both give stand-ins.
 */
@OptIn(ExperimentalTime::class)
internal fun Registry.javaLang() {
    value("System", JavaSystem)
    member<JavaSystem>("getProperty(key)") { _, a -> systemProperty(a.string("key")) }
    member<JavaSystem>("getProperty(key, def)") { _, a -> systemProperty(a.string("key")) ?: a.stringOrNull("def") }
    member<JavaSystem>("currentTimeMillis()") { _, _ -> Clock.System.now().toEpochMilliseconds() }
    member<JavaSystem>("nanoTime()") { _, _ -> start.elapsedNow().inWholeNanoseconds }
    member<JavaSystem>("lineSeparator()") { _, _ -> "\n" }
    fallbackMember<JavaSystem> { _, name, a -> standIn("System.$name", a) }

    value("Math", JavaMath)
    constants<JavaMath>("PI" to PI, "E" to E)
    member<JavaMath>("abs(a)") { _, a ->
        when (val x = a["a"]) {
            is Int -> abs(x)
            is Long -> abs(x)
            is Float -> abs(x)
            else -> abs(a.double("a"))
        }
    }
    member<JavaMath>("max(a, b)") { _, a -> extreme(a, max = true) }
    member<JavaMath>("min(a, b)") { _, a -> extreme(a, max = false) }
    member<JavaMath>("round(a)") { _, a ->
        when (val x = a["a"]) {
            is Float -> x.roundToInt()
            else -> a.double("a").roundToLong()
        }
    }
    member<JavaMath>("sqrt(a)") { _, a -> sqrt(a.double("a")) }
    member<JavaMath>("pow(a, b)") { _, a -> a.double("a").pow(a.double("b")) }
    member<JavaMath>("floor(a)") { _, a -> floor(a.double("a")) }
    member<JavaMath>("ceil(a)") { _, a -> ceil(a.double("a")) }
    member<JavaMath>("sin(a)") { _, a -> sin(a.double("a")) }
    member<JavaMath>("cos(a)") { _, a -> cos(a.double("a")) }
    member<JavaMath>("tan(a)") { _, a -> tan(a.double("a")) }
    member<JavaMath>("atan2(y, x)") { _, a -> atan2(a.double("y"), a.double("x")) }
    member<JavaMath>("hypot(x, y)") { _, a -> hypot(a.double("x"), a.double("y")) }
    member<JavaMath>("log(a)") { _, a -> ln(a.double("a")) }
    member<JavaMath>("log10(a)") { _, a -> log10(a.double("a")) }
    member<JavaMath>("exp(a)") { _, a -> exp(a.double("a")) }
    member<JavaMath>("toRadians(angdeg)") { _, a -> a.double("angdeg") * PI / 180 }
    member<JavaMath>("toDegrees(angrad)") { _, a -> a.double("angrad") * 180 / PI }
    member<JavaMath>("random()") { _, _ -> Random.nextDouble() }
    fallbackMember<JavaMath> { _, name, a -> standIn("Math.$name", a) }
}

private fun Args.double(name: String): Double = typed<Number>(name, 0.0).toDouble()

/** `Math.max` or `Math.min`, keeping the type of the arguments: Int, Long, Float, or else Double. */
private fun extreme(a: Args, max: Boolean): Number {
    val x = a["a"]
    val y = a["b"]
    return when {
        x is Int && y is Int -> if (max) maxOf(x, y) else minOf(x, y)
        x is Long && y is Long -> if (max) maxOf(x, y) else minOf(x, y)
        x is Float && y is Float -> if (max) maxOf(x, y) else minOf(x, y)
        else -> if (max) maxOf(a.double("a"), a.double("b")) else minOf(a.double("a"), a.double("b"))
    }
}

private fun standIn(name: String, a: Args): StandIn {
    a.warn("'$name' has no binding; a stand-in is used, so the preview may differ")
    return StandIn(name)
}
