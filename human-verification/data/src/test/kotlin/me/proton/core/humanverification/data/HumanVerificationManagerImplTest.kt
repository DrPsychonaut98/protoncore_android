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

package me.proton.core.humanverification.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.humanverification.domain.repository.HumanVerificationRepository
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.humanverification.VerificationMethod
import me.proton.core.network.domain.session.ClientId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HumanVerificationManagerImplTest {

    private val humanRepository = mockk<HumanVerificationRepository>()
    private lateinit var humanVerificationManager: HumanVerificationManagerImpl

    private val flowOfHumanVerificationStateChangedLists = mutableListOf<HumanVerificationDetails>()
    private val session = Session(
        sessionId = SessionId("session1"),
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        scopes = listOf("full", "calendar", "mail")
    )
    private val clientId = ClientId.AccountSession(session.sessionId)

    @Before
    fun beforeEveryTest() {
        humanVerificationManager = HumanVerificationManagerImpl(humanRepository)
    }

    @Test
    fun `on handleHumanVerificationSuccess`() = runBlockingTest {
        val tokenType = "newTokenType"
        val tokenCode = "newTokenCode"

        val humanVerificationDetailsNeeded = HumanVerificationDetails(
            clientId = clientId,
            verificationMethods = listOf(VerificationMethod.EMAIL),
            captchaVerificationToken = null,
            state = HumanVerificationState.HumanVerificationNeeded,
            tokenType = null,
            tokenCode = null
        )

        val flowOfHumanVerificationDetailsLists = listOf(listOf(humanVerificationDetailsNeeded))

        val humanVerificationStateSlot = slot<HumanVerificationState>()
        coEvery {
            humanRepository.updateHumanVerificationState(
                clientId,
                capture(humanVerificationStateSlot),
                tokenType,
                tokenCode
            )
        } answers {
            flowOfHumanVerificationStateChangedLists.add(
                flowOfHumanVerificationDetailsLists.last().first { it.clientId == clientId }.copy(
                    state = humanVerificationStateSlot.captured
                )
            )
        }

        every { humanRepository.onHumanVerificationStateChanged(any()) } answers {
            flowOf(*flowOfHumanVerificationStateChangedLists.toTypedArray())
        }

        humanVerificationManager.handleHumanVerificationSuccess(clientId, tokenType, tokenCode)

        coVerify(exactly = 1) {
            humanRepository.updateHumanVerificationState(
                clientId,
                HumanVerificationState.HumanVerificationSuccess,
                tokenType,
                tokenCode
            )
        }
        val sessionStateLists = humanVerificationManager.onHumanVerificationStateChanged().toList()
        assertEquals(1, sessionStateLists.size)
        assertEquals(HumanVerificationState.HumanVerificationSuccess, sessionStateLists[0].state)
    }

    @Test
    fun `on handleHumanVerificationFailed`() = runBlockingTest {
        val humanVerificationDetailsNeeded = HumanVerificationDetails(
            clientId = clientId,
            verificationMethods = listOf(VerificationMethod.EMAIL),
            captchaVerificationToken = null,
            state = HumanVerificationState.HumanVerificationNeeded,
            tokenType = null,
            tokenCode = null
        )

        val flowOfHumanVerificationDetailsLists = listOf(listOf(humanVerificationDetailsNeeded))

        val humanVerificationStateSlot = slot<HumanVerificationState>()
        coEvery {
            humanRepository.updateHumanVerificationState(
                clientId,
                capture(humanVerificationStateSlot),
                null,
                null
            )
        } answers {
            flowOfHumanVerificationStateChangedLists.add(
                flowOfHumanVerificationDetailsLists.last().first { it.clientId == clientId }.copy(
                    state = humanVerificationStateSlot.captured
                )
            )
        }

        every { humanRepository.onHumanVerificationStateChanged(any()) } answers {
            flowOf(*flowOfHumanVerificationStateChangedLists.toTypedArray())
        }

        humanVerificationManager.handleHumanVerificationFailed(clientId)

        coVerify(exactly = 1) {
            humanRepository.updateHumanVerificationState(
                clientId,
                HumanVerificationState.HumanVerificationFailed,
                null,
                null
            )
        }

        val stateLists = humanVerificationManager.onHumanVerificationStateChanged().toList()
        assertEquals(1, stateLists.size)
        assertEquals(HumanVerificationState.HumanVerificationFailed, stateLists[0].state)
    }
}
