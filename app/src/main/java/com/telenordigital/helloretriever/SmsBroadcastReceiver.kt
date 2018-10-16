package com.telenordigital.helloretriever

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsMessage
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

/**
 * This class is a [BroadcastReceiver] and will listen for `SMS_RECEIVED_ACTION`.
 * It will call `smsHandler.receivedSms` with the phone number and message body on the
 * messages that are received.
 */
class SmsBroadcastReceiver (private val smsHandler: SmsHandler) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> handleSms(intent)
            SmsRetriever.SMS_RETRIEVED_ACTION -> handleRetrieverSms(context, intent)
            else -> return
        }
    }

    private fun handleRetrieverSms(context : Context, intent: Intent) {
        val extras = intent.extras
        val status = extras.get(SmsRetriever.EXTRA_STATUS) as? Status ?: return
        when (status.statusCode) {
            CommonStatusCodes.SUCCESS -> {
                val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as? String ?: return
                smsHandler.receivedSms(null, message)
            }
            CommonStatusCodes.TIMEOUT -> context.startSmsRetriever()
            else -> return
        }
    }

    // added for history: this is how it used to work, with plain SMS
    private fun handleSms(intent: Intent) {
        val bundle = intent.extras ?: return
        val messages = getSmsMessages(intent, bundle)
        val messageBody = getMessageBodies(messages)
        val originatingAddress = messages[0]?.originatingAddress
        smsHandler.receivedSms(originatingAddress, messageBody)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun getSmsMessages(intent: Intent, bundle: Bundle): Array<SmsMessage?> {
        return Telephony.Sms.Intents.getMessagesFromIntent(intent)
    }

    private fun getMessageBodies(messages: Array<SmsMessage?>): String {
        val stringBuilder = StringBuilder()
        for (message in messages) {
            stringBuilder.append(message?.messageBody)
        }
        return stringBuilder.toString()
    }

}
