package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CailingOutline
import com.example.ui.theme.PastelBackground
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.PastelSkyBorder
import com.example.ui.theme.PastelSkyCard
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelSurfaceVariant
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary

@Composable
fun CailingTopBar(
  title: String = "Inicio",
  alertCount: Int,
  isSyncing: Boolean,
  canNavigateBack: Boolean = false,
  onBackClick: () -> Unit = {},
  onAlertsClick: () -> Unit,
  onProfileClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = PastelBackground,
    modifier = modifier.fillMaxWidth()
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left: Back button (if canNavigateBack) OR Store Icon + Title
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = if (canNavigateBack) {
            Modifier
              .weight(1f, fill = false)
              .clickable { onBackClick() }
          } else {
            Modifier.weight(1f, fill = false)
          }
        ) {
          if (canNavigateBack) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .shadow(2.dp, CircleShape, ambientColor = Color(0x1F000000))
                .clip(CircleShape)
                .background(PastelSurface)
                .border(1.2.dp, PastelBorder, CircleShape)
                .clickable { onBackClick() }
                .testTag("top_bar_back_button"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Regresar a la pantalla anterior",
                tint = PastelTextPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
          } else {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PastelSurfaceVariant)
                .border(1.2.dp, PastelBorder, RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Image(
                painter = painterResource(id = R.drawable.img_bag_icon),
                contentDescription = "Logo Cailin Store",
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
              )
            }
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
              text = if (canNavigateBack) "REGRESAR" else "CAILIN STORE",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = if (canNavigateBack) PastelSkyAccent else PastelTextSecondary
              ),
              maxLines = 1
            )
            Text(
              text = title,
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = PastelTextPrimary
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        // Right: Notification Bell & Profile
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Notification button with distinct pastel border and shadow
          Box(
            modifier = Modifier
              .size(40.dp)
              .shadow(2.dp, CircleShape, ambientColor = Color(0x1F000000))
              .clip(CircleShape)
              .background(com.example.ui.theme.PastelSurface)
              .border(1.dp, com.example.ui.theme.PastelBorder, CircleShape)
              .clickable { onAlertsClick() }
              .testTag("notifications_button"),
            contentAlignment = Alignment.Center
          ) {
            BadgedBox(
              badge = {
                if (alertCount > 0) {
                  Badge(
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White
                  ) {
                    Text(text = alertCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notificaciones",
                tint = com.example.ui.theme.PastelTextPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          // Profile avatar
          Box(
            modifier = Modifier
              .size(40.dp)
              .shadow(2.dp, CircleShape, ambientColor = Color(0x1F000000))
              .clip(CircleShape)
              .background(com.example.ui.theme.CailingPrimary)
              .clickable { onProfileClick() }
              .testTag("profile_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Perfil Propietaria",
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }
    }
  }
}
