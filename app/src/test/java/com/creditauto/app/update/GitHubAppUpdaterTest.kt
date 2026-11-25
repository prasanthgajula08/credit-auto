package com.creditauto.app.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubAppUpdaterTest {

    @Test
    fun comparesSemanticVersionsCorrectly() {
        assertTrue(GitHubAppUpdater.isNewerVersion("v1.2.0", "1.1.0"))
        assertTrue(GitHubAppUpdater.isNewerVersion("v1.10.0", "1.2.0"))
        assertTrue(GitHubAppUpdater.isNewerVersion("2.0.0", "1.9.9"))
        assertFalse(GitHubAppUpdater.isNewerVersion("v1.2.0", "1.2.0"))
        assertFalse(GitHubAppUpdater.isNewerVersion("v1.1.9", "1.2.0"))
    }

    @Test
    fun parsesLatestReleaseJsonAndFindsApkAsset() {
        val sampleJson = """
            {
              "tag_name": "v1.3.0",
              "draft": false,
              "body": "Faster store lookup and new cards",
              "assets": [
                {
                  "name": "checksums.txt",
                  "size": 120,
                  "browser_download_url": "https://github.com/prasanthgajula08/credit-auto/releases/download/v1.3.0/checksums.txt"
                },
                {
                  "name": "CreditAuto.apk",
                  "size": 1900000,
                  "browser_download_url": "https://github.com/prasanthgajula08/credit-auto/releases/download/v1.3.0/CreditAuto.apk"
                }
              ]
            }
        """.trimIndent()

        val updater = GitHubAppUpdater()
        val update = updater.parseLatestReleaseJson(sampleJson, "1.2.0")
        assertNotNull(update)
        assertEquals("v1.3.0", update?.tagName)
        assertEquals("1.3.0", update?.versionName)
        assertEquals(1900000L, update?.assetSizeBytes)
        assertTrue(update?.downloadUrl?.endsWith("CreditAuto.apk") == true)

        // Same version returns null (up to date)
        assertNull(updater.parseLatestReleaseJson(sampleJson, "1.3.0"))
    }
}
