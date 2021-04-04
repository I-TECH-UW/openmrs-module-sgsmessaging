package org.openmrs.module.sgsmessaging.domain;

public enum SGSMessagingMessageStatus {
	SENT(0),
	RETRYING(1),
	FAILED(2),
	OUTBOX(3);
	
	private int number;
	
	private SGSMessagingMessageStatus(int number) {
		this.number = number;
	}
	
	public static SGSMessagingMessageStatus getStatusByNumber(int number) {
		for (SGSMessagingMessageStatus m : SGSMessagingMessageStatus.values()) {
			if (m.getNumber() == number) {
				return m;
			}
		}
		return null;
	}
	
	/**
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}
}
