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

package me.proton.core.paymentiap.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
public class BillingIAPViewModel @Inject constructor(
    private val billingClient: BillingClient
) : ProtonViewModel() {

    private val mutableState = MutableStateFlow<State>(State.Initializing)
    public val state: StateFlow<State> = mutableState.asStateFlow()

    public sealed class State {
        public object Initializing : State()
        public object Initialized : State()
        public object QueryingProductDetails : State()
        public data class GoogleProductDetails(
            val amount: Long,
            val currency: String,
            val formattedPriceAndCurrency: String,
            val billingResult: BillingResult
        ) : State()

        public sealed class Error : State() {
            public object BillingClientUnavailable : State()
            public object BillingClientDisconnected : State()
            public sealed class ProductDetailsError : State() {
                public object ResponseCode : ProductDetailsError()
                public object Price : ProductDetailsError()
                public data class Message(val error: String? = null) : ProductDetailsError()
            }
        }
    }

    private lateinit var googlePlanName: String

    private val billingClientStateListener = object : BillingClientStateListener {

        override fun onBillingSetupFinished(billingResult: BillingResult) {
            val state = if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                viewModelScope.launch(Dispatchers.IO) {
                    queryProductDetails()
                }
                State.Initialized
            } else {
                State.Error.BillingClientUnavailable
            }
            mutableState.tryEmit(state)
        }

        override fun onBillingServiceDisconnected() {
            mutableState.tryEmit(State.Error.BillingClientDisconnected)
            billingClient.startConnection(this)
        }
    }

    private fun initialize() {
        mutableState.tryEmit(State.Initializing)
        billingClient.startConnection(billingClientStateListener)
    }

    public fun queryProductDetails(planName: String) {
        googlePlanName = planName
        initialize()
    }

    private fun queryProductDetails() = flow {
        emit(State.QueryingProductDetails)
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(googlePlanName)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val productList = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        val result = billingClient.queryProductDetails(productList)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            emit(State.Error.ProductDetailsError.ResponseCode)
        } else {
            val price = result.getProductPrice()
            if (price == null) {
                emit(State.Error.ProductDetailsError.Price)
            } else {
                emit(
                    State.GoogleProductDetails(
                        amount = price.priceAmountMicros,
                        currency = price.priceCurrencyCode,
                        formattedPriceAndCurrency = price.formattedPrice,
                        result.billingResult
                    )
                )
            }
        }
    }.catch { error ->
        mutableState.tryEmit(State.Error.ProductDetailsError.Message(error.message))
    }.onEach { subscriptionState ->
        mutableState.tryEmit(subscriptionState)
    }.launchIn(viewModelScope)

    override fun onCleared() {
        billingClient.endConnection()
    }

    private fun ProductDetailsResult.getProductPrice(): ProductDetails.PricingPhase? {
        val offerDetails = productDetailsList?.getOrNull(0)?.subscriptionOfferDetails
        return offerDetails?.getOrNull(0)?.pricingPhases?.pricingPhaseList?.getOrNull(0)
    }
}
