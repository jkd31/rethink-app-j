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
package com.celzero.bravedns.database

/**
 * Repository für Profil-Metadaten.
 * Dient als Wrapper um den DAO-Zugriff.
 */
class ProfileMetadataRepository(private val profileMetadataDao: ProfileMetadataDao) {
    
    fun getAllProfiles(): List<ProfileMetadata> {
        return profileMetadataDao.getAllProfiles()
    }
    
    fun getProfileById(id: Int): ProfileMetadata? {
        return profileMetadataDao.getProfileById(id)
    }
    
    fun getActiveProfile(): ProfileMetadata? {
        return profileMetadataDao.getActiveProfile()
    }
    
    fun insert(profile: ProfileMetadata): Long {
        return profileMetadataDao.insert(profile)
    }
    
    fun update(profile: ProfileMetadata): Int {
        return profileMetadataDao.update(profile)
    }
    
    fun updateActiveStatus(id: Int, isActive: Boolean): Int {
        return profileMetadataDao.updateActiveStatus(id, isActive)
    }
    
    fun deactivateAllProfiles(): Int {
        return profileMetadataDao.deactivateAllProfiles()
    }
    
    fun deleteProfile(id: Int): Int {
        return profileMetadataDao.deleteProfile(id)
    }
}
