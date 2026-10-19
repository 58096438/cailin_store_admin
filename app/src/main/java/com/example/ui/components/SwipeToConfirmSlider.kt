package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.example.ui.theme.PastelBorderSoft
import com.example.ui.theme.PastelSurfaceVariant
import com.example.ui.theme.PastelTextPrimary

@Composable
fun SwipeToConfirmSlider(
  onConfirm: () -> Unit,
  modifier: Modifier = Modifier,
  text: String = "Desliza para confirmar",
  confirmedText: String = "¡Confirmado!",
  isLoading: Boolean = false,
  containerColor: Color = PastelSurfaceVariant,
  trackCompleteColor: Color = Color(0xFF059669),
  thumbColor: Color = PastelTextPrimary
) {
  val coroutineScope = rememberCoroutineScope()
  val density = LocalDensity.current
  val dragOffset = remember { Animatable(0f) }
  var isConfirmed by remember { mutableStateOf(false) }

  val thumbSize = 50.dp
  val thumbSizePx = with(density) { thumbSize.toPx() }
  val height = 56.dp

  LaunchedEffect(isConfirmed) {
    if (!isConfirmed && dragOffset.value > 0f) {
      dragOffset.snapTo(0f)
    }
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .height(height)
      .clip(RoundedCornerShape(28.dp))
      .background(containerColor)
      .border(1.dp, PastelBorderSoft, RoundedCornerShape(28.dp))
      .testTag("swipe_to_confirm_slider"),
    contentAlignment = Alignment.CenterStart
  ) {
    val totalWidthPx = constraints.maxWidth.toFloat()
    val maxDragPx = (totalWidthPx - thumbSizePx).coerceAtLeast(0f)
    val progress = if (maxDragPx > 0f) (dragOffset.value / maxDragPx).coerceIn(0f, 1f) else 0f

    // Progress track background
    Box(
      modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth(progress)
        .background(
          Brush.horizontalGradient(
            colors = listOf(
              Color(0xFF0F766E).copy(alpha = 0.85f),
              trackCompleteColor
            )
          )
        )
    )

    // Center descriptive text
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 48.dp),
      contentAlignment = Alignment.Center
    ) {
      if (isConfirmed || isLoading) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              color = Color.White,
              strokeWidth = 2.dp
            )
          } else {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }
          Text(
            text = " $confirmedText",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              color = if (progress > 0.4f) Color.White else PastelTextPrimary,
              fontSize = 14.sp
            )
          )
        }
      } else {
        Text(
          text = text,
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            color = if (progress > 0.5f) Color.White.copy(alpha = 0.9f) else PastelTextPrimary,
            fontSize = 13.5.sp,
            letterSpacing = 0.3.sp
          )
        )
      }
    }

    // Draggable thumb
    Box(
      modifier = Modifier
        .offset { IntOffset(dragOffset.value.roundToInt(), 0) }
        .size(thumbSize)
        .padding(3.dp)
        .shadow(4.dp, CircleShape)
        .clip(CircleShape)
        .background(if (isConfirmed) trackCompleteColor else thumbColor)
        .pointerInput(isConfirmed, isLoading) {
          if (!isConfirmed && !isLoading) {
            detectHorizontalDragGestures(
              onHorizontalDrag = { _, dragAmount ->
                val newOffset = (dragOffset.value + dragAmount).coerceIn(0f, maxDragPx)
                coroutineScope.launch {
                  dragOffset.snapTo(newOffset)
                }
              },
              onDragEnd = {
                coroutineScope.launch {
                  if (dragOffset.value >= maxDragPx * 0.75f) {
                    dragOffset.animateTo(maxDragPx, spring())
                    isConfirmed = true
                    onConfirm()
                  } else {
                    dragOffset.animateTo(0f, spring())
                  }
                }
              },
              onDragCancel = {
                coroutineScope.launch {
                  dragOffset.animateTo(0f, spring())
                }
              }
            )
          }
        },
      contentAlignment = Alignment.Center
    ) {
      if (isConfirmed) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Confirmado",
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      } else {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = "Deslizar",
          tint = Color.White,
          modifier = Modifier.size(22.dp)
        )
      }
    }
  }
}
