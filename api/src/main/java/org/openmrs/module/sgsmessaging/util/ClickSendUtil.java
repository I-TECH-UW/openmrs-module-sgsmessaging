package org.openmrs.module.sgsmessaging.util;

import java.util.Arrays;
import java.util.List;

import ClickSend.ApiClient;
import ClickSend.ApiException;
import ClickSend.Api.SmsApi;
import ClickSend.Model.SmsMessage;
import ClickSend.Model.SmsMessageCollection;

public class ClickSendUtil {

	public static void postMessage(String phone, String messageText) {
		ApiClient defaultClient = new ApiClient();
		defaultClient.setUsername("bailly.rurangirwa@logiic.com");
		defaultClient.setPassword("F979BB1E-0B9D-45AB-1F1B-95B31F460F0F");
		SmsApi apiInstance = new SmsApi(defaultClient);

		SmsMessage smsMessage = new SmsMessage();
		smsMessage.body(messageText);
		smsMessage.to(phone);
		smsMessage.source("+250788544678");

		List<SmsMessage> smsMessageList = Arrays.asList(smsMessage);
		// SmsMessageCollection | SmsMessageCollection model
		SmsMessageCollection smsMessages = new SmsMessageCollection();
		smsMessages.messages(smsMessageList);
		try {
			String result = apiInstance.smsSendPost(smsMessages);
			System.out.println(result);
		} catch (ApiException e) {
			System.err.println("Exception when calling SmsApi#smsSendPost");
			e.printStackTrace();
		}
	}
}
