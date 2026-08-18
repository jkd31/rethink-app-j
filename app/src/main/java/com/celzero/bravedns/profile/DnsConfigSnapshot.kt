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

import com.celzero.bravedns.database.RethinkDnsEndpoint

/**
 * Snapshot einer DNS-Konfiguration für ein Profil.
 * Enthält nur die wesentlichen DNS-Einstellungen, nicht den gesamten PersistentState.
 */
data class DnsConfigSnapshot(
    // DNS-Typ (z.B. RETHINK_REMOTE, SYSTEM_DNS, etc.)
    val dnsType: Int,
    
    // URL des aktuellen DNS-Endpoints
    val connectedDnsUrl: String,
    
    // Name des aktuellen DNS-Endpoints
    val connectedDnsName: String,
    
    // Lokale Blocklist-Stamps (Base64-codiert)
    val localBlocklistStamp: String,
    
    // Anzahl der lokalen Blocklists
    val numberOfLocalBlocklists: Int,
    
    // Ob lokale Blocklists aktiviert sind
    val blocklistEnabled: Boolean,
    
    // Remote Blocklist Timestamp
    val remoteBlocklistTimestamp: Long,
    
    // Lokaler Blocklist Timestamp
    val localBlocklistTimestamp: Long,
    
    // DNS-over-HTTPS Endpoint (falls verwendet)
    val dohEndpoint: RethinkDnsEndpoint?,
    
    // Ob DNS-Caching aktiviert ist
    val enableDnsCache: Boolean,
    
    // Ob DNS-Alg aktiviert ist
    val enableDnsAlg: Boolean,
    
    // Standard-DNS-URL
    val defaultDnsUrl: String,
    
    // Private IPs routing
    val privateIps: Boolean,
    
    // Prevent DNS leaks
    val preventDnsLeaks: Boolean,
    
    // Proxy DNS requests over proxy
    val proxyDns: Boolean
)
