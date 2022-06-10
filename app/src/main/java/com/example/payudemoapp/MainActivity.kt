package com.example.payudemoapp

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.payudemoapp.databinding.ActivityMainBinding
import com.payu.base.models.ErrorResponse
import com.payu.base.models.PayUPaymentParams
import com.payu.checkoutpro.PayUCheckoutPro
import com.payu.checkoutpro.utils.PayUCheckoutProConstants
import com.payu.ui.model.listeners.PayUCheckoutProListener
import com.payu.ui.model.listeners.PayUHashGenerationListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.buttonFirst.setOnClickListener {
            proceedToPayment()
        }

    }

    private fun proceedToPayment() {
        val payUPaymentParams = PayUPaymentParams.Builder()
            .setAmount("1.0")
            .setIsProduction(true)
            .setKey(merchantKey)
            .setProductInfo("Test")
            .setPhone("9999999999")
            .setTransactionId(System.currentTimeMillis().toString())
            .setFirstName("John")
            .setEmail("john@yopmail.com")
            .setSurl("https://payu.herokuapp.com/success")
            .setFurl("https://payu.herokuapp.com/failure")
            .build()

        PayUCheckoutPro.open(
            this,
            payUPaymentParams,
            object : PayUCheckoutProListener {

                override fun onPaymentSuccess(response: Any) {
                    Log.d(TAG, "onPaymentSuccess() called with: response = $response")
                    showToast("Payment Success")
                }

                override fun onPaymentFailure(response: Any) {
                    Log.d(TAG, "onPaymentFailure() called with: response = $response")
                    showToast("Payment Failure")
                }

                override fun onPaymentCancel(isTxnInitiated: Boolean) {
                    Log.d(TAG, "onPaymentCancel() called with: isTxnInitiated = $isTxnInitiated")
                    showToast("Payment Cancelled")
                }

                override fun onError(errorResponse: ErrorResponse) {
                    Log.d(TAG, "onError() called with: errorResponse = [${errorResponse.errorCode}] ${errorResponse.errorMessage}")
                    showToast("[${errorResponse.errorCode}] ${errorResponse.errorMessage}")

                }

                override fun generateHash(
                    map: HashMap<String, String?>,
                    hashGenerationListener: PayUHashGenerationListener
                ) {
                    Log.d(
                        TAG,
                        "generateHash() called with: map = $map, hashGenerationListener = $hashGenerationListener"
                    )
                    if (map.containsKey(PayUCheckoutProConstants.CP_HASH_STRING)
                        && map.containsKey(PayUCheckoutProConstants.CP_HASH_STRING) != null
                        && map.containsKey(PayUCheckoutProConstants.CP_HASH_NAME)
                        && map.containsKey(PayUCheckoutProConstants.CP_HASH_NAME) != null
                    ) {

                        val hashData = map[PayUCheckoutProConstants.CP_HASH_STRING]
                        val hashName = map[PayUCheckoutProConstants.CP_HASH_NAME]
                        var salt = merchantSalt
                        if (map.containsKey(PayUCheckoutProConstants.CP_POST_SALT))
                            salt = salt.plus(map[PayUCheckoutProConstants.CP_POST_SALT])

                        var hash: String? = null

                        //Below hash should be calculated only when integrating Multi-currency support. If not integrating MCP
                        // then no need to have this if check.
                        if (hashName.equals(
                                PayUCheckoutProConstants.CP_LOOKUP_API_HASH,
                                ignoreCase = true
                            )
                        ) {

                            //Calculate HmacSHA1 hash using the hashData and merchant secret key
                            hash = HashGenerationUtils.generateHashFromSDK(
                                hashData!!,
                                salt,
                                merchantKey
                            )
                        } else {
                            //calculate SDH-512 hash using hashData and salt
                            hash = HashGenerationUtils.generateHashFromSDK(
                                hashData!!,
                                salt
                            )
                        }

                        if (!TextUtils.isEmpty(hash)) {
                            val hashMap: HashMap<String, String?> = HashMap()
                            hashMap[hashName!!] = hash!!
                            hashGenerationListener.onHashGenerated(hashMap)
                        }
                    }
                }

                override fun setWebViewProperties(webView: WebView?, bank: Any?) {
                    Log.d(
                        TAG,
                        "setWebViewProperties() called with: webView = $webView, bank = $bank"
                    )
                }
            })


    }

    private fun showToast(s: String) =
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()

    companion object {
        private const val TAG = "FirstFragment"
        private const val merchantKey = "7rnFly"
        private const val merchantSalt = "pjVQAWpA"
    }
}