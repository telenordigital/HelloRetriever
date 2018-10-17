package com.telenordigital.helloretriever

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.google.android.gms.auth.api.phone.SmsRetriever

/**
 * To get app signature use this once and store on server (don't send it)
 * val appSignatureHelper = AppSignatureHelper(this).appSignatures
 * 👉 XgATSyBF8ff 🚀
 *
 * To trigger the autofill send an SMS that looks like this to yourself:
 * ```
 * <#> Your TelenorID code is: 7102
 * XgATSyBF8ff
 * ```
 *
 * `<#>`, `7102`, and `XgATSyBF8ff` are mandatory - rest is fluff.
 */
class MainActivity : AppCompatActivity() {

    private val smsFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
    private val baseUrl = "https://connect.staging.telenordigital.com"
    private var smsBroadcastReceiver: BroadcastReceiver = SmsBroadcastReceiver(object : SmsHandler {
            override fun receivedSms(originatingAddress: String?, messageBody: String) {
                val pin = Regex("([0-9]{4})").find(messageBody)?.value ?: return
                val url = "$baseUrl/id/submit-pin?pin=$pin"
                launchUrlInCustomTab(url)
            }
        })

    private val connection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(className: ComponentName, client: CustomTabsClient) {
            customTabsClient = client
            client.warmup(0)
            session = client.newSession(object : CustomTabsCallback() {
                override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
                    if (navigationEvent == CustomTabsCallback.TAB_HIDDEN) {
                        onTabClosed()
                    }
                }
            })
            session!!.mayLaunchUrl(Uri.parse(baseUrl), null, null)
            findViewById<Button>(R.id.launch_world).setOnClickListener {
                onButtonClick()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            customTabsClient = null
            session = null
        }
    }

    private var customTabsClient: CustomTabsClient? = null
    private var session: CustomTabsSession? = null

    private fun onButtonClick() {
        startSmsRetriever()
        registerReceiver(smsBroadcastReceiver, smsFilter)
        launchUrlInCustomTab(baseUrl)
    }

    private fun onTabClosed() {
        unregisterReceiver(smsBroadcastReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CustomTabsClient.bindCustomTabsService(this, "com.android.chrome", connection)
    }

    private fun launchUrlInCustomTab(url : String) {
        CustomTabsIntent.Builder(session)
                .build()
                .launchUrl(this, Uri.parse(url))
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
        customTabsClient = null
        session = null
    }
}

fun Context.startSmsRetriever() {
    val logTag = "SmsRetrieverApi"
    Log.i(logTag, "Starting sms retriever api client")
    val smsRetriever = SmsRetriever.getClient(this)
            .startSmsRetriever()
            .addOnSuccessListener {
                Log.i(logTag, "Successfully started sms retriever api client")
            }
    smsRetriever
            .addOnCanceledListener {
                Log.e(logTag, "Failed to start sms retriever api client")
            }
}
