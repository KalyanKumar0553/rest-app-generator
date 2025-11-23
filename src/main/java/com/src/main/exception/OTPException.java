package com.src.main.exception;

import com.src.main.util.RequestStatus;

public class OTPException extends AbstractRuntimeException {

	public OTPException(RequestStatus error,Object... msgParams) {
        super(error.getCode(),error.getDescription(msgParams));
    }
}
