package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CailingPrimary
import com.example.ui.theme.PastelBackground
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelTextSecondary

data class NavItem(
  val tabIndex: Int,
  val label: String,
  val icon: ImageVector,
  val tag: String
)

@Composable
fun FloatingLiquidGlassBottomNav(
  activeTab: Int,
  onTabSelected: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val items = listOf(
    NavItem(0, "Inicio", Icons.Default.GridView, "nav_tab_dashboard"),
    NavItem(1, "Stock", Icons.Default.Inventory2, "nav_tab_inventory"),
    NavItem(2, "Estadísticas", Icons.Default.TrendingUp, "nav_tab_stats"),
    NavItem(3, "Ajustes", Icons.Default.Settings, "nav_tab_settings")
  )

  // Container with smooth pastel gradient backing for navigation bar area
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color.Transparent,
            PastelBackground.copy(alpha = 0.85f),
            PastelBackground
          )
        )
      )
      .navigationBarsPadding()
      .padding(start = 12.dp, top = 4.dp, end = 12.dp, bottom = 6.dp)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(
          elevation = 10.dp,
          shape = RoundedCornerShape(28.dp),
          ambientColor = Color(0x24000000),
          spotColor = Color(0x1F1E293B)
        )
        .border(
          width = 1.2.dp,
          color = PastelBorder,
          shape = RoundedCornerShape(28.dp)
        ),
      shape = RoundedCornerShape(28.dp),
      color = PastelSurface.copy(alpha = 0.98f),
      tonalElevation = 0.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        items.forEach { item ->
          val isSelected = activeTab == item.tabIndex
          val interactionSource = remember { MutableInteractionSource() }

          val pillBgColor by animateColorAsState(
            targetValue = if (isSelected) CailingPrimary else Color.Transparent,
            animationSpec = tween(200),
            label = "pillBgColor"
          )

          val contentColor by animateColorAsState(
            targetValue = if (isSelected) Color.White else PastelTextSecondary,
            animationSpec = tween(200),
            label = "contentColor"
          )

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(20.dp))
              .background(pillBgColor)
              .clickable(
                interactionSource = interactionSource,
                indication = null
              ) { onTabSelected(item.tabIndex) }
              .padding(vertical = 6.dp, horizontal = 2.dp)
              .testTag(item.tag),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(19.dp)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 10.sp,
                  color = contentColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }
  }
}
