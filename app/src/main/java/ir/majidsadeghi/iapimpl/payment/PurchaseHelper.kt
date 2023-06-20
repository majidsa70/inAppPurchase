package ir.majidsadeghi.iapimpl.payment

import android.app.Activity
import androidx.activity.result.ActivityResultRegistry

class PurchaseHelper(private val purchaseListener: PurchaseListener) {

    private var paymentRepository: PaymentRepository? = null

    private val userPayload by lazy {
        ""
    }


    fun launchPurchaseFlow(sku: String, activityResultRegistry: ActivityResultRegistry) {
        paymentRepository?.launchPurchase(sku, userPayload, activityResultRegistry) { result ->

            when {
                result.isSuccess -> {
                    result.getOrNull()?.run {
                        consumePurchaseInServer(this)
                    }
                }
                result.isFailure -> {
                    purchaseListener.onErrorInPayment(result.exceptionOrNull()?.message ?: "")
                }
            }

        }
    }


    fun startIAP(activity: Activity) {
        paymentRepository = PaymentRepositoryImpl(activity).apply {
            initService { initResult ->
                when {
                    initResult.isSuccess -> {
                        paymentRepository?.getPurchasedList {
                            if (it.isFailure)
                                purchaseListener.onErrorInPayment(
                                    initResult.exceptionOrNull()?.message ?: ""
                                )
                            else {
                                it.getOrNull()?.forEach { purchase ->
                                    consumePurchaseInServer(purchase)
                                }
                            }
                        }
                    }
                    initResult.isFailure -> {
                        purchaseListener.onErrorInPayment(initResult.exceptionOrNull()?.message
                            ?: "")
                    }
                }
            }
        }


    }

    private fun consumePurchaseInServer(purchase: MyPurchaseInfo) {
        if (verifyDeveloperPayload(purchase))
            purchaseListener.onConsumeFinished(purchase)

    }

    fun consumePurchaseInSDK(purchase: MyPurchaseInfo, result: (Result<MyPurchaseInfo>) -> Unit) {
        if (verifyDeveloperPayload(purchase))
            paymentRepository?.consumePurchase(purchase, result)
    }


    private fun verifyDeveloperPayload(purchase: MyPurchaseInfo): Boolean {
        val payload = purchase.payload
        return payload == userPayload
    }

    fun stopService() {
        paymentRepository?.stopConnection()
    }

    interface PurchaseListener {
        fun onConsumeFinished(purchase: MyPurchaseInfo)
        fun onErrorInPayment(error: String)
    }

}