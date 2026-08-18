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

/**
 * Snapshot einer Firewall-Konfiguration für ein Profil.
 * Enthält nur die wesentlichen Firewall-Einstellungen.
 */
data class FirewallConfigSnapshot(
    // Ob neu installierte Apps blockiert werden
    val blockNewApps: Boolean,
    
    // Ob bei gesperrtem Gerät blockiert wird (0 = nein, 1 = ja)
    val screenOffMode: Int,
    
    // Ob Hintergrund-Apps blockiert werden
    val blockBackgroundData: Boolean,
    
    // Ob HTTP-Verbindungen blockiert werden
    val blockHttpConnections: Boolean,
    
    // Ob gemessene Verbindungen blockiert werden
    val blockMeteredConnections: Boolean,
    
    // Universal Lockdown
    val universalLockdown: Boolean,
    
    // Ob DNS-Bypass erlaubt ist (false = disallow bypass)
    val disallowDnsBypass: Boolean
)
