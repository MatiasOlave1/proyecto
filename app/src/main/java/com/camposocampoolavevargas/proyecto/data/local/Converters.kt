package com.camposocampoolavevargas.proyecto.data.local

import androidx.room.TypeConverter
import com.camposocampoolavevargas.proyecto.data.local.model.AchievementType
import com.camposocampoolavevargas.proyecto.data.local.model.SleepQuality
import com.camposocampoolavevargas.proyecto.data.local.model.SyncStatus

/**
 * Type converters for converting complex enum types to and from their String representations
 * to allow them to be persisted in SQLite database tables.
 */
class Converters {

    @TypeConverter
    fun fromSleepQuality(quality: SleepQuality): String {
        return quality.name
    }

    @TypeConverter
    fun toSleepQuality(value: String): SleepQuality {
        return SleepQuality.valueOf(value)
    }

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return SyncStatus.valueOf(value)
    }

    @TypeConverter
    fun fromAchievementType(type: AchievementType): String {
        return type.name
    }

    @TypeConverter
    fun toAchievementType(value: String): AchievementType {
        return AchievementType.valueOf(value)
    }
}

