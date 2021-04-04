/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.sgsmessaging.util;

import java.io.IOException;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for sgsmessaging module. It includes methods for connecting to RapidPro for posting
 * JSON requests and methods for parsing sgsmessaging module global properties configurations.
 * 
 * @author Bailly RURANGIRWA
 */
public class SGSMessagingUtil {
	
	private static final Logger log = LoggerFactory.getLogger(SGSMessagingUtil.class);
	
	/**
	 * Creates a JSON post request to a configured URL from "sgsmessaging.postURL" global property.
	 * 
	 * @param phone The phone number to send the message to
	 * @param messageText The actual message to send
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	public static CloseableHttpResponse postMessage(String phone, String messageText) throws ClientProtocolException, IOException, AuthenticationException {
		String countryCode = Context.getAdministrationService().getGlobalProperty("sgsmessaging.countryCode");
		
		String fixedPhoneNumber = phone.replaceFirst("0", countryCode);
		String json = "{ \"urns\": [ \"tel:" + fixedPhoneNumber + "\"], \"text\": \"" + new String(messageText.getBytes("UTF-8"), "ISO-8859-1") + "\" }";
		HttpPost httpPost = new HttpPost(Context.getAdministrationService().getGlobalProperty("sgsmessaging.postURL"));
		httpPost.setEntity(new StringEntity(json));
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		httpPost.setHeader("Authorization", Context.getAdministrationService().getGlobalProperty("sgsmessaging.Authorization"));
		
		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = client.execute(httpPost);
		client.close();
		return response;
	}
}
