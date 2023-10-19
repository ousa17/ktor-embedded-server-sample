package com.oussamabw.myapplicationktor

import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.oussamabw.myapplicationktor.ui.theme.MyApplicationKtorTheme
import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStarting
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.delay
import java.net.BindException

class MainActivity : ComponentActivity() {

    private var server: ApplicationEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationKtorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val (showWebView, setShowWebView) = remember { mutableStateOf(false) }

                    Column {
                        Button(
                            onClick = { startServer() }) {
                            Text(text = "start")
                        }



                        Button(
                            onClick = { setShowWebView(true) }) {
                            Text(text = "show webview")
                        }


                        if (showWebView) {
                            WebViewCompose(url = "http://127.0.0.1:5555")
                        }
                    }

                }
            }
        }
    }

    @Composable
    fun WebViewCompose(url: String) {
        Column {

            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = WebViewClient()
                        this.loadUrl(url)
                    }
                }
            )

            Button(
                onClick = { stop() }) {
                Text(text = "stop")
            }

            Button(
                onClick = { startServer() }) {
                Text(text = "re-start")
            }
        }
    }

    private fun startServer() {
        try {
            server = createServer()
            server?.start(wait = false)
        } catch (e: BindException) {
            Log.e("ousa", "${e.message}")
        }
    }


    fun stop() {
        server?.stop()
    }

    private fun createServer(): ApplicationEngine {
        return embeddedServer(factory = CIO, port = 5555) {
            environment.monitor.subscribe(ApplicationStarting) {
                Log.e("ousa", "Server is starting...")
            }
            environment.monitor.subscribe(ApplicationStarted) {
                Log.e("ousa", "Server has started!")
            }

            environment.monitor.subscribe(ApplicationStopping) {

                Log.e("ousa", "Server is stopping...")
            }
            environment.monitor.subscribe(ApplicationStopped) {
                Log.e("ousa", "Server has stopped!")
            }
            routing {
                get("/") {
                    call.respondText(
                        text = "ok",
                        contentType = ContentType.Text.Html
                    )
                    stop()
                }
            }
        }
    }
}