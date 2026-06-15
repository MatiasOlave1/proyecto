package com.camposocampoolavevargas.proyecto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.camposocampoolavevargas.proyecto.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the "users" table.
 */
@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :identifier OR phone = :identifier LIMIT 1")
    suspend fun getUserByEmailOrPhone(identifier: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUser(userId: String)
}

