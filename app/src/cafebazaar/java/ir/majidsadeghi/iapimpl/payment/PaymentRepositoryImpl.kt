package ir.majidsadeghi.iapimpl.payment

import android.app.Activity
import androidx.activity.result.ActivityResultRegistry
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.entity.PurchaseInfo
import ir.cafebazaar.poolakey.request.PurchaseRequest
import java.lang.ref.WeakReference

class PaymentRepositoryImpl(override val activity: Activity) : PaymentRepository {
    private var paymentConnection: Connection? = null
    private var payment: Payment? = null
    private var weakActivity = WeakReference(activity)

    override fun initService(result: (Result<String>) -> Unit) {
        val localSecurityCheck = SecurityCheck.Enable(
            rsaPublicKey = "AppConstants.BAZAAR_KEY"
        )


        val paymentConfiguration = PaymentConfiguration(
            localSecurityCheck = localSecurityCheck
        )

        payment = Payment(context = weakActivity.get()?.applicationContext!!, config = paymentConfiguration)
        startConnection(result)
    }

    private fun startConnection(result: (Result<String>) -> Unit) {
        paymentConnection = payment?.connect {
            connectionSucceed {
                result.invoke(Result.success(""))
            }
            connectionFailed { throwable ->
                result.invoke(Result.failure(throwable))
            }
            disconnected {

            }
        }
    }

    override fun stopConnection() {
        paymentConnection?.disconnect()
    }

    override fun launchPurchase(
        SKU: String,
        payload: String,
        activityResultRegistry: ActivityResultRegistry,
        result: (Result<MyPurchaseInfo>) -> Unit
    ) {
        val purchaseRequest = PurchaseRequest(
            productId = SKU,
            payload = payload
        )

        payment?.purchaseProduct(
            registry = activityResultRegistry, request = purchaseRequest
        ) {
            purchaseFlowBegan {

            }
            failedToBeginFlow { throwable ->
                result(Result.failure(throwable))
            }
            purchaseSucceed { purchaseEntity ->
                result(Result.success(purchaseEntity.convertToMyPurchaseInfo()))
            }
            purchaseCanceled {
                result.invoke(
                    Result.failure(
                        Throwable()
                    )
                )
            }
            purchaseFailed { throwable ->
                result(Result.failure(throwable))
            }
        } ?: kotlin.run {
            callPurchaseFail(result)
        }
    }

    override fun getPurchasedList(result: (Result<List<MyPurchaseInfo>>) -> Unit) {
        payment?.getPurchasedProducts {
            querySucceed { purchasedProducts ->
                result.invoke(Result.success(purchasedProducts.map { it.convertToMyPurchaseInfo() }))
            }
            queryFailed { throwable ->
                result.invoke(Result.failure(throwable))
            }
        } ?: kotlin.run {
            callPurchaseFail(result)
        }
    }

    private fun <T> callPurchaseFail(result: (Result<T>) -> Unit) {
        result.invoke(Result.failure(Throwable("purchase is failed")))
    }

    override fun consumePurchase(purchaseInfo: MyPurchaseInfo, result: (Result<MyPurchaseInfo>) -> Unit) {
        payment?.consumeProduct(purchaseInfo.purchaseToken) {
            consumeSucceed {
                result(Result.success(purchaseInfo))
            }
            consumeFailed { throwable ->
                result(Result.failure(throwable))
            }
        } ?: kotlin.run {
            callPurchaseFail(result)
        }
    }
}

fun PurchaseInfo.convertToMyPurchaseInfo(): MyPurchaseInfo {
    return MyPurchaseInfo(
        orderId,
        purchaseToken,
        payload,
        packageName,
        purchaseTime,
        productId,
        originalJson,
        dataSignature
    )
}