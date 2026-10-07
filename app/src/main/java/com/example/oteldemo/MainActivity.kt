package com.example.oteldemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.oteldemo.logger.application.Logger
import com.example.oteldemo.ui.theme.OtelDemoTheme
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private val logger: Logger by lazy {
        (application as OtelApplication).loggerFactory.create("MainActivity")
    }
    private val tracer: Tracer by lazy {
        (application as OtelApplication).tracerFactory.get("MainActivity")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OtelDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DemoScreen(
                        onRoot = ::doRoot,
                        onWork = ::doWork,
                        onError = ::doError,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    private fun doRoot(setStatus: (String) -> Unit) {
        val span = tracer.spanBuilder("root").startSpan()
        try {
            logger.information("root hit", "endpoint" to "/")
            setStatus("root OK")
        } finally {
            span.end()
        }
    }

    private suspend fun doWork(setStatus: (String) -> Unit) {
        val span = tracer.spanBuilder("work").startSpan()
        try {
            val start = System.currentTimeMillis()
            withContext(Dispatchers.Default) { delay(50L + Random.nextLong(150)) }
            val duration = System.currentTimeMillis() - start
            span.setAttribute("work.duration_ms", duration)
            logger.information(
                "/work finished in {durationMs}ms",
                "endpoint" to "/work",
                "status" to 200,
                "durationMs" to duration,
            )
            setStatus("work OK (${duration}ms)")
        } finally {
            span.end()
        }
    }

    private fun doError(setStatus: (String) -> Unit) {
        val span = tracer.spanBuilder("error").startSpan()
        try {
            val boom = RuntimeException("boom")
            span.recordException(boom)
            span.setStatus(StatusCode.ERROR, "intentional failure")
            logger.error(
                "intentional failure at {endpoint}",
                "endpoint" to "/error",
                "status" to 500,
                error = boom,
            )
            setStatus("error emitted")
        } finally {
            span.end()
        }
    }
}

@Composable
fun DemoScreen(
    onRoot: ((String) -> Unit) -> Unit,
    onWork: suspend ((String) -> Unit) -> Unit,
    onError: ((String) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var status by remember { mutableStateOf("ready") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("OTel Demo — status: $status")
        Button(onClick = { onRoot { status = it } }) { Text("Root") }
        Button(onClick = { scope.launch { onWork { status = it } } }) { Text("Work") }
        Button(onClick = { onError { status = it } }) { Text("Error") }
    }
}
