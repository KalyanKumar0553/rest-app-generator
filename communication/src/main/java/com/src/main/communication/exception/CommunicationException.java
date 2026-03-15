package com.src.main.communication.exception;

import com.src.main.common.exception.AbstractRuntimeException;
import com.src.main.common.util.RequestStatus;

public class CommunicationException extends AbstractRuntimeException {

	public CommunicationException(RequestStatus error, Object... msgParams) {
		super(error.getCode(), error.getDescription(msgParams));
	}
}
