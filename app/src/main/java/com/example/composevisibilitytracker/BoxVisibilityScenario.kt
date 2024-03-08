package com.example.composevisibilitytracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun BoxVisibilityScenario(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Box(modifier = Modifier
                .size(100.dp)
                .background(randomColor())
                .onVisibilityChanged { event ->
                    println("onVisibilityChanged: first box! - $event")
                }) {
                Text("1", fontSize = 28.sp)
            }
            Box(modifier = Modifier
                .size(100.dp)
                .background(randomColor())
                .onVisibilityChanged { event ->
                    println("onVisibilityChanged: second box! - $event")
                }) {
                Text("2", fontSize = 28.sp)
            }
        }
    }
}

fun randomColor(alpha: Int = 255) = Color(
    Random.nextInt(256),
    Random.nextInt(256),
    Random.nextInt(256),
    alpha = alpha
)

@Preview
@Composable
private fun VisibleNormalScenarioPreview() {
    BoxVisibilityScenario()
}
