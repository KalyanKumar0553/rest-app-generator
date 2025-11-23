package com.src.main.exception;

import com.src.main.util.RequestStatus;

public class InvalidRequestException extends AbstractRuntimeException {

	public InvalidRequestException(RequestStatus error,Object... msgParams) {
        super(error.getCode(),error.getDescription(msgParams));
    }
}
