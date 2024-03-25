package com.fullstackprojectbackend.securecapita.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import static com.twilio.rest.api.v2010.account.Message.creator;

public class SmsUtils {
    public static final String FROM_NUMBER = "+12406964145";
    public static final String SID_KEY = "AC193e6c49d0435d74dc07090937df9bd5";
    public static final String TOKEN_KEY = "f237d7cf6bfc2a511f0cf1fd5d0d8a8b";

    public static void sendSMS(String to, String messageBody){
        System.out.println("details" + to + " " +  messageBody);
        Twilio.init(SID_KEY,TOKEN_KEY);
        Message message = creator(new PhoneNumber("+" + to), new PhoneNumber(FROM_NUMBER), messageBody).create();
        System.out.println("message generated" + message);
    }

}
