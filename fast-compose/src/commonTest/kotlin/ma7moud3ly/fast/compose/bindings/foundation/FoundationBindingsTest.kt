package ma7moud3ly.fast.compose.bindings.foundation

import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import ma7moud3ly.fast.compose.interpreter.Interpreter
import ma7moud3ly.fast.compose.interpreter.Registry
import ma7moud3ly.fast.compose.interpreter.RuntimeError
import ma7moud3ly.fast.compose.parser.Diagnostic
import ma7moud3ly.fast.compose.parser.KotlinParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FoundationBindingsTest {

    private val registry = Registry().apply { install(FoundationBindings) }

    private fun run(code: String): Any? {
        val result = KotlinParser.parse(code)
        assertEquals(emptyList<Diagnostic>(), result.diagnostics)
        return Interpreter(result.program, registry).call("test")
    }

    @Test
    fun annotatedStrings() {
        val code = """
            fun test(): String {
                val text = buildAnnotatedString {
                    append("Hi ")
                    withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                        append("there")
                    }
                }
                return text.text
            }
        """.trimIndent()
        assertEquals("Hi there", run(code))
    }

    @Test
    fun borderOverloads() {
        val problems = mutableListOf<RuntimeError>()
        val code = """
            fun test(): Boolean {
                val stroke = BorderStroke(1.dp, Color.Red)
                return (
                    Modifier.border(stroke, RoundedCornerShape(4.dp)) is Modifier &&
                        Modifier.border(2.dp, Color.Blue) is Modifier && stroke.width == 1.dp
                )
            }
        """.trimIndent()
        val program = KotlinParser.parse(code).program
        assertEquals(true, Interpreter(program, registry, onError = { problems += it }).call("test"))
        assertEquals(emptyList(), problems)
    }

    /** Runs `test()` and checks that no problem was reported while it ran. */
    private fun runClean(code: String): Any? {
        val problems = mutableListOf<RuntimeError>()
        val program = KotlinParser.parse(code.trimIndent()).program
        val result = Interpreter(program, registry, onError = { problems += it }).call("test")
        assertEquals(emptyList(), problems)
        return result
    }

    @Test
    fun stateFlows() {
        val code = """
            fun test(): Int {
                val state = MutableStateFlow(1)
                val readOnly = state.asStateFlow()
                state.update { it + 1 }
                return readOnly.value
            }
        """
        assertEquals(2, runClean(code))
    }

    @Test
    fun spacedByWithAlignment() {
        val code = "fun test() = Arrangement.spacedBy(4.dp, Alignment.CenterVertically) != Arrangement.spacedBy(4.dp)"
        assertEquals(true, runClean(code))
    }

    @Test
    fun strokeWithPathEffect() {
        val code = "fun test() = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 2f), 0f))"
        val stroke = runClean(code)
        assertTrue((stroke as Stroke).pathEffect != null)
    }

    @Test
    fun fontFamilyWithoutFontsIsTheDefault() {
        assertEquals(FontFamily.Default, runClean("fun test() = FontFamily()"))
    }

    @Test
    fun density() {
        val code = """
            fun test(): Float {
                val density = Density(2f, fontScale = 1.5f)
                val scaled = Density(density.density, fontScale = density.fontScale * 2)
                return scaled.density + scaled.fontScale
            }
        """.trimIndent()
        assertEquals(5f, run(code))
    }

    @Test
    fun combinedTransitions() {
        assertEquals(true, run("fun test() = (fadeIn() + expandIn()) is EnterTransition"))
    }

    @Test
    fun paddingOverloads() {
        assertEquals(true, run("fun test() = Modifier.padding(4.dp).padding(horizontal = 8.dp).padding(1.dp, 2.dp, 3.dp, 4.dp) is Modifier"))
    }

    @Test
    fun graphicsValues() {
        assertEquals(true, run("fun test() = ColorFilter.tint(Color.Red) is ColorFilter && ContentScale.Crop is ContentScale && BlendMode.Multiply is BlendMode"))
    }

    @Test
    fun constraints() {
        assertEquals(50, run("fun test() = Constraints(maxWidth = 100).copy(maxWidth = 50).maxWidth"))
    }
}
