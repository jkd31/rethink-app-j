/*
 * Copyright 2024 RethinkDNS and its authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.celzero.bravedns.profile

import android.content.Context
import android.content.SharedPreferences
import com.celzero.bravedns.database.AppInfo
import com.celzero.bravedns.database.RethinkDnsEndpoint
import com.celzero.bravedns.service.PersistentState
import com.celzero.bravedns.util.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Repository für Profil-Daten.
 * Speichert Profil-Metadaten in Room und Snapshots in SharedPreferences.
 */
class ProfileRepository(private val context: Context, private val profileMetadataDao: ProfileMetadataDao) {
    
    companion object {
        private const val PREFS_NAME = "profile_snapshots"
        private const val KEY_PREFIX = "profile_snapshot_"
        
        private val gson = Gson()
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Ruft alle Profile ab.
     */
    fun getAllProfiles(): List<ProfileMetadata> {
        return profileMetadataDao.getAllProfiles()
    }
    
    /**
     * Ruft ein Profil nach ID ab.
     */
    fun getProfileById(id: Int): ProfileMetadata? {
        return profileMetadataDao.getProfileById(id)
    }
    
    /**
     * Ruft das aktive Profil ab.
     */
    fun getActiveProfile(): ProfileMetadata? {
        return profileMetadataDao.getActiveProfile()
    }
    
    /**
     * Erstellt einen Snapshot aus dem aktuellen PersistentState.
     */
    fun createSnapshotFromCurrentState(
        profileId: Int,
        persistentState: PersistentState,
        allAppInfos: List<AppInfo>,
        connectedDnsEndpoint: RethinkDnsEndpoint?
    ): ProfileSnapshot {
        val currentTime = System.currentTimeMillis()
        
        // DNS-Konfiguration snapshoten
        val dnsSnapshot = DnsConfigSnapshot(
            dnsType = persistentState.dnsType,
            connectedDnsUrl = connectedDnsEndpoint?.url ?: "",
            connectedDnsName = persistentState.connectedDnsName,
            localBlocklistStamp = persistentState.localBlockListStamp,
            numberOfLocalBlocklists = persistentState.numberOfLocalBlocklists,
            blocklistEnabled = persistentState.blocklistEnabled,
            remoteBlocklistTimestamp = persistentState.remoteBlocklistUpdate,
            localBlocklistTimestamp = persistentState.localBlockListUpdate,
            dohEndpoint = connectedDnsEndpoint,
            enableDnsCache = persistentState.enableDnsCache,
            enableDnsAlg = persistentState.dnsAlg,
            defaultDnsUrl = persistentState.defaultDnsServer,
            privateIps = persistentState.privateIps,
            preventDnsLeaks = persistentState.preventDnsLeaks,
            proxyDns = persistentState.proxyDns
        )
        
        // Firewall-Konfiguration snapshoten
        val firewallSnapshot = FirewallConfigSnapshot(
            globalFirewallStatus = persistentState.globalFirewallStatus,
            blockUnknownConnections = persistentState.blockUnknownConnections,
            blockUdpTrafficOtherThanDns = persistentState.blockUdpTrafficOtherThanDns,
            blockWhenDeviceLocked = persistentState.blockWhenDeviceLocked,
            blockAppWhenBackground = persistentState.blockAppWhenBackground,
            blockHttpConnections = persistentState.blockHttpConnections,
            blockMeteredConnections = persistentState.blockMeteredConnections,
            universalLockdown = persistentState.universalLockdown,
            blockNewlyInstalledApp = persistentState.blockNewlyInstalledApp,
            braveMode = persistentState.braveMode,
            allowBypass = persistentState.allowBypass
        )
        
        // App-Regeln snapshoten
        val appRules = allAppInfos.map { appInfo ->
            AppRulesSnapshot.fromAppInfo(appInfo)
        }
        
        return ProfileSnapshot(
            profileId = profileId,
            dnsConfig = dnsSnapshot,
            firewallConfig = firewallSnapshot,
            appRules = appRules,
            snapshotVersion = ProfileSnapshot.CURRENT_SNAPSHOT_VERSION,
            createdAt = currentTime,
            updatedAt = currentTime
        )
    }
    
    /**
     * Speichert einen Profil-Snapshot in SharedPreferences.
     */
    fun saveSnapshot(snapshot: ProfileSnapshot) {
        val json = gson.toJson(snapshot)
        val key = "$KEY_PREFIX${snapshot.profileId}"
        sharedPreferences.edit().putString(key, json).apply()
    }
    
    /**
     * Lädt einen Profil-Snapshot aus SharedPreferences.
     */
    fun loadSnapshot(profileId: Int): ProfileSnapshot? {
        val key = "$KEY_PREFIX$profileId"
        val json = sharedPreferences.getString(key, null) ?: return null
        
        val type = object : TypeToken<ProfileSnapshot>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            // Bei Parsing-Fehlern null zurückgeben
            null
        }
    }
    
    /**
     * Aktualisiert die Metadaten eines Profils.
     */
    fun updateProfileMetadata(metadata: ProfileMetadata) {
        profileMetadataDao.update(metadata)
    }
    
    /**
     * Aktiviert ein Profil und deaktiviert alle anderen.
     */
    fun activateProfile(profileId: Int) {
        profileMetadataDao.deactivateAllProfiles()
        profileMetadataDao.updateActiveStatus(profileId, true)
    }
    
    /**
     * Initialisiert die Standardprofile (Everyday und Roaming).
     */
    fun initializeDefaultProfiles() {
        val existingProfiles = getAllProfiles()
        
        if (existingProfiles.isEmpty()) {
            val currentTime = System.currentTimeMillis()
            
            // Everyday-Profil
            val everydayProfile = ProfileMetadata(
                id = ProfileMetadataDao.PROFILE_EVERYDAY_ID,
                name = ProfileMetadataDao.DEFAULT_EVERYDAY_NAME,
                description = ProfileMetadataDao.DEFAULT_EVERYDAY_DESC,
                createdAt = currentTime,
                updatedAt = currentTime,
                isActive = true // Standardmäßig aktiv
            )
            profileMetadataDao.insert(everydayProfile)
            
            // Roaming-Profil
            val roamingProfile = ProfileMetadata(
                id = ProfileMetadataDao.PROFILE_ROAMING_ID,
                name = ProfileMetadataDao.DEFAULT_ROAMING_NAME,
                description = ProfileMetadataDao.DEFAULT_ROAMING_DESC,
                createdAt = currentTime,
                updatedAt = currentTime,
                isActive = false
            )
            profileMetadataDao.insert(roamingProfile)
        }
    }
}
