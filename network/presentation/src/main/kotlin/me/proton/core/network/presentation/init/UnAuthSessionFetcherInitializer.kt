/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.network.presentation.init

import android.content.Context
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import me.proton.core.network.presentation.UnAuthSessionFetcher

public class UnAuthSessionFetcherInitializer : Initializer<UnAuthSessionFetcher> {

    public override fun create(context: Context): UnAuthSessionFetcher {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            UnAuthSessionFetcherInitializerEntryPoint::class.java
        )
        val fetcher = entryPoint.getUnAuthSessionFetcher()
        fetcher.fetch()
        return fetcher
    }

    public override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface UnAuthSessionFetcherInitializerEntryPoint {
        fun getUnAuthSessionFetcher(): UnAuthSessionFetcher
    }
}
