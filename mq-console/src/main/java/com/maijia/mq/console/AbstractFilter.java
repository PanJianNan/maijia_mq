package com.maijia.mq.console;

public abstract class AbstractFilter {
	
	
	public enum CheckResult{
		ILLEGAL_URI,
		NOT_STARTSERVER,
		NONE_HTTPREQUEST_POST,
		NULL_PARAM,
		NONE_JSON_FORMAT_PARAM,
		NULL_SYSTEMID,
		NULL_UUID_UUIDKEY_SYSTEMID,
		CHECK_FAILURE,
		DUPLICATED_UUID,
		IILEGAL_TOKEN,
		OTHER_ERROE,
		OK		
	}
	
	protected AbstractFilter successor=null;

	public AbstractFilter getSuccessor() {
		return successor;
	}

	public void setSuccessor(AbstractFilter successor) {
		this.successor = successor;
	}
	
	public abstract CheckResult filter(Object... inParam);
	
	
}
