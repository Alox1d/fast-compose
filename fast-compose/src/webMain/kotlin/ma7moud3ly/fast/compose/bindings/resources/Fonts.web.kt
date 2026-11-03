package ma7moud3ly.fast.compose.bindings.resources

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

internal actual fun platformFont(identity: String, bytes: ByteArray, weight: FontWeight, style: FontStyle): Font =
    androidx.compose.ui.text.platform.Font(identity, bytes, weight, style)
