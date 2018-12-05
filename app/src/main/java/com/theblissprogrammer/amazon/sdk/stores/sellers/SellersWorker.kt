package com.theblissprogrammer.amazon.sdk.stores.sellers

import com.theblissprogrammer.amazon.sdk.enums.MarketplaceType
import com.theblissprogrammer.amazon.sdk.extensions.coroutineCompletionOnUi
import com.theblissprogrammer.amazon.sdk.stores.sellers.models.SellerModels
import com.theblissprogrammer.amazon.sdk.common.Result.Companion.failure
import com.theblissprogrammer.amazon.sdk.common.CompletionResponse
import com.theblissprogrammer.amazon.sdk.enums.DefaultsKeys
import com.theblissprogrammer.amazon.sdk.enums.DefaultsKeys.Companion.sellerID
import com.theblissprogrammer.amazon.sdk.errors.DataError
import com.theblissprogrammer.amazon.sdk.logging.LogHelper
import com.theblissprogrammer.amazon.sdk.preferences.PreferencesWorkerType
import com.theblissprogrammer.amazon.sdk.stores.sellers.models.Seller

/**
 * Created by ahmedsaad on 2018-08-04.
 * Copyright (c) 2018. All rights reserved.
 **/
class SellersWorker(val store: SellersStore,
                    val cacheStore: SellersCacheStore?,
                    val preferencesWorker: PreferencesWorkerType): SellersWorkerType {

    override suspend fun fetch(request: SellerModels.Request, completion: CompletionResponse<Seller>) {
        // Use cache storage if applicable
        if (cacheStore == null) {
            coroutineCompletionOnUi(completion) {
                completion(store.fetch(request = request).await())
            }
            return
        }

        val cache = cacheStore.fetch(request = request).await()

        // Retrieve missing cache data from cloud if applicable
        if (cache.error != null && cache.error === DataError.NonExistent) {
            val response = this.store.fetch(request = request).await()
            val value = response.value
            if (value == null || !response.isSuccess) {
                completion(response)
            } else {
                completion(cacheStore.createOrUpdate(value).await())
            }
        }

        // Immediately return local response
        completion(cache)

        val cacheElement = cache.value
        if (cacheElement == null || !cache.isSuccess) {
            return
        }

        // Sync remote updates to cache if applicable
        val response = this.store.fetch(request = request).await()

        // Validate if any updates that needs to be stored
        val element = response.value
        if (element == null || !response.isSuccess) {
            return
        }

        // Update local storage with updated data
        val savedElement = cacheStore.createOrUpdate(element).await()

        if (!savedElement.isSuccess) {
            LogHelper.e(messages = *arrayOf("Could not save updated user locally" +
                    " from remote storage: ${savedElement.error?.localizedMessage ?: ""}"))
        } else {
            // Callback handler again if updated
            completion(savedElement)
        }
    }

    override suspend fun fetchCurrent(completion: CompletionResponse<Seller>) {
        val id = preferencesWorker.get(sellerID)
        val marketplace = preferencesWorker.get(DefaultsKeys.marketplace)

        if (id == null || marketplace == null || id.isEmpty() || marketplace.isEmpty()) {
            completion(failure(DataError.Unauthorized))
            return
        }

        val request = SellerModels.Request(
                id = id,
                marketplace = MarketplaceType.valueOf(marketplace)
        )

        fetch(request = request, completion = completion)
    }

}