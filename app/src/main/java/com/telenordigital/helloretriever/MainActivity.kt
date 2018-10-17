package com.telenordigital.helloretriever

import android.content.BroadcastReceiver
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
import android.support.customtabs.CustomTabsSession
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
    private var customTabsClient: CustomTabsClient? = null
    private var session: CustomTabsSession? = null
    private var smsBroadcastReceiver: BroadcastReceiver? = null

    private val connection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(className: ComponentName, client: CustomTabsClient) {
            customTabsClient = client
            client.warmup(0)
            session = client.newSession(null)
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

    private fun onButtonClick() {
        startSmsRetriever()
        registerReceiver()
        launchUrlInCustomTab(baseUrl)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun launchUrlInCustomTab(url : String) {
        CustomTabsIntent.Builder(session)
                .build()
                .launchUrl(this, Uri.parse(url))
    }

    override fun onStart() {
        super.onStart()
        CustomTabsClient.bindCustomTabsService(this, "com.android.chrome", connection)
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
    }

    private fun registerReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver(object : SmsHandler {
            override fun receivedSms(originatingAddress: String?, messageBody: String) {
                val pin = Regex("([0-9]{4})").find(messageBody)?.value ?: return
                val url = "$baseUrl/id/submit-pin?pin=$pin"
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
