package com.creditauto.app

import android.app.Application
import com.creditauto.app.data.CreditAutoRepository
import com.creditauto.app.data.location.BackgroundLocationPrefetchManager
import com.creditauto.app.data.location.LocationHelper
import com.creditauto.app.data.location.NearbyStoreDetector
import com.creditauto.app.update.GitHubAppUpdater
import com.creditauto.app.wallet.GoogleWalletLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CreditAutoApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var repository: CreditAutoRepository
        private set

    lateinit var storeDetector: NearbyStoreDetector
        private set

    lateinit var walletLauncher: GoogleWalletLauncher
        private set

    lateinit var appUpdater: GitHubAppUpdater
        private set

    override fun onCreate() {
        super.onCreate()
        repository = CreditAutoRepository(this)
        storeDetector = NearbyStoreDetector()
        walletLauncher = GoogleWalletLauncher(this)
        appUpdater = GitHubAppUpdater()

        appScope.launch {
            repository.awaitLoaded()
            if (
                repository.settings.value.backgroundPrefetchEnabled &&
                LocationHelper.hasBackgroundLocationPermission(this@CreditAutoApp)
            ) {
                BackgroundLocationPrefetchManager.start(this@CreditAutoApp)
            }
        }
    }
}
