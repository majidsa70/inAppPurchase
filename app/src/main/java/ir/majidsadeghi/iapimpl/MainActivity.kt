package ir.majidsadeghi.iapimpl

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ir.majidsadeghi.iapimpl.payment.MyPurchaseInfo
import ir.majidsadeghi.iapimpl.payment.PurchaseHelper
import ir.majidsadeghi.iapimpl.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    private val purchaseHelper: PurchaseHelper by lazy {
        PurchaseHelper(purchaseListener = object : PurchaseHelper.PurchaseListener {

            override fun onConsumeFinished(purchase: MyPurchaseInfo) {

               // viewModel.consumePurchase(purchase, packageItem.price)

            }

            override fun onErrorInPayment(error: String) {
                //  showErrorMessage(error)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }

    fun initPayment(){
        purchaseHelper.startIAP(this)
    }

    fun consumePurchase(purchase: MyPurchaseInfo){
        purchaseHelper.consumePurchaseInSDK(purchase) {
            when {
                it.isSuccess -> {

                     //  setCreditResult(item.price ?: 0)

                }
                it.isFailure -> {
                   // showToast(getString(R.string.purchase_error))
                }
            }
        }
    }

    fun startPurchase(sku:String){
        purchaseHelper.launchPurchaseFlow(sku, activityResultRegistry)
    }

    override fun onDestroy() {
        super.onDestroy()
        purchaseHelper.stopService()
    }
}