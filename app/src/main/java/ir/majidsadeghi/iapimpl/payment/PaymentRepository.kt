package ir.majidsadeghi.iapimpl.payment

import androidx.activity.result.ActivityResultRegistry

interface PaymentRepository {
    fun initService(result: (Result<String>) -> Unit)
    fun stopConnection()
    fun launchPurchase(
        SKU: String, payload: String,
        activityResultRegistry: ActivityResultRegistry,
        result: (Result<MyPurchaseInfo>) -> Unit
    )

    fun getPurchasedList(result: (Result<List<MyPurchaseInfo>>) -> Unit)

    fun consumePurchase(purchaseInfo: MyPurchaseInfo, result: (Result<MyPurchaseInfo>) -> Unit)
}