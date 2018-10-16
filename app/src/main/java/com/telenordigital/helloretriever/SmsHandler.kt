package com.telenordigital.helloretriever

interface SmsHandler {
    fun receivedSms(originatingAddress: String?, messageBody: String)
}
