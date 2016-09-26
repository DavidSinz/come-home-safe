package com.example.moni.comehomesafe;

import android.telephony.SmsManager;

public class SendSMS {

    private SmsManager smsManager;

    public SendSMS() {
        smsManager = SmsManager.getDefault();
    }

    public void sendMessage(String phoneNumber, String message) {
        smsManager.sendTextMessage(phoneNumber, null, message, null,null);
    }

}