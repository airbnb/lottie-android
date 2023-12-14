import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.airbnb.lottie.sample.compose.App

fun main() {
    application {
        val windowState = rememberWindowState()

        Window(
            onCloseRequest = ::exitApplication,
            title = "Compose Multiplatform example",
            state = windowState,
        ) {
           App()
        }
    }
}
