/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.user.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.db.BaseDao
import me.proton.core.user.data.entity.UserKeyEntity

@Dao
abstract class UserKeyDao : BaseDao<UserKeyEntity>() {

    @Query("SELECT * FROM UserKeyEntity WHERE userId = :userId")
    abstract fun findAllByUserId(userId: String): Flow<List<UserKeyEntity>>

    @Query("SELECT * FROM UserKeyEntity WHERE userId = :userId")
    abstract suspend fun getAllByUserId(userId: String): List<UserKeyEntity>
}