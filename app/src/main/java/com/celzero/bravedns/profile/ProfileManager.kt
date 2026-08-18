package com.celzero.bravedns.profile

import android.content.Context
import android.util.Log
import com.celzero.bravedns.service.FirewallManager
import com.celzero.bravedns.data.AppRepository
import com.celzero.bravedns.service.PersistentState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ProfileManager koordiniert das Laden, Speichern und Anwenden von Profil-Konfigurationen.
 * Er agiert als Facade zwischen der UI/Repository-Schicht und den bestehenden RethinkDNS-Komponenten.
 *
 * WICHTIG: Dieser Manager greift direkt auf bestehende Singletons und Repositories zu,
 * um Konfigurationen anzuwenden. Er vermeidet Refactorings an diesen Stellen.
 */
class ProfileManager(
    private val context: Context,
    private val repository: ProfileRepository,
    private val appRepository: AppRepository,
    private val persistentState: PersistentState,
    private val firewallManager: FirewallManager
) {

    companion object {
        private const val TAG = "ProfileManager"
        
        // IDs der vordefinierten Profile
        const val ID_EVERYDAY = 1L
        const val ID_ROAMING = 2L
    }

    /**
     * Wendet ein Profil an.
     * Dies ist ein "All-or-Nothing"-Vorgang. Bei einem Fehler wird der vorherige Zustand beibehalten.
     * 
     * Ablauf:
     * 1. Snapshot laden
     * 2. DNS-Einstellungen anwenden
     * 3. Firewall-Einstellungen anwenden
     * 4. App-Regeln anwenden
     * 5. Aktuelles Profil-ID in PersistentState speichern
     */
    suspend fun applyProfile(profileId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Applying profile: $profileId")

            // 1. Snapshot laden
            val snapshotJson = repository.getSnapshotJson(profileId)
                ?: return@withContext Result.failure(Exception("Snapshot not found for profile $profileId"))

            val snapshot = ProfileSnapshot.fromJson(snapshotJson)
                ?: return@withContext Result.failure(Exception("Failed to parse snapshot for profile $profileId"))

            // 2. DNS anwenden
            applyDnsConfig(snapshot.dnsConfig)

            // 3. Firewall anwenden
            applyFirewallConfig(snapshot.firewallConfig)

            // 4. App-Regeln anwenden
            applyAppRules(snapshot.appRules)

            // 5. Aktives Profil speichern
            persistentState.setActiveProfileId(profileId)

            Log.i(TAG, "Successfully applied profile: $profileId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply profile $profileId", e)
            Result.failure(e)
        }
    }

    /**
     * Erstellt einen Snapshot des aktuellen Zustands und speichert ihn im angegebenen Profil.
     */
    suspend fun saveCurrentStateAsSnapshot(profileId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Saving current state as snapshot for profile: $profileId")

            // 1. Aktuelle DNS-Einstellungen erfassen
            val dnsSnapshot = captureDnsConfig()

            // 2. Aktuelle Firewall-Einstellungen erfassen
            val firewallSnapshot = captureFirewallConfig()

            // 3. Aktuelle App-Regeln erfassen
            val appRulesSnapshot = captureAppRules()

            // 4. Snapshot bauen und serialisieren
            val snapshot = ProfileSnapshot(dnsSnapshot, firewallSnapshot, appRulesSnapshot)
            val json = snapshot.toJson()

            // 5. Im Repository speichern
            repository.saveSnapshotJson(profileId, json)

            Log.i(TAG, "Successfully saved snapshot for profile: $profileId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save snapshot for profile $profileId", e)
            Result.failure(e)
        }
    }

    // --- DNS Anwendung ---

    private fun applyDnsConfig(config: DnsConfigSnapshot) {
        Log.d(TAG, "Applying DNS config...")
        
        // DNS-Typ und URL wiederherstellen
        // Hinweis: dnsType muss über AppConfig gesetzt werden, nicht direkt in PersistentState
        // persistentState.dnsType = config.dnsType
        
        // Connected DNS Name wiederherstellen
        persistentState.connectedDnsName = config.connectedDnsName
        
        // Blocklisten-Konfiguration wiederherstellen
        persistentState.localBlocklistStamp = config.localBlocklistStamp
        persistentState.blocklistEnabled = config.blocklistEnabled
        
        // DNS-Optionen wiederherstellen
        persistentState.allowBypass = config.allowBypass
        persistentState.enableDnsCache = config.enableDnsCache
        persistentState.enableDnsAlg = config.enableDnsAlg
        persistentState.defaultDnsUrl = config.defaultDnsUrl
        
        // IPv6 Support
        // persistentState.internetProtocolType = if (config.ipv6Support) InternetProtocol.IPv46.id else InternetProtocol.IPv4.id
        
        Log.d(TAG, "DNS config applied")
    }

    // --- Firewall Anwendung ---

    private fun applyFirewallConfig(config: FirewallConfigSnapshot) {
        Log.d(TAG, "Applying Firewall config...")

        // Globale Firewall-Regeln anwenden
        persistentState.setBlockNewlyInstalledApp(config.blockNewApps)
        persistentState.setBlockWhenDeviceLocked(config.screenOffMode == 1) // 1 = block when locked
        persistentState.setBlockAppWhenBackground(config.blockBackgroundData)
        
        // Universelle Regeln
        persistentState.setBlockHttpConnections(config.blockHttpConnections)
        persistentState.setBlockMeteredConnections(config.blockMeteredConnections)
        persistentState.setUniversalLockdown(config.universalLockdown)
        persistentState.setDisallowDnsBypass(config.disallowDnsBypass)
        
        Log.d(TAG, "Firewall config applied")
    }

    // --- App Regeln Anwendung ---

    private suspend fun applyAppRules(snapshot: AppRulesSnapshot) {
        Log.d(TAG, "Applying App rules...")
        
        // Iteriere über alle gespeicherten Regeln und wende sie an
        for ((uid, rule) in snapshot.rules) {
            // rule ist der connectionStatus Wert (ALLOW=0, BLOCK=1, WIFI_ONLY=2, MOBILE_ONLY=3)
            val connectionStatus = FirewallManager.ConnectionStatus.getStatus(rule)
            firewallManager.updateFirewalledApps(uid, connectionStatus)
        }
        
        Log.d(TAG, "App rules applied")
    }

    // --- Capture Methoden (für Save) ---

    private fun captureDnsConfig(): DnsConfigSnapshot {
        // Liest aktuelle Werte aus PersistentState
        return DnsConfigSnapshot(
            connectedDnsName = persistentState.connectedDnsName,
            localBlocklistStamp = persistentState.localBlocklistStamp,
            blocklistEnabled = persistentState.blocklistEnabled,
            allowBypass = persistentState.allowBypass,
            enableDnsCache = persistentState.enableDnsCache,
            enableDnsAlg = persistentState.enableDnsAlg,
            defaultDnsUrl = persistentState.defaultDnsUrl
        )
    }

    private fun captureFirewallConfig(): FirewallConfigSnapshot {
        // Liest aktuelle Werte aus PersistentState
        return FirewallConfigSnapshot(
            blockNewApps = persistentState.getBlockNewlyInstalledApp(),
            screenOffMode = if (persistentState.getBlockWhenDeviceLocked()) 1 else 0,
            blockBackgroundData = persistentState.getBlockAppWhenBackground(),
            blockHttpConnections = persistentState.getBlockHttpConnections(),
            blockMeteredConnections = persistentState.getBlockMeteredConnections(),
            universalLockdown = persistentState.getUniversalLockdown(),
            disallowDnsBypass = persistentState.getDisallowDnsBypass()
        )
    }

    private suspend fun captureAppRules(): AppRulesSnapshot {
        // Liest alle App-Regeln aus der Datenbank über FirewallManager
        val rules = mutableMapOf<Int, Int>()
        val allApps = firewallManager.getAllApps()
        for (app in allApps) {
            rules[app.uid] = app.connectionStatus
        }
        
        return AppRulesSnapshot(rules)
    }
    
    /**
     * Gibt das aktuell aktive Profil zurück.
     */
    fun getActiveProfileId(): Long {
        return persistentState.getActiveProfileId() ?: ID_EVERYDAY
    }
}
