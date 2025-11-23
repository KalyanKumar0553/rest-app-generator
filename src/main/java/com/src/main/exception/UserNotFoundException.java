package com.src.main.exception;

import com.src.main.util.RequestStatus;

public class UserNotFoundException extends AbstractRuntimeException {

	public UserNotFoundException(RequestStatus error,Object... msgParams) {
        super(error.getCode(),error.getDescription(msgParams));
    }
}
