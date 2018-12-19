package com.theblissprogrammer.amazon.sdk.stores.orders

import com.theblissprogrammer.amazon.sdk.common.CompletionResponse
import com.theblissprogrammer.amazon.sdk.common.DeferredLiveResult
import com.theblissprogrammer.amazon.sdk.common.DeferredResult
import com.theblissprogrammer.amazon.sdk.common.LiveCompletionResponse
import com.theblissprogrammer.amazon.sdk.stores.orders.models.*

/**
 * Created by ahmedsaad on 2018-08-05.
 * Copyright (c) 2018. All rights reserved.
 **/
interface OrdersStore {
    fun fetch(request: OrderModels.Request): DeferredResult<List<ListOrder>>
}

interface OrdersCacheStore {
    fun fetch(request: OrderModels.Request): DeferredLiveResult<List<Order>>
    fun fetchOldestOrder(): DeferredLiveResult<Order>
    fun createOrUpdate(request: ListOrder): DeferredLiveResult<Order>
    fun createOrUpdate(vararg orders: ListOrder): DeferredResult<Void>
}

interface OrdersWorkerType {
    suspend fun fetch(request: OrderModels.Request, completion: LiveCompletionResponse<List<Order>>)
    suspend fun fetchOldestOrder(completion: LiveCompletionResponse<Order>)
}