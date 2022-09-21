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

package me.proton.core.plan.presentation.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.SubscriptionManagement
import me.proton.core.payment.domain.usecase.CreatePaymentTokenWithGoogleIAP
import me.proton.core.payment.domain.usecase.PerformSubscribe
import me.proton.core.payment.domain.usecase.ValidateSubscriptionPlan
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.presentation.entity.PlanCurrency
import me.proton.core.plan.presentation.entity.PlanCycle
import javax.inject.Inject

internal class RedeemGooglePurchase @Inject constructor(
    private val createPaymentTokenWithGoogleIAP: CreatePaymentTokenWithGoogleIAP,
    private val performSubscribe: PerformSubscribe,
    private val validateSubscriptionPlan: ValidateSubscriptionPlan
) {
    suspend operator fun invoke(
        googlePurchase: GooglePurchase,
        purchasedPlan: Plan,
        userId: UserId
    ) {
        val currency = PlanCurrency.valueOf(purchasedPlan.currency!!)
        val planCycle =
            PlanCycle.map[purchasedPlan.vendorNames.first { it.name in googlePurchase.productIds }.cycle]!!
        val planNames = listOf(purchasedPlan.name)
        val subscriptionStatus = validateSubscriptionPlan(
            userId,
            codes = null,
            plans = planNames,
            currency.toSubscriptionCurrency(),
            planCycle.toSubscriptionCycle()
        )
        val tokenResult = createPaymentTokenWithGoogleIAP(
            userId,
            subscriptionStatus.amountDue,
            subscriptionStatus.currency,
            PaymentType.GoogleIAP(
                googlePurchase.productIds.first(),
                googlePurchase.purchaseToken,
                googlePurchase.orderId,
                googlePurchase.packageName
            )
        )
        check(tokenResult.status == PaymentTokenStatus.CHARGEABLE)

        performSubscribe(
            userId,
            subscriptionStatus.amountDue,
            subscriptionStatus.currency,
            subscriptionStatus.cycle,
            planNames,
            codes = null,
            tokenResult.token,
            SubscriptionManagement.GOOGLE_MANAGED
        )
    }
}