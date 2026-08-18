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

import com.google.gson.annotations.SerializedName

/**
 * Vollständiger Profil-Snapshot, der in SharedPreferences gespeichert wird.
 * Enthält DNS-Konfiguration, Firewall-Konfiguration und Per-App-Regeln.
 */
data class ProfileSnapshot(
    @SerializedName("profile_id") val profileId: Int,
    
    @SerializedName("dns_config") val dnsConfig: DnsConfigSnapshot,
    
    @SerializedName("firewall_config") val firewallConfig: FirewallConfigSnapshot,
    
    @SerializedName("app_rules") val appRules: List<AppRulesSnapshot>,
    
    @SerializedName("snapshot_version") val snapshotVersion: Int = 1,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long
) {
    companion object {
        const val CURRENT_SNAPSHOT_VERSION = 1
    }
}
