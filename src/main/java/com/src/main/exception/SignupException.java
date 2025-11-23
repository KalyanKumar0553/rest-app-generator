package com.src.main.exception;

import com.src.main.util.RequestStatus;

public class SignupException extends AbstractRuntimeException {

	public SignupException(RequestStatus error,Object... msgParams) {
        super(error.getCode(),error.getDescription(msgParams));
    }
}
