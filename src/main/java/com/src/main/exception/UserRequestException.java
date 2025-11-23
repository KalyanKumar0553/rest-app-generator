package com.src.main.exception;

import com.src.main.util.RequestStatus;

public class UserRequestException extends AbstractRuntimeException {

	public UserRequestException(RequestStatus error,Object... msgParams) {
        super(error.getCode(),error.getDescription(msgParams));
    }
}
