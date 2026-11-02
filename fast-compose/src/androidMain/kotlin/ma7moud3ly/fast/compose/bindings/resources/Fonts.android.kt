package ma7moud3ly.fast.compose.bindings.resources

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import java.io.File

/** Android loads fonts from files, so the bytes are written to a temporary file named after [identity]. */
internal actual fun platformFont(identity: String, bytes: ByteArray, weight: FontWeight, style: FontStyle): Font {
    val file = File(System.getProperty("java.io.tmpdir"), "fast-compose-font-${identity.hashCode().toUInt()}.ttf")
    if (!file.exists() || file.length() != bytes.size.toLong()) file.writeBytes(bytes)
    return Font(file, weight, style)
}
