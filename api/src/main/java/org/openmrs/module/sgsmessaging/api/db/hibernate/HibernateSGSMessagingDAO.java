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
package org.openmrs.module.sgsmessaging.api.db.hibernate;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Person;
import org.openmrs.module.sgsmessaging.api.db.SGSMessagingDAO;
import org.openmrs.module.sgsmessaging.domain.SGSMessagingMessage;
import org.openmrs.module.sgsmessaging.domain.SGSMessagingMessageStatus;

/**
 * It is a default implementation of {@link SGSMessagingDAO}.
 */
public class HibernateSGSMessagingDAO implements SGSMessagingDAO {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private SessionFactory sessionFactory;
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public List<SGSMessagingMessage> getAllMessages() {
		return getCurrentSession().createCriteria(SGSMessagingMessage.class).list();
	}
	
	public SGSMessagingMessage getMessage(Integer messageId) {
		return (SGSMessagingMessage) getCurrentSession().createCriteria(SGSMessagingMessage.class).add(Restrictions.eq("messageId", messageId)).list().get(0);
	}
	
	public List<SGSMessagingMessage> findMessagesWithPeople(Person sender, Person recipient, String content, Integer status) {
		Criteria c = getCurrentSession().createCriteria(SGSMessagingMessage.class);
		if (sender.equals(recipient) && sender != null) {
			c.add(Restrictions.or(Restrictions.eq("sender", sender), Restrictions.eq("recipient", recipient)));
		} else {
			if (sender != null) {
				c.add(Restrictions.eq("sender", sender));
			}
			if (recipient != null) {
				c.add(Restrictions.eq("recipient", recipient));
			}
		}
		if (content != null && !content.equals("")) {
			c.add(Restrictions.like("content", "%" + content + "%"));
		}
		if (status != null) {
			c.add(Restrictions.eq("status", status));
		}
		c.addOrder(Order.asc("date"));
		return c.list();
	}
	
	public void deleteMessage(SGSMessagingMessage message) {
		getCurrentSession().delete(message);
	}
	
	public void saveMessage(SGSMessagingMessage message) {
		getCurrentSession().saveOrUpdate(message);
	}
	
	public List<SGSMessagingMessage> getOutboxMessages() {
		Criteria c = getCurrentSession().createCriteria(SGSMessagingMessage.class);
		c.add(Restrictions.eq("status", SGSMessagingMessageStatus.OUTBOX.getNumber()));
		return c.list();
	}
	
	/**
	 * Gets the current hibernate session while taking care of the hibernate 3 and 4 differences.
	 * 
	 * @return the current hibernate session.
	 */
	private org.hibernate.Session getCurrentSession() {
		try {
			return sessionFactory.getCurrentSession();
		}
		catch (NoSuchMethodError ex) {
			try {
				Method method = sessionFactory.getClass().getMethod("getCurrentSession", null);
				return (org.hibernate.Session) method.invoke(sessionFactory, null);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to get the current hibernate session", e);
			}
		}
	}
	
}
