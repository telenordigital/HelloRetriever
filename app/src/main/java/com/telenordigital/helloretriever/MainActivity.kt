package com.telenordigital.helloretriever

import android.content.ComponentName
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.customtabs.CustomTabsClient
import android.support.customtabs.CustomTabsIntent
import android.support.customtabs.CustomTabsServiceConnection
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
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
    private lateinit var mCustomTabsClient: CustomTabsClient

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val connection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                mCustomTabsClient = client
                mCustomTabsClient.warmup(0)
                val session = mCustomTabsClient.newSession(null)
                SessionHelper.currentSession = session
                session.mayLaunchUrl(Uri.parse(baseUrl), null, null)
                findViewById<Button>(R.id.launch_world).setOnClickListener {
                    launchUrlInCustomTab(baseUrl)
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                SessionHelper.currentSession = null
            }
        }
        CustomTabsClient.bindCustomTabsService(this, "com.android.chrome", connection)

        startSmsRetriever()
        registerReceiver()
    }

    private fun registerReceiver() {
        val smsBroadcastReceiver = SmsBroadcastReceiver(object : SmsHandler {
            override fun receivedSms(originatingAddress: String?, messageBody: String) {
                val pin = Regex("([0-9]{4})").find(messageBody)?.value ?: return
                // val url = "$baseUrl/id/submit-pin?pin=$pin"
                // suggestion: create /id/submit-pin endpoint to have a fixed endpoint to support
                // what is really a POST request in a GET format, for submitting pin via
                // query param. So we can support autofill in custom tabs/browsers, because
                // we can't POST a url in a custom tab/browser
                // The url below works, but by chance.
                val url = "$baseUrl/id/verify-phone?pin=$pin"
                launchUrlInCustomTab(url)
            }
        })
        registerReceiver(smsBroadcastReceiver, smsFilter)
    }
}

fun Context.startSmsRetriever() {
    val logTag = "SmsRetrieverApi"
    Log.i(logTag, "Starting sms retriever api client")
    val startSmsRetriever = SmsRetriever.getClient(this)
            .startSmsRetriever()
            .addOnSuccessListener {
                Log.i(logTag, "Successfully started sms retriever api client")
            }
    startSmsRetriever
            .addOnCanceledListener {
                val canceledMessage = "Failed to start sms retriever api client"
                Toast.makeText(this, canceledMessage, Toast.LENGTH_SHORT).show()
                Log.e(logTag, canceledMessage)
            }
}

fun Context.launchUrlInCustomTab(url : String) {
    CustomTabsIntent.Builder(SessionHelper.currentSession)
            .build()
            .launchUrl(this,
                    Uri.parse(url))
}
