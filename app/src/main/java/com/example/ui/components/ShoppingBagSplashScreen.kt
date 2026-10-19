package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PastelBackground
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelRoseAccent
import com.example.ui.theme.PastelRoseCard
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class FallingProductItem(
  val id: Int,
  val icon: ImageVector,
  val tint: Color,
  val bg: Color,
  val startXOffset: Float,
  val rotation: Float
)

/**
 * Fast, elegant 1.5-second Boutique Shopping Bag startup animation.
 * Products gracefully dive into the bag, which catches them with a charming bounce,
 * then smoothly transitions directly into the app. No skip button, strictly 1.5 seconds.
 */
@Composable
fun ShoppingBagSplashScreen(
  onAnimationFinished: () -> Unit,
  modifier: Modifier = Modifier
) {
  val products = remember {
    listOf(
      FallingProductItem(0, Icons.Default.Diamond, PastelSkyAccent, Color(0xFFE0F2FE), -28f, -10f),
      FallingProductItem(1, Icons.Default.AutoAwesome, Color(0xFFD97706), Color(0xFFFEF3C7), 24f, 12f),
      FallingProductItem(2, Icons.Default.Favorite, PastelRoseAccent, Color(0xFFFCE7F3), -6f, 5f)
    )
  }

  // Bag bounce scale & rotation
  val bagScale = remember { Animatable(0.6f) }
  val bagRotation = remember { Animatable(0f) }
  val bagAlpha = remember { Animatable(0f) }

  // Item falling animatables
  val itemProgress = remember { List(products.size) { Animatable(0f) } }
  val itemAlphas = remember { List(products.size) { Animatable(0f) } }

  // Sparkles burst animatable
  val sparklesAlpha = remember { Animatable(0f) }
  val sparklesScale = remember { Animatable(0.6f) }

  // Subtitle / text fade in
  val textAlpha = remember { Animatable(0f) }

  // Overall screen fade out at the very end
  val screenAlpha = remember { Animatable(1f) }

  // Subtle ambient breathing for background
  val infiniteTransition = rememberInfiniteTransition(label = "ambient")
  val ambientGlow by infiniteTransition.animateFloat(
    initialValue = 0.92f,
    targetValue = 1.04f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow"
  )

  // Timeline runs for exactly 1500 ms (1.5 seconds)
  LaunchedEffect(Unit) {
    // 0ms - 220ms: Bag pops in quickly with soft spring
    launch {
      bagAlpha.animateTo(1f, tween(180))
    }
    launch {
      bagScale.animateTo(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
      )
    }
    launch {
      textAlpha.animateTo(1f, tween(250))
    }

    // Drop items in quick, charming succession
    // Item 0: starts at 180ms, finishes at 460ms (280ms duration)
    // Item 1: starts at 360ms, finishes at 640ms (280ms duration)
    // Item 2: starts at 540ms, finishes at 820ms (280ms duration)
    products.forEachIndexed { index, _ ->
      delay(if (index == 0) 180 else 160)

      launch {
        itemAlphas[index].animateTo(1f, tween(90))
        itemProgress[index].animateTo(1f, tween(260, easing = FastOutSlowInEasing))
        itemAlphas[index].animateTo(0f, tween(70))
      }

      // Quick squish on impact
      delay(200)
      launch {
        bagScale.animateTo(1.10f, tween(60))
        bagScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
      }
      launch {
        bagRotation.animateTo(if (index % 2 == 0) 4f else -4f, tween(60))
        bagRotation.animateTo(0f, tween(80))
      }
    }

    // 850ms - 1050ms: Sparkles burst and happy final wiggle
    launch {
      sparklesScale.snapTo(0.5f)
      sparklesAlpha.snapTo(1f)
      sparklesScale.animateTo(1.25f, tween(220))
      sparklesAlpha.animateTo(0f, tween(180))
    }
    launch {
      bagScale.animateTo(1.08f, tween(110))
      bagScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
    }

    // 1280ms - 1500ms: Smooth fade-out to main screen
    delay(280)
    screenAlpha.animateTo(0f, tween(200, easing = LinearEasing))

    // Exactly at ~1.5s total duration
    onAnimationFinished()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .alpha(screenAlpha.value)
      .background(
        Brush.radialGradient(
          colors = listOf(
            PastelRoseCard.copy(alpha = 0.55f),
            PastelBackground
          ),
          radius = 800f
        )
      )
      .testTag("splash_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Animation Stage: Products falling into Bag
      Box(
        modifier = Modifier
          .size(230.dp)
          .scale(ambientGlow),
        contentAlignment = Alignment.Center
      ) {
        // Sparkle stars burst ring
        if (sparklesAlpha.value > 0f) {
          SparklesBurst(
            scale = sparklesScale.value,
            alpha = sparklesAlpha.value
          )
        }

        // Falling Products
        products.forEachIndexed { index, product ->
          val progress = itemProgress[index].value
          val alpha = itemAlphas[index].value

          if (alpha > 0f) {
            val startY = -110f
            val endY = 16f
            val currentY = startY + (endY - startY) * progress
            val currentScale = 1f - (progress * 0.35f)

            Box(
              modifier = Modifier
                .offset {
                  IntOffset(
                    x = (product.startXOffset * (1f - progress * 0.3f)).dp.roundToPx(),
                    y = currentY.dp.roundToPx()
                  )
                }
                .rotate(product.rotation * (1f - progress))
                .scale(currentScale)
                .alpha(alpha),
              contentAlignment = Alignment.Center
            ) {
              Surface(
                shape = CircleShape,
                color = product.bg,
                border = androidx.compose.foundation.BorderStroke(1.2.dp, product.tint.copy(alpha = 0.35f)),
                shadowElevation = 3.dp,
                modifier = Modifier.size(38.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = product.icon,
                    contentDescription = null,
                    tint = product.tint,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }
          }
        }

        // Luxury Boutique Shopping Bag (Bolso de compras)
        Box(
          modifier = Modifier
            .offset(y = 22.dp)
            .scale(bagScale.value)
            .rotate(bagRotation.value)
            .alpha(bagAlpha.value),
          contentAlignment = Alignment.Center
        ) {
          BoutiqueShoppingBagVector()
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Branding Title
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(textAlpha.value)
      ) {
        Text(
          text = "RIGUT MANAGER",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = 2.sp,
            color = PastelTextPrimary
          )
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = "CAILIN STORE",
          style = MaterialTheme.typography.bodySmall.copy(
            color = PastelTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
          )
        )
      }
    }
  }
}

/**
 * Beautiful vector Canvas representation of a boutique shopping tote bag
 * with ribbon handles, chic stitching, and rose gold trim.
 */
@Composable
fun BoutiqueShoppingBagVector() {
  Canvas(modifier = Modifier.size(110.dp, 120.dp)) {
    val width = size.width
    val height = size.height

    // 1. Soft drop shadow under the bag
    drawOval(
      color = Color(0x1F1E293B),
      topLeft = Offset(width * 0.12f, height * 0.90f),
      size = Size(width * 0.76f, height * 0.10f)
    )

    // 2. Twin Ribbon Handles (Looping over the top)
    val handlePath = Path().apply {
      moveTo(width * 0.32f, height * 0.32f)
      cubicTo(
        width * 0.32f, height * 0.02f,
        width * 0.68f, height * 0.02f,
        width * 0.68f, height * 0.32f
      )
    }
    // Shadow handle
    drawPath(
      path = handlePath,
      color = Color(0xFFD4AF37), // Champagne gold handle
      style = Stroke(width = 5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    )
    drawPath(
      path = handlePath,
      color = Color(0xFFFBBF24), // Gold highlight
      style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    )

    // 3. Trapezoidal Shopping Bag Body
    val bagPath = Path().apply {
      moveTo(width * 0.20f, height * 0.30f) // Top left
      lineTo(width * 0.80f, height * 0.30f) // Top right
      lineTo(width * 0.86f, height * 0.90f) // Bottom right
      lineTo(width * 0.14f, height * 0.90f) // Bottom left
      close()
    }

    // Gradient fill: Chic Blush Pink to Rose Quartz
    drawPath(
      path = bagPath,
      brush = Brush.verticalGradient(
        colors = listOf(
          Color(0xFFFFF1F2), // Very soft blush
          Color(0xFFFCE7F3), // Pastel rose
          Color(0xFFFBCFE8)  // Rose quartz base
        ),
        startY = height * 0.30f,
        endY = height * 0.90f
      )
    )

    // Bag Border outline
    drawPath(
      path = bagPath,
      color = Color(0xFFF472B6),
      style = Stroke(width = 1.8.dp.toPx())
    )

    // 4. Subtle front crease fold line for depth
    drawLine(
      color = Color(0x33F472B6),
      start = Offset(width * 0.30f, height * 0.30f),
      end = Offset(width * 0.26f, height * 0.90f),
      strokeWidth = 1.2.dp.toPx()
    )
    drawLine(
      color = Color(0x33F472B6),
      start = Offset(width * 0.70f, height * 0.30f),
      end = Offset(width * 0.74f, height * 0.90f),
      strokeWidth = 1.2.dp.toPx()
    )

    // 5. Chic Gold Brand Emblem in the center of the bag
    drawCircle(
      color = Color(0xFFFBBF24),
      radius = 7.dp.toPx(),
      center = Offset(width * 0.50f, height * 0.60f)
    )
    drawCircle(
      color = Color(0xFFFFFBEB),
      radius = 4.5.dp.toPx(),
      center = Offset(width * 0.50f, height * 0.60f)
    )

    // 6. Cute decorative bow / tag at handle base
    drawCircle(
      color = Color(0xFFE11D48),
      radius = 3.5.dp.toPx(),
      center = Offset(width * 0.32f, height * 0.32f)
    )
    drawCircle(
      color = Color(0xFFE11D48),
      radius = 3.5.dp.toPx(),
      center = Offset(width * 0.68f, height * 0.32f)
    )
  }
}

/**
 * Ring of cute star sparkles that expands outward on impact
 */
@Composable
private fun SparklesBurst(scale: Float, alpha: Float) {
  Canvas(
    modifier = Modifier
      .size(170.dp)
      .scale(scale)
      .alpha(alpha)
  ) {
    val center = Offset(size.width / 2f, size.height / 2f + 18.dp.toPx())
    val radius = 55.dp.toPx()

    val angles = listOf(0.0, 45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0)
    val colors = listOf(
      Color(0xFFFBBF24),
      Color(0xFFF472B6),
      Color(0xFF38BDF8),
      Color(0xFF34D399)
    )

    angles.forEachIndexed { i, deg ->
      val rad = Math.toRadians(deg)
      val x = center.x + (radius * kotlin.math.cos(rad)).toFloat()
      val y = center.y + (radius * kotlin.math.sin(rad)).toFloat()
      val color = colors[i % colors.size]

      drawCircle(
        color = color,
        radius = 3.dp.toPx(),
        center = Offset(x, y)
      )
    }
  }
}
