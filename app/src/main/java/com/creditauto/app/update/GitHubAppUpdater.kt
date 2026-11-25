package com.creditauto.app.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Immutable
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

@Immutable
data class ReleaseUpdateInfo(
    val tagName: String,
    val versionName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val assetSizeBytes: Long
)

@Immutable
sealed interface UpdateStatus {
    data object Idle : UpdateStatus
    data object Checking : UpdateStatus
    data object UpToDate : UpdateStatus
    data class Available(val info: ReleaseUpdateInfo) : UpdateStatus
    data class Downloading(val info: ReleaseUpdateInfo, val progressPercent: Int) : UpdateStatus
    data class Error(val message: String) : UpdateStatus
}

enum class InstallLaunchResult {
    LAUNCHED_INSTALLER,
    NEEDS_INSTALL_PERMISSION,
    FAILED
}

class GitHubAppUpdater(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun checkForUpdate(
        repoSlug: String,
        currentVersionName: String
    ): Result<ReleaseUpdateInfo?> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanRepo = repoSlug.trim().removePrefix("https://github.com/").trim('/')
            require(cleanRepo.contains("/")) { "Repository must be in owner/repo format" }

            val request = Request.Builder()
                .url("https://api.github.com/repos/$cleanRepo/releases/latest")
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "CreditAutoAndroid/$currentVersionName")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.code == 404) {
                    return@runCatching null
                }
                if (!response.isSuccessful) {
                    throw IllegalStateException("GitHub API HTTP ${response.code}")
                }
                val raw = response.body?.string().orEmpty()
                parseLatestReleaseJson(raw, currentVersionName)
            }
        }
    }

    suspend fun downloadApk(
        context: Context,
        info: ReleaseUpdateInfo,
        onProgress: (Int) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            // Clean up any older downloaded APKs
            updatesDir.listFiles()?.forEach { it.delete() }

            val safeTag = info.tagName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val finalFile = File(updatesDir, "CreditAuto-$safeTag.apk")
            val tempFile = File(updatesDir, "CreditAuto-$safeTag.apk.tmp")

            val request = Request.Builder()
                .url(info.downloadUrl)
                .header("User-Agent", "CreditAutoAndroid")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("APK download failed (HTTP ${response.code})")
                }
                val body = response.body ?: throw IllegalStateException("Empty APK response")
                val totalBytes = body.contentLength().takeIf { it > 0 } ?: info.assetSizeBytes

                body.byteStream().use { input ->
                    tempFile.outputStream().use { output ->
                        val buffer = ByteArray(16 * 1024)
                        var downloaded = 0L
                        var lastReportedPct = -1
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            output.write(buffer, 0, read)
                            downloaded += read
                            if (totalBytes > 0) {
                                val pct = ((downloaded * 100) / totalBytes).toInt().coerceIn(0, 100)
                                if (pct != lastReportedPct) {
                                    lastReportedPct = pct
                                    onProgress(pct)
                                }
                            }
                        }
                        output.flush()
                    }
                }
            }

            if (!tempFile.renameTo(finalFile)) {
                tempFile.copyTo(finalFile, overwrite = true)
                tempFile.delete()
            }
            onProgress(100)
            finalFile
        }
    }

    fun launchInstaller(context: Context, apkFile: File): InstallLaunchResult {
        if (!context.packageManager.canRequestPackageInstalls()) {
            val permIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return runCatching {
                context.startActivity(permIntent)
                InstallLaunchResult.NEEDS_INSTALL_PERMISSION
            }.getOrDefault(InstallLaunchResult.FAILED)
        }

        return runCatching {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(installIntent)
            InstallLaunchResult.LAUNCHED_INSTALLER
        }.getOrDefault(InstallLaunchResult.FAILED)
    }

    internal fun parseLatestReleaseJson(
        rawJson: String,
        currentVersionName: String
    ): ReleaseUpdateInfo? {
        if (rawJson.isBlank()) return null
        val root = json.parseToJsonElement(rawJson).jsonObject
        val isDraft = root["draft"]?.jsonPrimitive?.booleanOrNull == true
        if (isDraft) return null

        val tagName = root["tag_name"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
        if (tagName.isBlank()) return null
        val versionName = tagName.removePrefix("v").removePrefix("V")

        if (!isNewerVersion(versionName, currentVersionName)) {
            return null
        }

        val assets = root["assets"]?.jsonArray ?: return null
        val apkAsset = assets.firstOrNull { elem ->
            val name = elem.jsonObject["name"]?.jsonPrimitive?.contentOrNull.orEmpty()
            name.endsWith(".apk", ignoreCase = true)
        }?.jsonObject ?: return null

        val downloadUrl = apkAsset["browser_download_url"]?.jsonPrimitive?.contentOrNull.orEmpty()
        if (downloadUrl.isBlank()) return null

        val sizeBytes = apkAsset["size"]?.jsonPrimitive?.longOrNull ?: 0L
        val notes = root["body"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()

        return ReleaseUpdateInfo(
            tagName = tagName,
            versionName = versionName,
            releaseNotes = notes,
            downloadUrl = downloadUrl,
            assetSizeBytes = sizeBytes
        )
    }

    companion object {
        fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
            val remoteParts = parseVersionParts(remoteVersion)
            val currentParts = parseVersionParts(currentVersion)
            val maxLen = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r != c) return r > c
            }
            return false
        }

        private fun parseVersionParts(raw: String): List<Int> {
            return raw.trim()
                .removePrefix("v")
                .removePrefix("V")
                .substringBefore('-')
                .split('.')
                .map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
        }
    }
}
