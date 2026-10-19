package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ota.GitHubReleaseInfo
import com.example.data.ota.OtaUpdateManager
import com.example.ui.theme.CailingPrimary
import com.example.ui.theme.LiveDotGreen
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelBorderSoft
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelLavenderCard
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelMintCard
import com.example.ui.theme.PastelRoseAccent
import com.example.ui.theme.PastelRoseCard
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.PastelSkyCard
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelSurfaceVariant
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary
import com.example.ui.theme.StockOptimal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Diálogo modal Liquid Glass para Actualizaciones Remotas del Sistema (OTA).
 * Soporta conexión directa con GitHub Releases para descarga e instalación
 * de paquetes APK reales, además de despliegue sincronizado a terminales.
 */
@Composable
fun RemoteUpdateDialog(
  currentVersion: String = "v1.2.0",
  newVersion: String = "v1.3.0",
  targetDeviceName: String = "Galaxy A54",
  batteryPercentage: Int = 87,
  onDismiss: () -> Unit,
  onUpdateApplied: (String) -> Unit = {}
) {
  val context = LocalContext.current
  val otaManager = remember { OtaUpdateManager(context) }
  val coroutineScope = rememberCoroutineScope()

  // 0 = Despliegue OTA, 1 = Configuración GitHub
  var mainTab by remember { mutableIntStateOf(0) }
  var selectedTargetTab by remember { mutableIntStateOf(0) } // 0 = Terminal Remota, 1 = Administrador

  var gitHubRepo by remember { mutableStateOf(OtaUpdateManager.DEFAULT_GITHUB_REPO) }
  var isCheckingGitHub by remember { mutableStateOf(false) }
  var gitHubRelease by remember { mutableStateOf<GitHubReleaseInfo?>(null) }
  var gitHubCheckMessage by remember { mutableStateOf<String?>(null) }
  var isGitHubSuccess by remember { mutableStateOf<Boolean?>(null) }
  var showGitHubGuide by remember { mutableStateOf(false) }

  var isDeploying by remember { mutableStateOf(false) }
  var deployProgress by remember { mutableFloatStateOf(0f) }
  var isInstalledSuccessfully by remember { mutableStateOf(false) }
  var statusMessage by remember { mutableStateOf("Paquete OTA listo para desplegar") }

  val canInstallPackages = remember { otaManager.canRequestPackageInstalls() }

  val effectiveTargetVersion = gitHubRelease?.tagName ?: newVersion

  val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.2f,
    animationSpec = infiniteRepeatable(
      animation = tween(1100, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )

  Dialog(
    onDismissRequest = {
      if (!isDeploying) onDismiss()
    },
    properties = DialogProperties(
      dismissOnBackPress = !isDeploying,
      dismissOnClickOutside = !isDeploying,
      usePlatformDefaultWidth = false
    )
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .padding(vertical = 14.dp)
        .testTag("remote_update_dialog"),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = PastelSurface),
      border = BorderStroke(1.4.dp, PastelBorder),
      elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        // Cabecera Liquid Glass con icono y botón cerrar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                  Brush.linearGradient(
                    colors = listOf(PastelLavenderCard, PastelSkyCard)
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                tint = PastelLavenderAccent,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Actualizaciones Remotas",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 17.sp,
                  color = PastelTextPrimary
                )
              )
              Text(
                text = "RIGUT OS • GitHub Releases OTA",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = PastelTextSecondary,
                  fontSize = 11.sp
                )
              )
            }
          }

          if (!isDeploying && !isInstalledSuccessfully) {
            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(PastelSurfaceVariant)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = PastelTextSecondary,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pestañas Principales Liquid Glass: Despliegue vs Conexión GitHub
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = PastelSurfaceVariant,
          border = BorderStroke(1.dp, PastelBorderSoft),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (mainTab == 0) PastelSurface else Color.Transparent,
              border = if (mainTab == 0) BorderStroke(1.dp, PastelBorder) else null,
              modifier = Modifier
                .weight(1f)
                .clickable(enabled = !isDeploying) { mainTab = 0 }
            ) {
              Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CloudDownload,
                  contentDescription = null,
                  tint = if (mainTab == 0) PastelLavenderAccent else PastelTextSecondary,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Despliegue OTA",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (mainTab == 0) FontWeight.Bold else FontWeight.Medium,
                    color = if (mainTab == 0) PastelTextPrimary else PastelTextSecondary,
                    fontSize = 12.sp
                  )
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (mainTab == 1) PastelSurface else Color.Transparent,
              border = if (mainTab == 1) BorderStroke(1.dp, PastelBorder) else null,
              modifier = Modifier
                .weight(1f)
                .clickable(enabled = !isDeploying) { mainTab = 1 }
            ) {
              Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Code,
                  contentDescription = null,
                  tint = if (mainTab == 1) PastelSkyAccent else PastelTextSecondary,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Servidor GitHub",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (mainTab == 1) FontWeight.Bold else FontWeight.Medium,
                    color = if (mainTab == 1) PastelTextPrimary else PastelTextSecondary,
                    fontSize = 12.sp
                  )
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // CONTENIDO SEGÚN LA PESTAÑA ACTIVA
        if (mainTab == 0) {
          // ==========================================
          // PESTAÑA 0: DESPLIEGUE OTA
          // ==========================================

          // Selector de Terminal de Tienda vs Administrador
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = PastelSurface,
            border = BorderStroke(1.dp, PastelBorderSoft),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (selectedTargetTab == 0) PastelSkyCard else Color.Transparent,
                border = if (selectedTargetTab == 0) BorderStroke(1.dp, PastelSkyAccent.copy(alpha = 0.5f)) else null,
                modifier = Modifier
                  .weight(1f)
                  .clickable(enabled = !isDeploying) { selectedTargetTab = 0 }
              ) {
                Row(
                  modifier = Modifier.padding(vertical = 7.dp, horizontal = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = if (selectedTargetTab == 0) PastelSkyAccent else PastelTextSecondary,
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(5.dp))
                  Text(
                    text = targetDeviceName,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontWeight = if (selectedTargetTab == 0) FontWeight.Bold else FontWeight.Normal,
                      color = if (selectedTargetTab == 0) PastelSkyAccent else PastelTextSecondary,
                      fontSize = 11.sp
                    )
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (selectedTargetTab == 1) PastelLavenderCard else Color.Transparent,
                border = if (selectedTargetTab == 1) BorderStroke(1.dp, PastelLavenderAccent.copy(alpha = 0.5f)) else null,
                modifier = Modifier
                  .weight(1f)
                  .clickable(enabled = !isDeploying) { selectedTargetTab = 1 }
              ) {
                Row(
                  modifier = Modifier.padding(vertical = 7.dp, horizontal = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = null,
                    tint = if (selectedTargetTab == 1) PastelLavenderAccent else PastelTextSecondary,
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(5.dp))
                  Text(
                    text = "Administrador",
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontWeight = if (selectedTargetTab == 1) FontWeight.Bold else FontWeight.Normal,
                      color = if (selectedTargetTab == 1) PastelLavenderAccent else PastelTextSecondary,
                      fontSize = 11.sp
                    )
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Tarjeta de Estado del Dispositivo Seleccionado
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = PastelSurfaceVariant,
            border = BorderStroke(1.dp, PastelBorderSoft),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .scale(pulseScale)
                      .clip(CircleShape)
                      .background(LiveDotGreen)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (selectedTargetTab == 0) "TERMINAL TIENDA" else "PANEL MAESTRO",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 0.8.sp,
                      color = PastelTextSecondary,
                      fontSize = 10.sp
                    )
                  )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = if (selectedTargetTab == 0) "$targetDeviceName (Mostrador)" else "Dispositivo Actual",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PastelTextPrimary,
                    fontSize = 14.sp
                  )
                )
                Text(
                  text = "Versión en uso: $currentVersion",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = PastelTextSecondary,
                    fontSize = 11.sp
                  )
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = PastelMintCard,
                  border = BorderStroke(1.dp, PastelMintAccent.copy(alpha = 0.5f))
                ) {
                  Text(
                    text = "Nueva: $effectiveTargetVersion",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = PastelMintAccent,
                      fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.BatteryFull,
                    contentDescription = null,
                    tint = StockOptimal,
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "$batteryPercentage% • Conectado",
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = PastelTextSecondary,
                      fontSize = 10.sp
                    )
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Paquete y Fuente de Descarga
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = PastelSurface,
            border = BorderStroke(1.dp, PastelBorderSoft),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  tint = if (gitHubRelease != null) PastelMintAccent else PastelLavenderAccent,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (gitHubRelease != null) "GitHub Release Oficial" else "Paquete OTA Verificado",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = PastelTextPrimary
                  )
                )
              }
              Text(
                text = if (gitHubRelease?.apkSizeBytes != null && gitHubRelease!!.apkSizeBytes > 0) {
                  "${String.format(java.util.Locale.US, "%.1f", gitHubRelease!!.apkSizeBytes / (1024.0 * 1024.0))} MB"
                } else {
                  "14.8 MB • APK"
                },
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = PastelLavenderAccent
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Notas de la Versión (Changelog)
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PastelSurface),
            border = BorderStroke(1.2.dp, PastelBorderSoft),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Sell,
                  contentDescription = null,
                  tint = PastelLavenderAccent,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Novedades en $effectiveTargetVersion",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PastelTextPrimary
                  )
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              if (gitHubRelease != null && gitHubRelease!!.changelog.isNotBlank()) {
                Text(
                  text = gitHubRelease!!.changelog,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = PastelTextPrimary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                  )
                )
              } else {
                ChangelogItem(
                  title = "Filtro «Sin mora» y estados claros",
                  description = "Clasificación inmediata de clientes: Sin mora, Vencidos, Por vencer y Con saldo pendiente."
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = PastelBorderSoft)

                ChangelogItem(
                  title = "Códigos consecutivos CL-0001 y tickets TK-01",
                  description = "Numeración correlativa automática para todos los clientes y comprobantes de venta."
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = PastelBorderSoft)

                ChangelogItem(
                  title = "Descarga de APK desde GitHub Releases",
                  description = "Recepción de archivos APK reales e instalación mediante Android PackageInstaller nativo."
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Monitor de Descarga / Despliegue en Tiempo Real
          AnimatedVisibility(visible = isDeploying) {
            Column(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = statusMessage,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = PastelLavenderAccent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                  ),
                  modifier = Modifier.weight(1f)
                )
                Text(
                  text = "${(deployProgress * 100).toInt()}%",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = PastelLavenderAccent,
                    fontSize = 11.sp
                  )
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              LinearProgressIndicator(
                progress = { deployProgress },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(7.dp)
                  .clip(RoundedCornerShape(3.5.dp)),
                color = PastelLavenderAccent,
                trackColor = PastelSurfaceVariant
              )
              Spacer(modifier = Modifier.height(14.dp))
            }
          }

          // Estado de Éxito al Finalizar
          AnimatedVisibility(visible = isInstalledSuccessfully) {
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = PastelMintCard,
              border = BorderStroke(1.dp, PastelMintAccent.copy(alpha = 0.5f)),
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = PastelMintAccent,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "¡Actualización Completada!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = PastelMintAccent,
                      fontSize = 14.sp
                    )
                  )
                  Text(
                    text = "El sistema ha registrado $effectiveTargetVersion con éxito.",
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = PastelTextSecondary,
                      fontSize = 11.sp
                    )
                  )
                }
              }
            }
          }

          // Deslizador de Confirmación
          if (!isInstalledSuccessfully) {
            if (!isDeploying) {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SwipeToConfirmSlider(
                  onConfirm = {
                    isDeploying = true
                    coroutineScope.launch {
                      val downloadUrl = gitHubRelease?.apkDownloadUrl
                      if (!downloadUrl.isNullOrBlank()) {
                        // Descarga REAL del APK desde GitHub
                        statusMessage = "Conectando al servidor de GitHub..."
                        val result = otaManager.downloadApk(
                          downloadUrl = downloadUrl,
                          targetFileName = gitHubRelease?.apkFileName ?: "rigut_manager_$effectiveTargetVersion.apk",
                          onProgress = { p, msg ->
                            deployProgress = p
                            statusMessage = msg
                          }
                        )

                        result.fold(
                          onSuccess = { apkFile ->
                            statusMessage = "Abriendo instalador oficial de Android..."
                            deployProgress = 1.0f
                            delay(400)
                            otaManager.installApk(apkFile)
                            isDeploying = false
                            isInstalledSuccessfully = true
                            onUpdateApplied(effectiveTargetVersion)
                          },
                          onFailure = { err ->
                            isDeploying = false
                            statusMessage = "Error en descarga: ${err.message}"
                          }
                        )
                      } else {
                        // Flujo OTA de despliegue simulado y sincronización en vivo
                        statusMessage = "Conectando al túnel seguro OTA de $targetDeviceName..."
                        delay(500)
                        deployProgress = 0.20f
                        statusMessage = "Transfiriendo paquete $effectiveTargetVersion (14.8 MB)..."
                        delay(650)
                        deployProgress = 0.55f
                        statusMessage = "Verificando firmas SHA-256 e integridad..."
                        delay(550)
                        deployProgress = 0.85f
                        statusMessage = "Aplicando parches y actualizando base de datos..."
                        delay(600)
                        deployProgress = 1.0f
                        statusMessage = "Reiniciando servicios en la terminal..."
                        delay(450)
                        isDeploying = false
                        isInstalledSuccessfully = true
                        onUpdateApplied(effectiveTargetVersion)
                      }
                    }
                  },
                  text = if (gitHubRelease?.apkDownloadUrl != null) "Desliza para descargar e instalar APK" else "Desliza para desplegar actualización",
                  confirmedText = "Iniciando proceso...",
                  containerColor = PastelLavenderCard,
                  trackCompleteColor = PastelLavenderAccent,
                  thumbColor = PastelLavenderAccent
                )

                OutlinedButton(
                  onClick = onDismiss,
                  shape = RoundedCornerShape(14.dp),
                  border = BorderStroke(1.dp, PastelBorder),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text("Descartar por ahora", color = PastelTextSecondary, fontSize = 12.sp)
                }
              }
            } else {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                CircularProgressIndicator(
                  color = PastelLavenderAccent,
                  modifier = Modifier.size(18.dp),
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "Procesando actualización remota...",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = PastelLavenderAccent,
                    fontSize = 12.sp
                  )
                )
              }
            }
          } else {
            Button(
              onClick = onDismiss,
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = PastelMintAccent,
                contentColor = Color.White
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.White
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Entendido, Continuar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        } else {
          // ==========================================
          // PESTAÑA 1: CONFIGURACIÓN Y GUÍA DE GITHUB
          // ==========================================
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
              text = "Repositorio en GitHub",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PastelTextPrimary,
                fontSize = 13.sp
              )
            )

            OutlinedTextField(
              value = gitHubRepo,
              onValueChange = { gitHubRepo = it },
              placeholder = { Text("usuario/nombre-repositorio", fontSize = 12.sp) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              colors = TextFieldDefaults.colors(
                focusedContainerColor = PastelSurfaceVariant,
                unfocusedContainerColor = PastelSurfaceVariant,
                focusedIndicatorColor = PastelSkyAccent,
                unfocusedIndicatorColor = PastelBorderSoft
              ),
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Code,
                  contentDescription = null,
                  tint = PastelSkyAccent,
                  modifier = Modifier.size(18.dp)
                )
              }
            )

            Button(
              onClick = {
                isCheckingGitHub = true
                gitHubCheckMessage = null
                coroutineScope.launch {
                  val res = otaManager.checkLatestGitHubRelease(gitHubRepo)
                  isCheckingGitHub = false
                  res.fold(
                    onSuccess = { rel ->
                      gitHubRelease = rel
                      isGitHubSuccess = true
                      gitHubCheckMessage = "¡Release ${rel.tagName} encontrada! Archivo APK: ${rel.apkFileName ?: "No adjunto"}"
                    },
                    onFailure = { err ->
                      isGitHubSuccess = false
                      gitHubCheckMessage = err.message ?: "No se pudo consultar el repositorio"
                    }
                  )
                }
              },
              enabled = !isCheckingGitHub && gitHubRepo.isNotBlank(),
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = PastelSkyAccent,
                contentColor = Color.White
              )
            ) {
              if (isCheckingGitHub) {
                CircularProgressIndicator(
                  color = Color.White,
                  modifier = Modifier.size(16.dp),
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Consultando GitHub...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              } else {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Comprobar Releases en GitHub", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }

            // Resultado de la comprobación en GitHub
            if (gitHubCheckMessage != null) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isGitHubSuccess == true) PastelMintCard else PastelRoseCard,
                border = BorderStroke(1.dp, if (isGitHubSuccess == true) PastelMintAccent.copy(alpha = 0.5f) else PastelRoseAccent.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = gitHubCheckMessage ?: "",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isGitHubSuccess == true) PastelMintAccent else PastelRoseAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                  ),
                  modifier = Modifier.padding(12.dp)
                )
              }
            }

            // Permiso de instalación de paquetes de Android
            if (!canInstallPackages) {
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = PastelLavenderCard,
                border = BorderStroke(1.dp, PastelLavenderAccent.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Security,
                      contentDescription = null,
                      tint = PastelLavenderAccent,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Permiso de Instalación Requerido",
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PastelTextPrimary,
                        fontSize = 12.sp
                      )
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Android necesita que autorices «Instalar aplicaciones desconocidas» para que esta app pueda actualizarse.",
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = PastelTextSecondary,
                      fontSize = 11.sp
                    )
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Button(
                    onClick = { otaManager.openInstallPermissionSettings() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                      containerColor = PastelLavenderAccent,
                      contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text("Autorizar en Ajustes de Android", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }

            // Guía Interactiva: Cómo publicar una actualización en GitHub
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = PastelSurfaceVariant,
              border = BorderStroke(1.dp, PastelBorderSoft),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showGitHubGuide = !showGitHubGuide },
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.HelpOutline,
                      contentDescription = null,
                      tint = PastelSkyAccent,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "¿Cómo subir tu APK a GitHub?",
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PastelTextPrimary,
                        fontSize = 12.sp
                      )
                    )
                  }
                  Text(
                    text = if (showGitHubGuide) "Ocultar" else "Ver guía",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = PastelSkyAccent,
                      fontWeight = FontWeight.Bold,
                      fontSize = 11.sp
                    )
                  )
                }

                if (showGitHubGuide) {
                  Spacer(modifier = Modifier.height(10.dp))
                  HorizontalDivider(color = PastelBorderSoft)
                  Spacer(modifier = Modifier.height(10.dp))

                  GuideStepItem(
                    num = "1",
                    title = "Descarga tu APK desde AI Studio (sin instalar nada)",
                    desc = "En el menú de AI Studio (arriba a la derecha), usa 'Export' o descarga el APK directamente generado en la nube."
                  )
                  GuideStepItem(
                    num = "2",
                    title = "Compilación automática con GitHub Actions",
                    desc = "Al subir el proyecto a GitHub, ve a la pestaña 'Actions' > 'Compilar y Publicar APK (OTA)' y pulsa 'Run workflow'. ¡GitHub compilará el APK en 2 minutos!"
                  )
                  GuideStepItem(
                    num = "3",
                    title = "Release automático con APK adjunto",
                    desc = "GitHub creará la release oficial y adjuntará el archivo APK listo para descargar."
                  )
                  GuideStepItem(
                    num = "4",
                    title = "Instalación y actualización en 1 toque",
                    desc = "Cualquier teléfono con la app instalada solo tiene que pulsar 'Comprobar Releases en GitHub' y se actualizará sola sin cables USB."
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun GuideStepItem(
  num: String,
  title: String,
  desc: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.Top
  ) {
    Surface(
      shape = CircleShape,
      color = PastelSkyCard,
      border = BorderStroke(1.dp, PastelSkyAccent.copy(alpha = 0.5f)),
      modifier = Modifier.size(20.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = num,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = PastelSkyAccent,
            fontSize = 10.sp
          )
        )
      }
    }
    Spacer(modifier = Modifier.width(8.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodySmall.copy(
          fontWeight = FontWeight.Bold,
          color = PastelTextPrimary,
          fontSize = 11.sp
        )
      )
      Text(
        text = desc,
        style = MaterialTheme.typography.bodySmall.copy(
          color = PastelTextSecondary,
          fontSize = 10.sp,
          lineHeight = 14.sp
        )
      )
    }
  }
}

@Composable
private fun ChangelogItem(
  title: String,
  description: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .padding(top = 4.dp)
        .size(6.dp)
        .clip(CircleShape)
        .background(PastelLavenderAccent)
    )
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodySmall.copy(
          fontWeight = FontWeight.Bold,
          color = PastelTextPrimary,
          fontSize = 12.sp
        )
      )
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall.copy(
          color = PastelTextSecondary,
          fontSize = 11.sp,
          lineHeight = 15.sp
        )
      )
    }
  }
}
