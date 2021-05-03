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
package org.openmrs.module.sgsmessaging.api.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.sgsmessaging.SGSMessagingConfig;
import org.openmrs.module.sgsmessaging.api.SGSMessagingService;
import org.openmrs.module.sgsmessaging.api.db.SGSMessagingDAO;
import org.openmrs.module.sgsmessaging.domain.SGSMessagingMessage;
import org.openmrs.module.sgsmessaging.util.SGSMessagingUtil;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;

/**
 * It is a default implementation of {@link SGSMessagingService}.
 */
public class SGSMessagingServiceImpl extends BaseOpenmrsService implements SGSMessagingService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private SGSMessagingDAO dao;
	
	/**
	 * @param dao the dao to set
	 */
	public void setDao(SGSMessagingDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @return the dao
	 */
	public SGSMessagingDAO getDao() {
		return dao;
	}
	
	public List<SGSMessagingMessage> getAllMessages() {
		return dao.getAllMessages();
	}
	
	public SGSMessagingMessage getMessage(Integer messageId) {
		return dao.getMessage(messageId);
	}
	
	public List<SGSMessagingMessage> getMessagesToPerson(Person recipient) {
		return dao.findMessagesWithPeople(recipient, null, null, null);
	}
	
	public List<SGSMessagingMessage> getMessagesFromPerson(Person sender) {
		return dao.findMessagesWithPeople(null, sender, null, null);
	}
	
	public List<SGSMessagingMessage> getMessagesToOrFromPerson(Person person) {
		return dao.findMessagesWithPeople(person, person, null, null);
	}
	
	public void deleteMessage(SGSMessagingMessage message) throws APIException {
		dao.deleteMessage(message);
	}
	
	public void saveMessage(SGSMessagingMessage message) throws APIException {
		dao.saveMessage(message);
	}
	
	public List<SGSMessagingMessage> getOutboxMessages() {
		return dao.getOutboxMessages();
	}
	
	public String getPersonName(Person person) {
		String patientName = "";
		if (person.getMiddleName() == null) {
			patientName = person.getFamilyName() + " " + person.getGivenName();
		} else {
			patientName = person.getFamilyName() + " " + person.getMiddleName() + " " + person.getGivenName();
		}
		return patientName;
	}
	
	/**
	 * Gets a list of MessagingConfig objects from calling {@link #getMessagingConfig()}. Each
	 * configuration object from that list is used to read the service configured, the number of days
	 * before the appointment day and the actual message to send. We then call {@link #postMessage()} of
	 * MessagingUtil to send the actual message
	 */
	
	@Override
	public void sendAppointmentReminders() throws AuthenticationException, ClientProtocolException, IOException {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String phoneAttributeName = Context.getAdministrationService().getGlobalProperty("sgsmessaging.phoneAttribute");
		try {
			List<SGSMessagingConfig> configs = SGSMessagingUtil.getMessagingConfig();
			for (SGSMessagingConfig messagingConfig : configs) {
				AppointmentServiceDefinition service = Context.getService(AppointmentServiceDefinitionService.class).getAppointmentServiceByUuid(messagingConfig.getServiceUUID());
				List<Appointment> appointments = Context.getService(AppointmentsService.class).getAllFutureAppointmentsForService(service);
				String phone = null;
				PersonAttribute phoneAttribute = null;
				for (Appointment appointment : appointments) {
					if (Days.daysBetween(new DateTime(new Date()), new DateTime(appointment.getStartDateTime())).getDays() == messagingConfig.getDaysBefore() - 1) {
						phoneAttribute = Context.getPersonService().getPerson(appointment.getPatient().getPatientId()).getAttribute(phoneAttributeName);
						if (phoneAttribute != null) {
							phone = phoneAttribute.getValue();
						}
						if (phone != null && phone.length() > 0) {
							Patient p = appointment.getPatient();
							String patientName = getPersonName(p);
							Date appointmentDate = appointment.getStartDateTime();
							String messageAfterNameReplace = messagingConfig.getMessageText().replace("patientName", patientName);
							String messageAfterAppointmentDateReplace = messageAfterNameReplace.replace("appointmentDate", dateFormat.format(appointmentDate));						
							SGSMessagingUtil.postMessage(phone, messageAfterAppointmentDateReplace);
						}
					}
				}
				
			}
		}
		catch (Exception e) {
			log.error("There was an error sending appointment reminders" + e);
		}
		
	}
	
	@Override
	public void sendLabResultsNotifications() throws AuthenticationException, ClientProtocolException, IOException {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		
		try {
			List<SGSMessagingMessage> messages = getAllMessages();
			for (SGSMessagingMessage message : messages) {
				String phone = message.getDestination();
				if (phone != null && phone.length() > 0) {
					Person person = message.getRecipient();
					String patientName = getPersonName(person);
					String messageAfterNameReplace = message.getContent().replace("patientName", patientName);
					CloseableHttpResponse response = SGSMessagingUtil.postMessage(phone, messageAfterNameReplace);
				}
				
			}
		}
		catch (Exception e) {
			log.error("There was an error sending appointment reminders" + e);
		}
		
	}
}
