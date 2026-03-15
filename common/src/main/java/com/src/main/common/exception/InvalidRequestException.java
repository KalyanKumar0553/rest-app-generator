package com.src.main.common.exception;

import com.src.main.common.util.RequestStatus;

public class InvalidRequestException extends AbstractRuntimeException {

	public InvalidRequestException(RequestStatus error,Object... msgParams) {
        super(error.getCode(),error.getDescription(msgParams));
    }
}
