package com.beingniloy.lively.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beingniloy.lively.R
import com.beingniloy.lively.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit
) {
    val colors = LocalAppColors.current
    var startAnimation by remember { mutableStateOf(false) }
    
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "SplashFade"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer(alpha = alphaAnim)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "Lively Wallpaper Logo",
                modifier = Modifier.size(110.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Lively Wallpaper",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (colors.isDark) Color.White else Color.Black,
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Personalize Your Device Screen",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textMuted,
                    letterSpacing = 0.2.sp
                )
            )
        }
    }
}
