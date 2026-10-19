package com.example.data.ota

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

data class GitHubReleaseInfo(
  val tagName: String,
  val releaseName: String,
  val changelog: String,
  val apkDownloadUrl: String?,
  val apkFileName: String?,
  val apkSizeBytes: Long,
  val publishedAt: String,
  val htmlUrl: String
)

/**
 * Gestor nativo para verificación, descarga e instalación de actualizaciones
 * del sistema Over-The-Air (OTA) directamente desde GitHub Releases.
 */
class OtaUpdateManager(private val context: Context) {

  companion object {
    private const val TAG = "OtaUpdateManager"
    const val DEFAULT_GITHUB_REPO = "carlitosrivera84593326/rigut-manager"
  }

  private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .followRedirects(true)
    .followSslRedirects(true)
    .build()

  /**
   * Consulta la API pública de GitHub para obtener la última release publicada.
   * Endpoint: GET https://api.github.com/repos/{owner}/{repo}/releases/latest
   */
  suspend fun checkLatestGitHubRelease(
    repoPath: String = DEFAULT_GITHUB_REPO
  ): Result<GitHubReleaseInfo> = withContext(Dispatchers.IO) {
    val cleanRepo = repoPath.trim().removePrefix("https://github.com/").trim('/')
    if (cleanRepo.isBlank() || !cleanRepo.contains("/")) {
      return@withContext Result.failure(
        IllegalArgumentException("Formato de repositorio inválido. Usa 'usuario/nombre-repositorio'.")
      )
    }

    val url = "https://api.github.com/repos/$cleanRepo/releases/latest"
    try {
      val request = Request.Builder()
        .url(url)
        .header("Accept", "application/vnd.github.v3+json")
        .header("User-Agent", "Rigut-Manager-Android-OTA")
        .build()

      val response = client.newCall(request).execute()
      if (!response.isSuccessful) {
        val code = response.code
        val bodyStr = response.body?.string() ?: ""
        response.close()
        return@withContext if (code == 404) {
          Result.failure(
            IllegalStateException("No se encontraron releases publicadas aún en '$cleanRepo'. Sube un release con tu archivo APK.")
          )
        } else {
          Result.failure(
            IllegalStateException("Error consultando GitHub (HTTP $code): $bodyStr")
          )
        }
      }

      val jsonStr = response.body?.string() ?: throw IllegalStateException("Respuesta vacía de GitHub")
      response.close()

      val json = JSONObject(jsonStr)
      val tagName = json.optString("tag_name", "v1.3.0")
      val releaseName = json.optString("name", tagName)
      val body = json.optString("body", "Mejoras de rendimiento y corrección de errores.")
      val publishedAt = json.optString("published_at", "")
      val htmlUrl = json.optString("html_url", "https://github.com/$cleanRepo/releases")

      var apkUrl: String? = null
      var apkName: String? = null
      var apkSize: Long = 0L

      val assets = json.optJSONArray("assets")
      if (assets != null) {
        for (i in 0 until assets.length()) {
          val asset = assets.getJSONObject(i)
          val name = asset.optString("name", "")
          if (name.endsWith(".apk", ignoreCase = true)) {
            apkUrl = asset.optString("browser_download_url")
            apkName = name
            apkSize = asset.optLong("size", 0L)
            break
          }
        }
      }

      val info = GitHubReleaseInfo(
        tagName = tagName,
        releaseName = releaseName,
        changelog = body,
        apkDownloadUrl = apkUrl,
        apkFileName = apkName ?: "rigut_manager_$tagName.apk",
        apkSizeBytes = apkSize,
        publishedAt = publishedAt,
        htmlUrl = htmlUrl
      )
      Result.success(info)
    } catch (e: Exception) {
      Log.e(TAG, "Error verificando releases en GitHub", e)
      Result.failure(e)
    }
  }

  /**
   * Descarga el archivo APK desde la URL provista por GitHub con seguimiento de progreso progresivo.
   */
  suspend fun downloadApk(
    downloadUrl: String,
    targetFileName: String,
    onProgress: (Float, String) -> Unit
  ): Result<File> = withContext(Dispatchers.IO) {
    try {
      onProgress(0.05f, "Iniciando conexión con servidor de GitHub...")
      val request = Request.Builder()
        .url(downloadUrl)
        .header("User-Agent", "Rigut-Manager-Android-OTA")
        .build()

      val response = client.newCall(request).execute()
      if (!response.isSuccessful) {
        response.close()
        return@withContext Result.failure(
          IllegalStateException("Error al descargar paquete APK desde GitHub (HTTP ${response.code})")
        )
      }

      val body = response.body ?: throw IllegalStateException("Cuerpo de respuesta vacío al descargar APK")
      val contentLength = body.contentLength()

      val outputDir = File(context.getExternalFilesDir(null) ?: context.cacheDir, "ota_updates")
      if (!outputDir.exists()) outputDir.mkdirs()

      val outputFile = File(outputDir, targetFileName)
      if (outputFile.exists()) outputFile.delete()

      var inputStream: InputStream? = null
      var outputStream: FileOutputStream? = null

      try {
        inputStream = body.byteStream()
        outputStream = FileOutputStream(outputFile)

        val buffer = ByteArray(8 * 1024)
        var totalBytesRead = 0L
        var bytesRead: Int

        onProgress(0.15f, "Descargando paquete de actualización...")

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
          outputStream.write(buffer, 0, bytesRead)
          totalBytesRead += bytesRead

          if (contentLength > 0) {
            val progress = 0.15f + (totalBytesRead.toFloat() / contentLength.toFloat()) * 0.70f
            val downloadedMb = String.format(java.util.Locale.US, "%.1f", totalBytesRead / (1024.0 * 1024.0))
            val totalMb = String.format(java.util.Locale.US, "%.1f", contentLength / (1024.0 * 1024.0))
            onProgress(progress.coerceIn(0.15f, 0.85f), "Descargando: $downloadedMb MB de $totalMb MB")
          }
        }

        outputStream.flush()
        onProgress(0.90f, "Verificando integridad del paquete descargado...")
      } finally {
        inputStream?.close()
        outputStream?.close()
        response.close()
      }

      if (!outputFile.exists() || outputFile.length() == 0L) {
        return@withContext Result.failure(IllegalStateException("El archivo descargado está vacío o corrupto"))
      }

      onProgress(0.98f, "Paquete verificado. Preparando instalación...")
      Result.success(outputFile)
    } catch (e: Exception) {
      Log.e(TAG, "Error durante la descarga del APK de GitHub", e)
      Result.failure(e)
    }
  }

  /**
   * Comprueba si la aplicación tiene permiso para instalar paquetes en Android 8.0+.
   */
  fun canRequestPackageInstalls(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      context.packageManager.canRequestPackageInstalls()
    } else {
      true
    }
  }

  /**
   * Abre la pantalla de ajustes de Android para que el usuario autorice "Instalar apps desconocidas".
   */
  fun openInstallPermissionSettings() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
        data = Uri.parse("package:${context.packageName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    }
  }

  /**
   * Lanza el instalador oficial de paquetes de Android utilizando FileProvider.
   * Android preguntará al usuario: "¿Deseas instalar una actualización para esta app?
   * Tus datos existentes no se perderán".
   */
  fun installApk(apkFile: File): Result<Unit> {
    return try {
      if (!apkFile.exists() || apkFile.length() == 0L) {
        return Result.failure(IllegalStateException("El archivo APK no existe o está dañado."))
      }

      val contentUri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        apkFile
      )

      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(contentUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      context.startActivity(intent)
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Error iniciando el instalador de Android", e)
      Result.failure(e)
    }
  }
}
