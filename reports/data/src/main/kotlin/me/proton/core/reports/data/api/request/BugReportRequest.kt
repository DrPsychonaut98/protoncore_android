/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.reports.data.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class BugReportRequest(
    @SerialName("OS")
    val osName: String,

    @SerialName("OSVersion")
    val osVersion: String,

    @SerialName("Client")
    val client: String,

    @SerialName("ClientType")
    val clientType: Int,

    @SerialName("ClientVersion")
    val appVersionName: String,

    @SerialName("Title")
    val title: String,

    @SerialName("Description")
    val description: String,

    @SerialName("Username")
    val username: String,

    @SerialName("Email")
    val email: String,

    /** VPN-only */
    @SerialName("Country")
    val country: String? = null,

    /** VPN-only */
    @SerialName("ISP")
    val isp: String? = null
)
