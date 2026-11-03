package ma7moud3ly.fast.compose.interpreter

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import kotlinx.coroutines.test.runTest
import ma7moud3ly.fast.compose.parser.Diagnostic
import ma7moud3ly.fast.compose.parser.KotlinParser
import ma7moud3ly.fast.compose.parser.ast.FunDecl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OrderedArgumentsTest {

    private fun interpreter(code: String, registry: Registry = Registry(), errors: MutableList<String> = mutableListOf()): Interpreter {
        val parsed = KotlinParser.parse(code)
        assertEquals(emptyList<Diagnostic>(), parsed.diagnostics)
        return Interpreter(parsed.program, registry, onError = { errors += it.message })
    }

    @Test
    fun positionalArgumentAfterNamedArgumentKeepsItsParameter() {
        val errors = mutableListOf<String>()
        val registry = Registry().apply {
            function("capture(a, b, c)") { a -> listOf(a["a"], a["b"], a["c"]) }
        }
        val result = interpreter("fun test() = capture(1, b = 2, 3)", registry, errors).call("test")
        assertEquals(listOf(1, 2, 3), result)
        assertEquals(emptyList(), errors)
    }

    @Test
    fun explicitNamedNullDoesNotConsumeTheFollowingPositionalArgument() {
        val errors = mutableListOf<String>()
        val registry = Registry().apply {
            function("Icon(imageVector, contentDescription, modifier, tint)") { a ->
                listOf(a["imageVector"], a.has("contentDescription"), a["contentDescription"], a["modifier"], a.has("tint"))
            }
        }
        val result = interpreter("fun test() = Icon(1, contentDescription = null, 3)", registry, errors).call("test")
        assertEquals(listOf(1, true, null, 3, false), result)
        assertEquals(emptyList(), errors)
    }

    @Test
    fun overloadSelectionUsesTheOriginalArgumentOrder() {
        val registry = Registry().apply {
            function("choose(a: Int, b: String, c: Int)") { "wrong" }
            function("choose(a: Int, b: Int, c: String)") { "right" }
        }
        assertEquals("right", interpreter("fun test() = choose(1, b = 2, \"three\")", registry).call("test"))
    }

    @Test
    fun invalidDuplicateIsStillReported() {
        val errors = mutableListOf<String>()
        val registry = Registry().apply {
            function("capture(a, b, c)") { a -> listOf(a["a"], a["b"], a["c"]) }
        }
        interpreter("fun test() = capture(1, 3, b = 2)", registry, errors).call("test")
        assertTrue(errors.any { "'b'" in it && "twice" in it }, errors.toString())
    }

    @Test
    fun duplicateNamedArgumentsAreReported() {
        val errors = mutableListOf<String>()
        val registry = Registry().apply {
            function("capture(a, b, c)") { a -> listOf(a["a"], a["b"], a["c"]) }
        }
        interpreter("fun test() = capture(1, b = 2, b = 3)", registry, errors).call("test")
        assertTrue(errors.any { "'b'" in it && "twice" in it }, errors.toString())
    }

    @Test
    fun positionalArgumentsCannotResumeAfterReorderedNamedArguments() {
        val errors = mutableListOf<String>()
        val registry = Registry().apply {
            function("capture(a, b, c)") { a -> listOf(a["a"], a["b"], a["c"]) }
        }
        interpreter("fun test() = capture(b = 2, 1, c = 3)", registry, errors).call("test")
        assertTrue(errors.isNotEmpty(), "A positional argument after reordered named arguments must be diagnosed")
    }

    @Test
    fun userFunctionsRejectDuplicateArguments() {
        val code = """
            fun capture(a: Int, b: Int, c: Int) = listOf(a, b, c)
            fun test() = capture(1, 3, b = 2)
        """.trimIndent()
        val error = assertFailsWith<InterpreterException> { interpreter(code).call("test") }
        assertTrue("'b'" in error.message.orEmpty() && "twice" in error.message.orEmpty(), error.message)
    }

    @Test
    fun userOverloadsRespectNamedArgumentPositions() {
        val code = """
            fun choose(a: Int, b: String, c: Int) = "wrong"
            fun choose(a: Int, c: Int, b: String) = "right"
            fun test() = choose(1, c = 2, "three")
        """.trimIndent()
        assertEquals("right", interpreter(code).call("test"))
    }

    @Test
    fun dataClassCopyKeepsMixedArgumentPositions() {
        val code = """
            data class Item(val a: Int, val b: Int, val c: Int)
            fun test(): List<Int> {
                val item = Item(0, 0, 0).copy(1, b = 2, 3)
                return listOf(item.a, item.b, item.c)
            }
        """.trimIndent()
        assertEquals(listOf(1, 2, 3), interpreter(code).call("test"))
    }

    @Test
    fun enumConstructorKeepsMixedArgumentPositionsAndEvaluationOrder() {
        val evaluated = mutableListOf<Any?>()
        val registry = Registry().apply {
            function("mark(value)") { a -> a["value"].also { evaluated += it } }
        }
        val code = """
            enum class Entry(val a: Int, val b: Int, val c: Int) {
                ONE(mark(1), b = mark(2), mark(3))
            }
            fun test() = listOf(Entry.ONE.a, Entry.ONE.b, Entry.ONE.c)
        """.trimIndent()
        assertEquals(listOf(1, 2, 3), interpreter(code, registry).call("test"))
        assertEquals(listOf<Any?>(1, 2, 3), evaluated)
    }

    @Test
    fun explicitNullAndOmittedDefaultsRemainDistinct() {
        val code = """
            fun capture(a: Int, b: String? = "default", c: Int = 4) = listOf(a, b, c)
            fun test() = listOf(capture(1, b = null, 3), capture(a = 2))
        """.trimIndent()
        assertEquals(listOf(listOf(1, null, 3), listOf(2, "default", 4)), interpreter(code).call("test"))
    }

    @Test
    fun positionalNullDoesNotUseADefault() {
        val code = """
            fun capture(value: String? = "default") = value
            fun test() = capture(null)
        """.trimIndent()
        assertEquals(null, interpreter(code).call("test"))
    }

    @Test
    fun positionalOverloadsStillRequireUnfilledParameters() {
        val candidates = listOf(
            "fun choose(a: Int = 0, b: Int) = \"wrong\"" to "1",
            "fun choose(required: Int, vararg values: Int) = \"wrong\"" to "",
            "fun choose(vararg values: Int, content: () -> Int) = \"wrong\"" to "",
        )
        for ((candidate, argument) in candidates) {
            val parameter = if (argument.isEmpty()) "" else "a: Int"
            val code = "$candidate\nfun choose($parameter) = \"right\"\nfun test() = choose($argument)"
            assertEquals("right", interpreter(code).call("test"), candidate)
        }
    }

    @Test
    fun trailingLambdaCannotReplaceAPositionalArgument() {
        val code = """
            fun capture(value: Int, content: () -> Int) = content()
            fun test() = capture(1, { 2 }) { 3 }
        """.trimIndent()
        assertFailsWith<InterpreterException> { interpreter(code).call("test") }
    }

    @Test
    fun varargsAndSpreadsFollowAnInPositionNamedArgument() {
        val errors = mutableListOf<String>()
        val registry = Registry().apply {
            function("capture(prefix, vararg values, suffix)") { a -> listOf(a["prefix"], a.varargs("values"), a["suffix"]) }
        }
        val code = "fun test() = capture(prefix = \"p\", 1, *listOf(2, 3), suffix = \"s\")"
        assertEquals(listOf("p", listOf(1, 2, 3), "s"), interpreter(code, registry, errors).call("test"))
        assertEquals(emptyList(), errors)
    }

    @Test
    fun varargsAndTrailingLambdaKeepTheirBindings() {
        val code = """
            fun capture(prefix: String, vararg values: Int, content: () -> Int) = listOf(prefix, values, content())
            fun test() = capture(prefix = "p", 1, *listOf(2, 3)) { 4 }
        """.trimIndent()
        assertEquals(listOf("p", listOf(1, 2, 3), 4), interpreter(code).call("test"))
    }

    @Test
    fun mixedArgumentsAreEvaluatedOnceInSourceOrder() {
        val evaluated = mutableListOf<Any?>()
        val registry = Registry().apply {
            function("mark(value)") { a -> a["value"].also { evaluated += it } }
            function("capture(a, b, c)") { a -> listOf(a["a"], a["b"], a["c"]) }
        }
        val result = interpreter("fun test() = capture(mark(1), b = mark(2), mark(3))", registry).call("test")
        assertEquals(listOf<Any?>(1, 2, 3), evaluated)
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun suspendCallsPreserveArgumentOrder() = runTest {
        val errors = mutableListOf<String>()
        val registry = Registry().apply {
            suspendFunction("capture(a, b, c)") { a -> listOf(a["a"], a["b"], a["c"]) }
        }
        val result = interpreter("suspend fun test() = capture(1, b = 2, 3)", registry, errors).callSuspend("test")
        assertEquals(listOf(1, 2, 3), result)
        assertEquals(emptyList(), errors)
    }

    @Test
    fun composableCallsPreserveArgumentOrder() = runTest {
        val errors = mutableListOf<String>()
        val captured = mutableListOf<List<Any?>>()
        val registry = Registry().apply {
            composable("Capture(a, b, c)") { a -> captured += listOf(a["a"], a["b"], a["c"]) }
        }
        val interpreter = interpreter("@Composable fun test() { Capture(1, b = 2, 3) }", registry, errors)
        val entry = interpreter.program.declarations.filterIsInstance<FunDecl>().single()
        val recomposer = Recomposer(coroutineContext)
        val composition = Composition(object : AbstractApplier<Unit>(Unit) {
            override fun insertTopDown(index: Int, instance: Unit) = Unit
            override fun insertBottomUp(index: Int, instance: Unit) = Unit
            override fun remove(index: Int, count: Int) = Unit
            override fun move(from: Int, to: Int, count: Int) = Unit
            override fun onClear() = Unit
        }, recomposer)
        try {
            composition.setContent { interpreter.Render(entry) }
            assertEquals(listOf(listOf<Any?>(1, 2, 3)), captured)
            assertEquals(emptyList(), errors)
        } finally {
            composition.dispose()
            recomposer.cancel()
        }
    }
}
