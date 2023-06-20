package ir.majidsadeghi.iapimpl.payment

import android.app.Activity
import androidx.activity.result.ActivityResultRegistry
import ir.myket.billingclient.IabHelper
import ir.myket.billingclient.IabHelper.ITEM_TYPE_INAPP
import ir.myket.billingclient.util.Purchase
import java.lang.ref.WeakReference

class PaymentRepositoryImpl(override val activity: Activity
) : PaymentRepository {
    private var weakActivity = WeakReference(activity)
    private var mHelper: IabHelper? = null
    override fun initService(result: (Result<String>) -> Unit) {
        mHelper = IabHelper(weakActivity.get()?.baseContext, "AppConstants.MYKET_KEY")
        try {
            mHelper?.startSetup { status ->
                when {
                    status.isSuccess -> {
                        result.invoke(Result.success(""))
                    }

                    status.isFailure -> {
                        result.invoke(Result.failure(Throwable(status.message)))
                    }
                }


            }
        } catch (ex: Exception) {
            result.invoke(Result.failure(Throwable("")))
        }
    }

    override fun stopConnection() {
        mHelper?.dispose()
    }

    override fun launchPurchase(
        SKU: String,
        payload: String,
        activityResultRegistry: ActivityResultRegistry,
        result: (Result<MyPurchaseInfo>) -> Unit
    ) {

        /*if (mHelper?.asyncInProgress == true)
            return*/

        mHelper?.launchPurchaseFlow(
            weakActivity.get(),
            SKU,
            { status, purchase ->
                when {
                    status.isFailure -> {
                        result.invoke(
                            Result.failure(
                                Throwable()
                            )
                        )
                    }

                    status.isSuccess -> {
                        result.invoke(Result.success(purchase.convertToMyPurchase()))
                    }
                }
            },
            payload
        ) ?: kotlin.run {
            callPurchaseFail(result)
        }

    }


    override fun getPurchasedList(result: (Result<List<MyPurchaseInfo>>) -> Unit) {
        try {
            mHelper?.queryInventoryAsync { queryResult, inventory ->
                when {
                    queryResult.isSuccess -> {
                        val purchaseList = arrayListOf<MyPurchaseInfo>()

                        result.invoke(Result.success(purchaseList))
                    }

                    queryResult.isFailure -> {
                        result.invoke(Result.failure(Throwable(queryResult.message)))
                    }
                }
            } ?: kotlin.run {
                callPurchaseFail(result)
            }
        } catch (e: Exception) {
            callPurchaseFail(result)
        }
    }

    private fun <T> callPurchaseFail(result: (Result<T>) -> Unit) {
        result.invoke(Result.failure(Throwable("purchase is failed")))
    }

    override fun consumePurchase(
        purchaseInfo: MyPurchaseInfo,
        result: (Result<MyPurchaseInfo>) -> Unit
    ) {
        mHelper?.consumeAsync(
            purchaseInfo.convertToPurchase()
        ) { purchase, status ->
            when {
                status.isSuccess -> {
                    result.invoke(Result.success(purchase.convertToMyPurchase()))
                }

                status.isFailure -> {
                    result.invoke(
                        Result.failure(
                            Throwable()
                        )
                    )
                }
            }

        } ?: kotlin.run {
            callPurchaseFail(result)
        }
    }
}

fun Purchase.convertToMyPurchase(): MyPurchaseInfo {
    return MyPurchaseInfo(
        orderId = orderId,
        purchaseToken = token,
        payload = developerPayload,
        packageName = packageName,
        purchaseTime = purchaseTime,
        productId = sku,
        originalJson = originalJson,
        dataSignature = signature
    )
}

fun MyPurchaseInfo.convertToPurchase(): Purchase {
    return Purchase(
        ITEM_TYPE_INAPP,
        originalJson,
        dataSignature
    )
}