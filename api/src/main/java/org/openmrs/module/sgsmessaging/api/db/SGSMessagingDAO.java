/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.sgsmessaging.api.db;

import java.util.List;

import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.module.sgsmessaging.api.SGSMessagingService;
import org.openmrs.module.sgsmessaging.domain.SGSMessagingMessage;

/**
 * Database methods for {@link SGSMessagingService}.
 */
public interface SGSMessagingDAO {
	
	/**
	 * @see MessageService#getAllMessages()
	 */
	public List<SGSMessagingMessage> getAllMessages();
	
	/**
	 * @see MessageService#getMessage(Integer)
	 */
	public SGSMessagingMessage getMessage(Integer messageId);
	
	/**
	 * @see MessageService#findMessages(MessagingGateway, Person, Person, String, Integer)
	 */
	public List<SGSMessagingMessage> findMessagesWithPeople(Person sender, Person recipient, String content, Integer status);
	
	/**
	 * @see MessageService#saveMessage(SGSMessagingMessage)
	 */
	public void saveMessage(SGSMessagingMessage message) throws APIException;
	
	/**
	 * @see MessageService#deleteMessage(SGSMessagingMessage)
	 */
	public void deleteMessage(SGSMessagingMessage message) throws APIException;
	
	public List<SGSMessagingMessage> getOutboxMessages();
}
