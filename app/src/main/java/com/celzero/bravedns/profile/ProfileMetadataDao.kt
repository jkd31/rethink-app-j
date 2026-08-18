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
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.celzero.bravedns.database.ProfileMetadata

/**
 * DAO für Profil-Metadaten.
 */
@Dao
interface ProfileMetadataDao {
    
    @Query("SELECT * FROM ProfileMetadata ORDER BY id")
    fun getAllProfiles(): List<ProfileMetadata>
    
    @Query("SELECT * FROM ProfileMetadata WHERE id = :id")
    fun getProfileById(id: Int): ProfileMetadata?
    
    @Query("SELECT * FROM ProfileMetadata WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): ProfileMetadata?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(profile: ProfileMetadata): Long
    
    @Update
    fun update(profile: ProfileMetadata): Int
    
    @Query("UPDATE ProfileMetadata SET isActive = :isActive WHERE id = :id")
    fun updateActiveStatus(id: Int, isActive: Boolean): Int
    
    @Query("UPDATE ProfileMetadata SET isActive = 0")
    fun deactivateAllProfiles(): Int
    
    @Query("DELETE FROM ProfileMetadata WHERE id = :id")
    fun deleteProfile(id: Int): Int
    
    companion object {
        const val PROFILE_EVERYDAY_ID = 1
        const val PROFILE_ROAMING_ID = 2
        
        const val DEFAULT_EVERYDAY_NAME = "Everyday"
        const val DEFAULT_ROAMING_NAME = "Roaming"
        
        const val DEFAULT_EVERYDAY_DESC = "Standardkonfiguration für den täglichen Gebrauch"
        const val DEFAULT_ROAMING_DESC = "Konfiguration für Roaming-Szenarien"
    }
}
