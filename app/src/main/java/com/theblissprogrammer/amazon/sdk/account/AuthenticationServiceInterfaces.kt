package com.theblissprogrammer.amazon.sdk.account

import com.theblissprogrammer.amazon.sdk.account.models.AccountModels
import com.theblissprogrammer.amazon.sdk.account.models.LoginModels
import com.theblissprogrammer.amazon.sdk.common.Result
import com.theblissprogrammer.amazon.sdk.common.CompletionResponse
import kotlinx.coroutines.Deferred

/**
 * Created by ahmedsaad on 2017-11-03.
 * Copyright © 2017. All rights reserved.
 */

interface AuthenticationService {
    val isAuthorized: Boolean
    fun pingAuthorization(): Deferred<Result<Void>>
    fun login(request: LoginModels.Request): Deferred<Result<AccountModels.ServerResponse>>
}

interface AuthenticationWorkerType {
    val isAuthorized: Boolean
    suspend fun pingAuthorization(completion: CompletionResponse<Void>)
    suspend fun login(request: LoginModels.Request,
              completion: CompletionResponse<AccountModels.Response>)
    fun logout()
}