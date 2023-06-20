package ir.majidsadeghi.iapimpl.payment



data class MyPurchaseInfo(
    val orderId: String,
    val purchaseToken: String,
    val payload: String,
    val packageName: String,
    val purchaseTime: Long,
    val productId: String?,
    val originalJson: String,
    val dataSignature: String
)



