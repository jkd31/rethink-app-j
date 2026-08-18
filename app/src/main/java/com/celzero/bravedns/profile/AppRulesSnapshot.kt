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

import com.celzero.bravedns.database.AppInfo

/**
 * Snapshot der Per-App-Firewall-Regeln für ein Profil.
 * Enthält nur die relevanten Firewall-Einstellungen pro App.
 */
data class AppRulesSnapshot(
    val packageName: String,
    val uid: Int,
    val firewallStatus: Int,
    val connectionStatus: Int,
    val backgroundAllowed: Boolean,
    val screenOffAllowed: Boolean,
    val isProxyExcluded: Boolean
) {
    companion object {
        /**
         * Erstellt einen AppRulesSnapshot aus einem AppInfo-Objekt.
         */
        fun fromAppInfo(appInfo: AppInfo): AppRulesSnapshot {
            return AppRulesSnapshot(
                packageName = appInfo.packageName,
                uid = appInfo.uid,
                firewallStatus = appInfo.firewallStatus,
                connectionStatus = appInfo.connectionStatus,
                backgroundAllowed = appInfo.backgroundAllowed,
                screenOffAllowed = appInfo.screenOffAllowed,
                isProxyExcluded = appInfo.isProxyExcluded
            )
        }
        
        /**
         * Wendet den Snapshot auf ein AppInfo-Objekt an.
         * Gibt eine neue AppInfo-Instanz zurück, um Seiteneffekte zu vermeiden.
         */
        fun applyToAppInfo(snapshot: AppRulesSnapshot, original: AppInfo): AppInfo {
            return AppInfo(
                packageName = snapshot.packageName,
                appName = original.appName,
                uid = snapshot.uid,
                isSystemApp = original.isSystemApp,
                firewallStatus = snapshot.firewallStatus,
                appCategory = original.appCategory,
                wifiDataUsed = original.wifiDataUsed,
                mobileDataUsed = original.mobileDataUsed,
                connectionStatus = snapshot.connectionStatus,
                isProxyExcluded = snapshot.isProxyExcluded,
                screenOffAllowed = snapshot.screenOffAllowed,
                backgroundAllowed = snapshot.backgroundAllowed,
                tombstoneTs = original.tombstoneTs
            )
        }
    }
}
